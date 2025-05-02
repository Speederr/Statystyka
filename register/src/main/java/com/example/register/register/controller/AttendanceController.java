package com.example.register.register.controller;

import com.example.register.register.model.Attendance;
import com.example.register.register.model.User;
import com.example.register.register.repository.AttendanceRepository;
import com.example.register.register.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class AttendanceController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @PostMapping("/api/attendance/update")
    public ResponseEntity<String> updateAttendance(@RequestParam Long userId, @RequestParam String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Attendance attendance = attendanceRepository.findByUserAndAttendanceDate(user, LocalDate.now())
                .orElseGet(() -> {
                    Attendance newAttendance = new Attendance();
                    newAttendance.setUser(user);
                    newAttendance.setAttendanceDate(LocalDate.now());
                    newAttendance.setWorkMode(null);
                    newAttendance.setStatus(status);
                    return newAttendance;
                });

        // Jeśli rekord już istniał – zaktualizuj tylko status
        if (attendance.getId() != null) {
            attendance.setStatus(status); // 🔹 Zmieniamy tylko status
        }

        attendanceRepository.save(attendance);

        return ResponseEntity.ok("Status zaktualizowany!");
    }


}
