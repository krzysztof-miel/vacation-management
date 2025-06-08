package com.example.vacation_management.service;


import com.example.vacation_management.dto.LeaveRequestDto;
import com.example.vacation_management.entity.LeaveRequest;
import com.example.vacation_management.entity.LeaveStatus;
import com.example.vacation_management.entity.LeaveType;
import com.example.vacation_management.entity.User;
import com.example.vacation_management.repository.LeaveRequestRepository;
import com.example.vacation_management.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    public List<LeaveRequestDto> getAllRequests() {
        return leaveRequestRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<LeaveRequest> getRequestById(Long id) {
        return leaveRequestRepository.findById(id);
    }

    public List<LeaveRequestDto> getRequestsByUser(Long userId) {
        return leaveRequestRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<LeaveRequestDto> getRequestsByUserAndYear(Long userId, Integer year) {
        return leaveRequestRepository.findByUserIdAndYear(userId, year).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<LeaveRequestDto> getPendingRequests() {
        return leaveRequestRepository.findPendingRequests().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public LeaveRequest createLeaveRequest(LeaveRequestDto requestDto) {

        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + requestDto.getUserId()));


        validateDates(requestDto.getStartDate(), requestDto.getEndDate());


        if (leaveRequestRepository.hasApprovedLeaveInPeriod(
                requestDto.getUserId(), requestDto.getStartDate(), requestDto.getEndDate())) {
            throw new RuntimeException("User already has approved leave in this period");
        }


        if (!canApproveRequest(requestDto.getStartDate(), requestDto.getEndDate())) {
            throw new RuntimeException("Cannot create request: maximum 2 people can be on leave at the same time");
        }


        if (requestDto.getLeaveType() == LeaveType.PAID) {
            LeaveRequest tempRequest = new LeaveRequest();
            tempRequest.setStartDate(requestDto.getStartDate());
            tempRequest.setEndDate(requestDto.getEndDate());
            tempRequest.calculateWorkingDays();

            int year = requestDto.getStartDate().getYear();
            if (!leaveBalanceService.hasEnoughDays(requestDto.getUserId(), year, tempRequest.getWorkingDays())) {
                throw new RuntimeException("Not enough vacation days available");
            }
        }


        LeaveRequest request = new LeaveRequest();
        request.setUser(user);
        request.setStartDate(requestDto.getStartDate());
        request.setEndDate(requestDto.getEndDate());
        request.setLeaveType(requestDto.getLeaveType());
        request.setComment(requestDto.getComment());
        request.calculateWorkingDays();

        return leaveRequestRepository.save(request);
    }


    public LeaveRequest approveRequest(Long requestId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + requestId));

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be approved");
        }


        if (!canApproveRequest(request.getStartDate(), request.getEndDate())) {
            throw new RuntimeException("Cannot approve: maximum 2 people can be on leave at the same time");
        }


        if (request.getLeaveType() == LeaveType.PAID) {
            int year = request.getStartDate().getYear();
            try {
                leaveBalanceService.useDays(request.getUser().getId(), year, request.getWorkingDays());
            } catch (RuntimeException e) {
                throw new RuntimeException("Cannot approve request: " + e.getMessage());
            }
        }

        request.setStatus(LeaveStatus.APPROVED);

        LeaveRequest savedRequest = leaveRequestRepository.save(request);

        savedRequest.getUser().getFirstName();

        return savedRequest;
    }


    public LeaveRequest rejectRequest(Long requestId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + requestId));

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be rejected");
        }

        request.setStatus(LeaveStatus.REJECTED);
        return leaveRequestRepository.save(request);
    }


    public LeaveRequest cancelRequest(Long requestId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + requestId));

        if (request.getStatus() != LeaveStatus.APPROVED) {
            throw new RuntimeException("Only approved requests can be cancelled");
        }

        if (!request.canBeCancelled()) {
            throw new RuntimeException("Request cannot be cancelled: less than 7 days before start date");
        }


        if (request.getLeaveType() == LeaveType.PAID) {
            int year = request.getStartDate().getYear();
            leaveBalanceService.returnDays(request.getUser().getId(), year, request.getWorkingDays());
        }

        request.setStatus(LeaveStatus.CANCELLED);
        return leaveRequestRepository.save(request);
    }


    private boolean canApproveRequest(LocalDate startDate, LocalDate endDate) {
        List<LeaveRequest> overlapping = leaveRequestRepository.findApprovedOverlappingRequests(startDate, endDate);
        return overlapping.size() < 2;
    }


    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new RuntimeException("Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("Start date cannot be after end date");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot create leave request for past dates");
        }
    }


    public List<LeaveRequestDto> getActiveLeaves() {
        return leaveRequestRepository.findActiveLeaves(LocalDate.now()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    public List<LeaveRequestDto> getUpcomingLeaves() {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        return leaveRequestRepository.findUpcomingLeaves(today, nextWeek).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    private LeaveRequestDto convertToDto(LeaveRequest request) {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setId(request.getId());
        dto.setUserId(request.getUser().getId());
        dto.setStartDate(request.getStartDate());
        dto.setEndDate(request.getEndDate());
        dto.setWorkingDays(request.getWorkingDays());
        dto.setLeaveType(request.getLeaveType());
        dto.setStatus(request.getStatus());
        dto.setComment(request.getComment());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUserEmail(request.getUser().getEmail());
        dto.setUserFullName(request.getUser().getFirstName() + " " + request.getUser().getLastName());
        return dto;
    }

}
