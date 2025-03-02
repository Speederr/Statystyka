package com.example.register.register.repository;

import com.example.register.register.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Sprawdza, czy użytkownik ma wpis o obecności w danym dniu
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.user.username = :username AND a.attendanceDate = :date")
    int countByUsernameAndDate(String username, LocalDate date);

    // Pobiera liczbę użytkowników obecnych w danym dniu
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.attendanceDate = :date AND a.status = 'present'")
    int countPresentEmployees(LocalDate date);

    // Pobiera liczbę użytkowników na urlopie w danym dniu
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.attendanceDate = :date AND a.status = 'leave'")
    int countEmployeesOnLeave(LocalDate date);

    // Pobiera listę użytkowników obecnych w danym dniu
    List<Attendance> findByAttendanceDateAndStatus(LocalDate date, String status);
}
