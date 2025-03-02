package com.example.register.register.service;

import com.example.register.register.model.Attendance;
import com.example.register.register.model.User;
import com.example.register.register.repository.AttendanceRepository;
import com.example.register.register.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    @Autowired
    public AttendanceService(AttendanceRepository attendanceRepository, UserRepository userRepository) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
    }

    public boolean hasAttendanceForToday(String username) {
        return attendanceRepository.countByUsernameAndDate(username, LocalDate.now()) > 0;
    }

    public void recordAttendance(String username, LocalDate date, String status) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie znaleziony: " + username));

        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setAttendanceDate(date);
        attendance.setStatus(status);

        attendanceRepository.save(attendance);
    }

    public int countPresentEmployees(LocalDate date) {
        return attendanceRepository.countPresentEmployees(date);
    }

    public int countEmployeesOnLeave(LocalDate date) {
        return attendanceRepository.countEmployeesOnLeave(date);
    }

    public List<Attendance> getPresentEmployees(LocalDate date) {
        return attendanceRepository.findByAttendanceDateAndStatus(date, "present");
    }

    public List<Attendance> getEmployeesOnLeave(LocalDate date) {
        return attendanceRepository.findByAttendanceDateAndStatus(date, "leave");
    }
}
