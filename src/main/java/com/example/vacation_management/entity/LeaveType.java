package com.example.vacation_management.entity;

public enum LeaveType {

    PAID("Paid Leave"),
    UNPAID("Unpaid Leave");

    private final String displayName;

    LeaveType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
