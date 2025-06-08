package com.example.vacation_management.controller;

import com.example.vacation_management.dto.LeaveRequestDto;
import com.example.vacation_management.entity.LeaveRequest;
import com.example.vacation_management.service.LeaveRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    @GetMapping
    public ResponseEntity<List<LeaveRequestDto>> getAllRequests() {
        List<LeaveRequestDto> requests = leaveRequestService.getAllRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequest> getRequestById(@PathVariable Long id) {
        return leaveRequestService.getRequestById(id)
                .map(request -> ResponseEntity.ok(request))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LeaveRequestDto>> getRequestsByUser(
            @PathVariable Long userId, HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("userId");
        String currentUserRole = (String) request.getAttribute("userRole");

        if (!"ADMIN".equals(currentUserRole) && !currentUserId.equals(userId)) {
            throw new RuntimeException("Access denied: You can only view your own leave requests");
        }

        List<LeaveRequestDto> requests = leaveRequestService.getRequestsByUser(userId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<LeaveRequestDto>> getPendingRequests() {
        List<LeaveRequestDto> requests = leaveRequestService.getPendingRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/active")
    public ResponseEntity<List<LeaveRequestDto>> getActiveLeaves() {
        List<LeaveRequestDto> requests = leaveRequestService.getActiveLeaves();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<LeaveRequestDto>> getUpcomingLeaves() {
        List<LeaveRequestDto> requests = leaveRequestService.getUpcomingLeaves();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/my")
    public ResponseEntity<List<LeaveRequestDto>> getMyRequests(HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId == null) {
            throw new RuntimeException("User not authenticated");
        }

        List<LeaveRequestDto> requests = leaveRequestService.getRequestsByUser(currentUserId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/my/current-year")
    public ResponseEntity<List<LeaveRequestDto>> getMyCurrentYearRequests(HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId == null) {
            throw new RuntimeException("User not authenticated");
        }

        List<LeaveRequestDto> requests = leaveRequestService.getRequestsByUserAndYear(currentUserId,
                java.time.LocalDate.now().getYear());
        return ResponseEntity.ok(requests);
    }

    @PostMapping
    public ResponseEntity<LeaveRequest> createLeaveRequest(
            @Valid @RequestBody LeaveRequestDto requestDto, HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("userId");
        String currentUserRole = (String) request.getAttribute("userRole");

        if (!"ADMIN".equals(currentUserRole) && !currentUserId.equals(requestDto.getUserId())) {
            throw new RuntimeException("Access denied: You can only create leave requests for yourself");
        }

        LeaveRequest createdRequest = leaveRequestService.createLeaveRequest(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveRequest> approveRequest(@PathVariable Long id) {
        LeaveRequest approvedRequest = leaveRequestService.approveRequest(id);
        return ResponseEntity.ok(approvedRequest);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveRequest> rejectRequest(@PathVariable Long id) {
        LeaveRequest rejectedRequest = leaveRequestService.rejectRequest(id);
        return ResponseEntity.ok(rejectedRequest);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<LeaveRequest> cancelRequest(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        String currentUserRole = (String) request.getAttribute("userRole");

        if (!"ADMIN".equals(currentUserRole)) {
            LeaveRequest request2 = leaveRequestService.getRequestById(id)
                    .orElseThrow(() -> new RuntimeException("Leave request not found"));

            if (!currentUserId.equals(request2.getUser().getId())) {
                throw new RuntimeException("Access denied: You can only cancel your own leave requests");
            }
        }

        LeaveRequest cancelledRequest = leaveRequestService.cancelRequest(id);
        return ResponseEntity.ok(cancelledRequest);
    }

}
