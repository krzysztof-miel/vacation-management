package com.example.vacation_management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


@Entity
@Table(name = "leave_balances",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "year"}))
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_days", nullable = false)
    @NotNull(message = "Total days is required")
    @Min(value = 0, message = "Total days cannot be negative")
    private Integer totalDays;

    @Column(name = "used_days", nullable = false)
    @Min(value = 0, message = "Used days cannot be negative")
    private Integer usedDays = 0;

    @Column(name = "remaining_days", nullable = false)
    @Min(value = 0, message = "Remaining days cannot be negative")
    private Integer remainingDays;

    @Column(nullable = false)
    @NotNull(message = "Year is required")
    private Integer year;

    public LeaveBalance() {}

    public LeaveBalance(User user, Integer totalDays, Integer year) {
        this.user = user;
        this.totalDays = totalDays;
        this.year = year;
        this.usedDays = 0;
        this.remainingDays = totalDays;
    }

    public void updateRemainingDays() {
        this.remainingDays = this.totalDays - this.usedDays;
    }

    public boolean canUseDays(Integer daysToUse) {
        return this.remainingDays >= daysToUse;
    }

    public void useDays(Integer daysToUse) {
        if (!canUseDays(daysToUse)) {
            throw new RuntimeException("Not enough remaining vacation days. Available: " +
                    this.remainingDays + ", requested: " + daysToUse);
        }
        this.usedDays += daysToUse;
        updateRemainingDays();
    }

    public void returnDays(Integer daysToReturn) {
        this.usedDays = Math.max(0, this.usedDays - daysToReturn);
        updateRemainingDays();
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

    @Override
    public String toString() {
        return "LeaveBalance{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", totalDays=" + totalDays +
                ", usedDays=" + usedDays +
                ", remainingDays=" + remainingDays +
                ", year=" + year +
                '}';
    }
}
