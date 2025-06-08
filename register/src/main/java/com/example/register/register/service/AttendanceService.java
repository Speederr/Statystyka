package com.example.register.register.service;

import com.example.register.register.model.Attendance;
import com.example.register.register.model.User;
import com.example.register.register.repository.AttendanceRepository;
import com.example.register.register.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private UserRepository userRepository;


    public List<Attendance> getPresentEmployees(LocalDate date, Principal principal) {
        User manager = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found."));

        Long teamId = manager.getTeam().getId();

        return attendanceRepository.findByAttendanceDateAndStatusAndUser_Team_Id(date, "present", teamId);
    }

    public List<Attendance> getEmployeesOnLeave(LocalDate date, Principal principal) {
        User manager = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found."));

        Long teamId = manager.getTeam().getId();

        return attendanceRepository.findByAttendanceDateAndStatusAndUser_Team_Id(date, "leave", teamId);
    }

    public void markNotLoggedUsers(LocalDate date) {
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            boolean hasAttendance = attendanceRepository.findByUserAndAttendanceDate(user, date).isPresent();

            if (!hasAttendance) {
                Attendance attendance = new Attendance();
                attendance.setUser(user);
                attendance.setAttendanceDate(date);
                attendance.setStatus("notloggedin");
                attendance.setWorkMode(null);
                attendanceRepository.save(attendance);
            }
        }
    }

    public void recordAttendanceAfterLogin(String username, String clientIp) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie znaleziony: " + username));

        LocalDate today = LocalDate.now();
        Optional<Attendance> optional = attendanceRepository.findByUserAndAttendanceDate(user, today);

        String workMode = clientIp.startsWith("192.168.") ? "office" : "homeoffice";

        if (optional.isPresent()) {
            Attendance attendance = optional.get();

            // jeśli był oznaczony jako "notloggedin", zaktualizuj na "present"
            if ("notloggedin".equalsIgnoreCase(attendance.getStatus())) {
                attendance.setStatus("present");
                attendance.setWorkMode(workMode);
                attendanceRepository.save(attendance);
            }

        } else {
            // jeśli nie było wpisu, dodaj nowy
            Attendance attendance = new Attendance();
            attendance.setUser(user);
            attendance.setAttendanceDate(today);
            attendance.setStatus("present");
            attendance.setWorkMode(workMode);
            attendanceRepository.save(attendance);
        }
    }


    // 🔄 Codzienne przypisanie "notloggedin" dla użytkowników bez wpisu
    @Scheduled(cron = "0 */3 * * * *") // co 3 minuty
    public void autoMarkNotLoggedUsers() {
        markNotLoggedUsers(LocalDate.now());
    }
}
