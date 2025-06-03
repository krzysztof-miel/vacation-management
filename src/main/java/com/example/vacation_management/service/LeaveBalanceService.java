package com.example.vacation_management.service;


import com.example.vacation_management.dto.LeaveBalanceDto;
import com.example.vacation_management.entity.LeaveBalance;
import com.example.vacation_management.entity.User;
import com.example.vacation_management.repository.LeaveBalanceRepository;
import com.example.vacation_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LeaveBalanceService {

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<LeaveBalance> getCurrentYearBalance(Long userId) {
        int currentYear = LocalDate.now().getYear();
        return leaveBalanceRepository.findByUserIdAndYear(userId, currentYear);
    }

    public Optional<LeaveBalance> getBalanceByUserAndYear(Long userId, Integer year) {
        return leaveBalanceRepository.findByUserIdAndYear(userId, year);
    }

    public List<LeaveBalance> getAllBalancesForUser(Long userId) {
        return leaveBalanceRepository.findByUserId(userId);
    }

    public LeaveBalance createBalance(LeaveBalanceDto balanceDto) {

        User user = userRepository.findById(balanceDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + balanceDto.getUserId()));

        if (leaveBalanceRepository.existsByUserIdAndYear(balanceDto.getUserId(), balanceDto.getYear())) {
            throw new RuntimeException("Leave balance for user " + balanceDto.getUserId() +
                    " and year " + balanceDto.getYear() + " already exists");
        }

        LeaveBalance balance = new LeaveBalance();
        balance.setUser(user);
        balance.setTotalDays(balanceDto.getTotalDays());
        balance.setYear(balanceDto.getYear());
        balance.setUsedDays(balanceDto.getUsedDays() != null ? balanceDto.getUsedDays() : 0);
        balance.updateRemainingDays();

        return leaveBalanceRepository.save(balance);
    }

    public LeaveBalance updateBalance(Long balanceId, LeaveBalanceDto balanceDto) {
        LeaveBalance balance = leaveBalanceRepository.findById(balanceId)
                .orElseThrow(() -> new RuntimeException("Leave balance not found with id: " + balanceId));

        balance.setTotalDays(balanceDto.getTotalDays());
        if (balanceDto.getUsedDays() != null) {
            balance.setUsedDays(balanceDto.getUsedDays());
        }
        balance.updateRemainingDays();

        return leaveBalanceRepository.save(balance);
    }

    public LeaveBalance createDefaultBalanceForUser(User user, Integer year, Integer defaultDays) {
        if (leaveBalanceRepository.existsByUserAndYear(user, year)) {
            throw new RuntimeException("Leave balance for this user and year already exists");
        }

        LeaveBalance balance = new LeaveBalance(user, defaultDays, year);
        return leaveBalanceRepository.save(balance);
    }

    public LeaveBalance useDays(Long userId, Integer year, Integer daysToUse) {
        LeaveBalance balance = leaveBalanceRepository.findByUserIdAndYear(userId, year)
                .orElseThrow(() -> new RuntimeException("Leave balance not found for user " + userId + " and year " + year));

        balance.useDays(daysToUse);
        return leaveBalanceRepository.save(balance);
    }

    public LeaveBalance returnDays(Long userId, Integer year, Integer daysToReturn) {
        LeaveBalance balance = leaveBalanceRepository.findByUserIdAndYear(userId, year)
                .orElseThrow(() -> new RuntimeException("Leave balance not found for user " + userId + " and year " + year));

        balance.returnDays(daysToReturn);
        return leaveBalanceRepository.save(balance);
    }

    public List<LeaveBalanceDto> getAllBalancesForYear(Integer year) {
        return leaveBalanceRepository.findByYear(year).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public boolean hasEnoughDays(Long userId, Integer year, Integer requiredDays) {
        Optional<LeaveBalance> balance = leaveBalanceRepository.findByUserIdAndYear(userId, year);
        return balance.map(b -> b.canUseDays(requiredDays)).orElse(false);
    }

    private LeaveBalanceDto convertToDto(LeaveBalance balance) {
        LeaveBalanceDto dto = new LeaveBalanceDto();
        dto.setId(balance.getId());
        dto.setUserId(balance.getUser().getId());
        dto.setTotalDays(balance.getTotalDays());
        dto.setUsedDays(balance.getUsedDays());
        dto.setRemainingDays(balance.getRemainingDays());
        dto.setYear(balance.getYear());
        dto.setUserEmail(balance.getUser().getEmail());
        dto.setUserFullName(balance.getUser().getFirstName() + " " + balance.getUser().getLastName());
        return dto;
    }

}
