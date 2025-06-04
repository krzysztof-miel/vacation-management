package com.example.vacation_management.repository;

import com.example.vacation_management.entity.LeaveRequest;
import com.example.vacation_management.entity.LeaveStatus;
import com.example.vacation_management.entity.LeaveType;
import com.example.vacation_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByUser(User user);

    List<LeaveRequest> findByUserId(Long userId);

    List<LeaveRequest> findByUserAndStatus(User user, LeaveStatus status);

    List<LeaveRequest> findByUserIdAndStatus(Long userId, LeaveStatus status);

    List<LeaveRequest> findByStatus(LeaveStatus status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.startDate <= :endDate AND lr.endDate >= :startDate")
    List<LeaveRequest> findOverlappingRequests(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = 'APPROVED' AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    List<LeaveRequest> findApprovedOverlappingRequests(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT lr FROM LeaveRequest lr WHERE YEAR(lr.startDate) = :year OR YEAR(lr.endDate) = :year")
    List<LeaveRequest> findByYear(@Param("year") Integer year);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId AND (YEAR(lr.startDate) = :year OR YEAR(lr.endDate) = :year)")
    List<LeaveRequest> findByUserIdAndYear(@Param("userId") Long userId, @Param("year") Integer year);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId AND lr.status = 'APPROVED' AND (YEAR(lr.startDate) = :year OR YEAR(lr.endDate) = :year)")
    List<LeaveRequest> findApprovedByUserIdAndYear(@Param("userId") Long userId, @Param("year") Integer year);

    List<LeaveRequest> findByLeaveType(LeaveType leaveType);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = 'APPROVED' AND :today BETWEEN lr.startDate AND lr.endDate")
    List<LeaveRequest> findActiveLeaves(@Param("today") LocalDate today);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = 'APPROVED' AND lr.startDate BETWEEN :today AND :futureDate")
    List<LeaveRequest> findUpcomingLeaves(@Param("today") LocalDate today, @Param("futureDate") LocalDate futureDate);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = 'PENDING' ORDER BY lr.createdAt ASC")
    List<LeaveRequest> findPendingRequests();

    @Query("SELECT COUNT(lr) > 0 FROM LeaveRequest lr WHERE lr.user.id = :userId AND lr.status = 'APPROVED' AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    boolean hasApprovedLeaveInPeriod(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


}
