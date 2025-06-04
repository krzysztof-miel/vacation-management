package com.example.vacation_management.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "leave_requests")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @Column(name = "start_date", nullable = false)
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @Column(name = "working_days", nullable = false)
    private Integer workingDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false)
    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public LeaveRequest() {
        this.createdAt = LocalDateTime.now();
        this.status = LeaveStatus.PENDING;
    }

    public LeaveRequest(User user, LocalDate startDate, LocalDate endDate, LeaveType leaveType) {
        this();
        this.user = user;
        this.startDate = startDate;
        this.endDate = endDate;
        this.leaveType = leaveType;
        calculateWorkingDays();
    }

    public void calculateWorkingDays() {
        if (startDate != null && endDate != null) {
            long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            int workingDaysCount = 0;

            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                if (currentDate.getDayOfWeek().getValue() < 6) {
                    workingDaysCount++;
                }
                currentDate = currentDate.plusDays(1);
            }

            this.workingDays = workingDaysCount;
        }
    }

    public boolean canBeCancelled() {
        if (this.status != LeaveStatus.APPROVED) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return ChronoUnit.DAYS.between(today, this.startDate) >= 7;
    }

    public boolean hasStarted() {
        return LocalDate.now().isAfter(this.startDate) || LocalDate.now().isEqual(this.startDate);
    }

    public boolean hasEnded() {
        return LocalDate.now().isAfter(this.endDate);
    }

    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return (today.isEqual(startDate) || today.isAfter(startDate)) &&
                (today.isEqual(endDate) || today.isBefore(endDate));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        calculateWorkingDays();
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        calculateWorkingDays();
    }

    public Integer getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(Integer workingDays) {
        this.workingDays = workingDays;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public void setStatus(LeaveStatus status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "LeaveRequest{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", workingDays=" + workingDays +
                ", leaveType=" + leaveType +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }

}
