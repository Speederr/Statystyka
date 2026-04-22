package com.example.register.register.repository;

import com.example.register.register.model.Attendance;
import com.example.register.register.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByAttendanceDateAndStatusAndUser_Team_Id(LocalDate date, String status, Long teamId);
    //Optional<Attendance> findByUserAndAttendanceDate(User user, LocalDate date);
    Optional<Attendance> findByUser_IdAndAttendanceDate(Long userId, LocalDate date);
    List<Attendance> findByAttendanceDateAndStatus(LocalDate attendanceDate, String status);
    List<Attendance> findByStatusAndUser_Team_Id(String status, Long teamId);

    @Transactional
    void deleteAllByUserAndAttendanceDateBetweenAndStatus(User user, LocalDate start, LocalDate end, String status);


    @Query("""
    SELECT a FROM Attendance a
    WHERE a.user = :user
      AND a.attendanceDate BETWEEN :start AND :end
      AND a.status = 'leave'
    """)
    List<Attendance> findLeavesInRange(@Param("user") User user, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
