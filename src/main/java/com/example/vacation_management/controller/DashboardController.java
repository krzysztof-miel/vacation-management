package com.example.vacation_management.controller;


import com.example.vacation_management.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/year/{year}")
    public ResponseEntity<Map<String, Object>> getYearlyStats(@PathVariable Integer year) {
        Map<String, Object> stats = dashboardService.getYearlyStats(year);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/calendar/{year}/{month}")
    public ResponseEntity<Map<String, Object>> getLeaveCalendar(
            @PathVariable Integer year, @PathVariable Integer month) {
        Map<String, Object> calendar = dashboardService.getLeaveCalendar(year, month);
        return ResponseEntity.ok(calendar);
    }

    @GetMapping("/reports/team-summary")
    public ResponseEntity<Map<String, Object>> getTeamSummary() {
        Map<String, Object> summary = dashboardService.getTeamSummary();
        return ResponseEntity.ok(summary);
    }

}
