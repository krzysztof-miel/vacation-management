package com.example.vacation_management.repository;

import com.example.vacation_management.entity.LeaveBalance;
import com.example.vacation_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    Optional<LeaveBalance> findByUserAndYear(User user, Integer year);

    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.user.id = :userId AND lb.year = :year")
    Optional<LeaveBalance> findByUserIdAndYear(@Param("userId") Long userId, @Param("year") Integer year);

    List<LeaveBalance> findByUser(User user);

    List<LeaveBalance> findByUserId(Long userId);

    List<LeaveBalance> findByYear(Integer year);

    boolean existsByUserAndYear(User user, Integer year);

    @Query("SELECT COUNT(lb) > 0 FROM LeaveBalance lb WHERE lb.user.id = :userId AND lb.year = :year")
    boolean existsByUserIdAndYear(@Param("userId") Long userId, @Param("year") Integer year);

    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.year = :year AND lb.remainingDays <= :threshold")
    List<LeaveBalance> findUsersWithLowVacationDays(@Param("year") Integer year, @Param("threshold") Integer threshold);


}
