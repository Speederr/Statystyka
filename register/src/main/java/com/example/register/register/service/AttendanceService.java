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


    public void recordAttendance(String username, LocalDate date, String status, String workMode) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie znaleziony: " + username));

        Optional<Attendance> optional = attendanceRepository.findByUserAndAttendanceDate(user, date);

        Attendance attendance;
        if (optional.isPresent()) {
            attendance = optional.get(); // ✅ aktualizujemy istniejący rekord
        } else {
            attendance = new Attendance(user, date, status, workMode); // ⬅️ nowy jeśli nie ma
        }

        attendance.setStatus(status);
        attendance.setWorkMode(workMode);
        attendanceRepository.save(attendance);
    }


    public void recordAttendanceWithIp(String username, String clientIp) {
        String workMode = (clientIp.startsWith("192.168.*")) ? "office" : "homeoffice";
        recordAttendance(username, LocalDate.now(), "present", workMode);
    }

    public int countPresentEmployees(LocalDate date) {
        return attendanceRepository.countPresentEmployees(date);
    }

    public int countEmployeesOnLeave(LocalDate date) {
        return attendanceRepository.countEmployeesOnLeave(date);
    }

    public List<Attendance> getPresentEmployees(LocalDate date, Principal principal) {
        User manager = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Kierownik nie znaleziony."));

        Long teamId = manager.getTeam().getId();

        return attendanceRepository.findByAttendanceDateAndStatusAndUser_Team_Id(date, "present", teamId);
    }

    public List<Attendance> getEmployeesOnLeave(LocalDate date, Principal principal) {
        User manager = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Kierownik nie znaleziony"));

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

    // 🔄 Codzienne przypisanie "notloggedin" dla użytkowników bez wpisu
    @Scheduled(cron = "0 */3 * * * *") // co 15 minut
    public void autoMarkNotLoggedUsers() {
        markNotLoggedUsers(LocalDate.now());
    }
}
