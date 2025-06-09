package com.example.register.register.repository;

import com.example.register.register.model.Attendance;
import com.example.register.register.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByAttendanceDateAndStatusAndUser_Team_Id(LocalDate date, String status, Long teamId);
    Optional<Attendance> findByUserAndAttendanceDate(User user, LocalDate date);
    List<Attendance> findByAttendanceDateAndStatus(LocalDate attendanceDate, String status);
    List<Attendance> findByAttendanceDateAndWorkMode(LocalDate date, String workMode);

}
