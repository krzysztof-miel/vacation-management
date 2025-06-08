package com.example.vacation_management.service;


import com.example.vacation_management.entity.LeaveRequest;
import com.example.vacation_management.entity.LeaveStatus;
import com.example.vacation_management.entity.Role;
import com.example.vacation_management.entity.User;
import com.example.vacation_management.repository.LeaveBalanceRepository;
import com.example.vacation_management.repository.LeaveRequestRepository;
import com.example.vacation_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userRepository.count();
        long totalEmployees = userRepository.countByRole(Role.EMPLOYEE);
        long totalAdmins = userRepository.countByRole(Role.ADMIN);

        long pendingRequests = leaveRequestRepository.findByStatus(LeaveStatus.PENDING).size();
        long approvedRequests = leaveRequestRepository.findByStatus(LeaveStatus.APPROVED).size();
        long activeLeaves = leaveRequestRepository.findActiveLeaves(LocalDate.now()).size();

        LocalDate nextWeek = LocalDate.now().plusDays(7);
        long upcomingLeaves = leaveRequestRepository.findUpcomingLeaves(LocalDate.now(), nextWeek).size();

        stats.put("totalUsers", totalUsers);
        stats.put("totalEmployees", totalEmployees);
        stats.put("totalAdmins", totalAdmins);
        stats.put("pendingRequests", pendingRequests);
        stats.put("approvedRequests", approvedRequests);
        stats.put("activeLeaves", activeLeaves);
        stats.put("upcomingLeaves", upcomingLeaves);
        stats.put("generatedAt", LocalDate.now());

        return stats;
    }

    public Map<String, Object> getYearlyStats(Integer year) {
        Map<String, Object> stats = new HashMap<>();

        List<LeaveRequest> yearRequests = leaveRequestRepository.findByYear(year);

        Map<LeaveStatus, Long> statusCounts = yearRequests.stream()
                .collect(Collectors.groupingBy(LeaveRequest::getStatus, Collectors.counting()));

        Map<Integer, Long> monthlyCounts = yearRequests.stream()
                .collect(Collectors.groupingBy(
                        req -> req.getStartDate().getMonthValue(),
                        Collectors.counting()
                ));

        int totalDaysUsed = yearRequests.stream()
                .filter(req -> req.getStatus() == LeaveStatus.APPROVED)
                .mapToInt(LeaveRequest::getWorkingDays)
                .sum();

        stats.put("year", year);
        stats.put("totalRequests", yearRequests.size());
        stats.put("statusBreakdown", statusCounts);
        stats.put("monthlyBreakdown", monthlyCounts);
        stats.put("totalDaysUsed", totalDaysUsed);

        return stats;
    }

    public Map<String, Object> getLeaveCalendar(Integer year, Integer month) {
        Map<String, Object> calendar = new HashMap<>();

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<LeaveRequest> monthLeaves = leaveRequestRepository.findApprovedOverlappingRequests(startDate, endDate);

        Map<LocalDate, List<Map<String, Object>>> dailyLeaves = new HashMap<>();

        for (LeaveRequest leave : monthLeaves) {
            LocalDate current = leave.getStartDate().isAfter(startDate) ? leave.getStartDate() : startDate;
            LocalDate end = leave.getEndDate().isBefore(endDate) ? leave.getEndDate() : endDate;

            while (!current.isAfter(end)) {
                dailyLeaves.computeIfAbsent(current, k -> new ArrayList<>()).add(Map.of(
                        "userId", leave.getUser().getId(),
                        "userName", leave.getUser().getFirstName() + " " + leave.getUser().getLastName(),
                        "leaveType", leave.getLeaveType(),
                        "isFullLeave", current.equals(leave.getStartDate()) && current.equals(leave.getEndDate())
                ));
                current = current.plusDays(1);
            }
        }

        calendar.put("year", year);
        calendar.put("month", month);
        calendar.put("monthName", yearMonth.getMonth().name());
        calendar.put("daysInMonth", yearMonth.lengthOfMonth());
        calendar.put("dailyLeaves", dailyLeaves);

        return calendar;
    }

    public Map<String, Object> getTeamSummary() {
        Map<String, Object> summary = new HashMap<>();

        List<User> employees = userRepository.findByRole(Role.EMPLOYEE);
        List<Map<String, Object>> teamMembers = new ArrayList<>();

        int currentYear = LocalDate.now().getYear();

        for (User employee : employees) {
            Map<String, Object> memberData = new HashMap<>();
            memberData.put("userId", employee.getId());
            memberData.put("name", employee.getFirstName() + " " + employee.getLastName());
            memberData.put("email", employee.getEmail());

            List<LeaveRequest> userRequests = leaveRequestRepository.findApprovedByUserIdAndYear(employee.getId(), currentYear);
            int daysUsed = userRequests.stream().mapToInt(LeaveRequest::getWorkingDays).sum();

            leaveBalanceRepository.findByUserIdAndYear(employee.getId(), currentYear)
                    .ifPresentOrElse(
                            balance -> {
                                memberData.put("totalDays", balance.getTotalDays());
                                memberData.put("usedDays", balance.getUsedDays());
                                memberData.put("remainingDays", balance.getRemainingDays());
                            },
                            () -> {
                                memberData.put("totalDays", 0);
                                memberData.put("usedDays", 0);
                                memberData.put("remainingDays", 0);
                            }
                    );

            boolean currentlyOnLeave = leaveRequestRepository.findActiveLeaves(LocalDate.now())
                    .stream().anyMatch(req -> req.getUser().getId().equals(employee.getId()));
            memberData.put("currentlyOnLeave", currentlyOnLeave);

            long totalRequests = leaveRequestRepository.findByUserIdAndYear(employee.getId(), currentYear).size();
            memberData.put("totalRequests", totalRequests);

            teamMembers.add(memberData);
        }

        summary.put("year", currentYear);
        summary.put("totalEmployees", employees.size());
        summary.put("teamMembers", teamMembers);

        return summary;
    }

}
