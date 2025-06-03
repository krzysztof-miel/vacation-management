package com.example.vacation_management.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


public class LeaveBalanceDto {

    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Total days is required")
    @Min(value = 0, message = "Total days cannot be negative")
    private Integer totalDays;

    @Min(value = 0, message = "Used days cannot be negative")
    private Integer usedDays;

    private Integer remainingDays;

    @NotNull(message = "Year is required")
    private Integer year;

    private String userEmail;
    private String userFullName;

    public LeaveBalanceDto() {}

    public LeaveBalanceDto(Long userId, Integer totalDays, Integer year) {
        this.userId = userId;
        this.totalDays = totalDays;
        this.year = year;
        this.usedDays = 0;
        this.remainingDays = totalDays;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(Integer totalDays) {
        this.totalDays = totalDays;
        updateRemainingDays();
    }

    public Integer getUsedDays() {
        return usedDays;
    }

    public void setUsedDays(Integer usedDays) {
        this.usedDays = usedDays;
        updateRemainingDays();
    }

    public Integer getRemainingDays() {
        return remainingDays;
    }

    public void setRemainingDays(Integer remainingDays) {
        this.remainingDays = remainingDays;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    private void updateRemainingDays() {
        if (this.totalDays != null && this.usedDays != null) {
            this.remainingDays = this.totalDays - this.usedDays;
        }
    }

}
