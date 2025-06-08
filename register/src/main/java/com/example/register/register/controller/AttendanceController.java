package com.example.register.register.controller;

import com.example.register.register.model.Attendance;
import com.example.register.register.model.User;
import com.example.register.register.repository.AttendanceRepository;
import com.example.register.register.repository.UserRepository;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendance/")
public class AttendanceController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @PostMapping("/update")
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

@GetMapping("/workmode/summary")
public ResponseEntity<Map<String, Integer>> getWorkModeSummary(@RequestParam(required = false) String sectionId, Principal principal) {
    List<User> users;

    if (sectionId != null && !"all".equalsIgnoreCase(sectionId)) {
        Long id;
        try {
            id = Long.parseLong(sectionId);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
        users = userRepository.findBySection_Id(id);
    } else {
        // domyślnie: użytkownicy z zespołu zalogowanego użytkownika
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        users = userRepository.findByTeam_Id(currentUser.getTeam().getId());
    }

    int total = users.size();
    int office = 0;
    int homeoffice = 0;
    LocalDate today = LocalDate.now();

    for (User user : users) {
        Optional<Attendance> attendanceOpt = attendanceRepository.findByUserAndAttendanceDate(user, today);
        if (attendanceOpt.isPresent()) {
            String mode = attendanceOpt.get().getWorkMode();
            if ("office".equalsIgnoreCase(mode)) {
                office++;
            } else if ("homeoffice".equalsIgnoreCase(mode)) {
                homeoffice++;
            }
        }
    }


    Map<String, Integer> result = Map.of(
            "total", total,
            "office", office,
            "homeoffice", homeoffice
    );

    return ResponseEntity.ok(result);
}





}
