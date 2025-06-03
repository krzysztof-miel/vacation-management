package com.example.vacation_management.controller;


import com.example.vacation_management.dto.LeaveBalanceDto;
import com.example.vacation_management.entity.LeaveBalance;
import com.example.vacation_management.service.LeaveBalanceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-balances")
public class LeaveBalanceController {

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LeaveBalance>> getAllBalancesForUser(@PathVariable Long userId) {
        List<LeaveBalance> balances = leaveBalanceService.getAllBalancesForUser(userId);
        return ResponseEntity.ok(balances);
    }

    @GetMapping("/user/{userId}/current")
    public ResponseEntity<LeaveBalance> getCurrentYearBalance(@PathVariable Long userId) {
        return leaveBalanceService.getCurrentYearBalance(userId)
                .map(balance -> ResponseEntity.ok(balance))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}/year/{year}")
    public ResponseEntity<LeaveBalance> getBalanceByUserAndYear(
            @PathVariable Long userId, @PathVariable Integer year) {
        return leaveBalanceService.getBalanceByUserAndYear(userId, year)
                .map(balance -> ResponseEntity.ok(balance))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<List<LeaveBalanceDto>> getAllBalancesForYear(@PathVariable Integer year) {
        List<LeaveBalanceDto> balances = leaveBalanceService.getAllBalancesForYear(year);
        return ResponseEntity.ok(balances);
    }

    @PostMapping
    public ResponseEntity<LeaveBalance> createBalance(@Valid @RequestBody LeaveBalanceDto balanceDto) {
        try {
            LeaveBalance createdBalance = leaveBalanceService.createBalance(balanceDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBalance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveBalance> updateBalance(
            @PathVariable Long id, @Valid @RequestBody LeaveBalanceDto balanceDto) {
        try {
            LeaveBalance updatedBalance = leaveBalanceService.updateBalance(id, balanceDto);
            return ResponseEntity.ok(updatedBalance);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/user/{userId}/use-days")
    public ResponseEntity<LeaveBalance> useDays(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") Integer year,
            @RequestParam Integer days) {
        try {
            LeaveBalance updatedBalance = leaveBalanceService.useDays(userId, year, days);
            return ResponseEntity.ok(updatedBalance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/user/{userId}/return-days")
    public ResponseEntity<LeaveBalance> returnDays(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") Integer year,
            @RequestParam Integer days) {
        try {
            LeaveBalance updatedBalance = leaveBalanceService.returnDays(userId, year, days);
            return ResponseEntity.ok(updatedBalance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}/check-days")
    public ResponseEntity<Boolean> checkAvailableDays(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") Integer year,
            @RequestParam Integer requiredDays) {
        boolean hasEnough = leaveBalanceService.hasEnoughDays(userId, year, requiredDays);
        return ResponseEntity.ok(hasEnough);
    }

}
