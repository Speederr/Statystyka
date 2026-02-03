package com.example.register.register.service;

import com.example.register.register.DTO.LeaveEventDTO;
import com.example.register.register.model.Attendance;
import com.example.register.register.model.User;
import com.example.register.register.repository.AttendanceRepository;
import com.example.register.register.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

        System.out.println("clientIp: " + clientIp);

        clientIp = clientIp.trim(); // 🧼 usuń białe znaki

        String workMode = (
                clientIp.startsWith("192.168.") ||
                        clientIp.equals("127.0.0.1") ||
                        clientIp.equals("0:0:0:0:0:0:0:1") ||
                        clientIp.equals("::1")
        ) ? "homeoffice" : "office";



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

public List<LeaveEventDTO> getAllLeaves(Principal principal) {
    User user = userRepository.findByUsername(principal.getName())
            .orElseThrow(() -> new RuntimeException("User not found."));

    Long teamId = user.getTeam().getId();
    List<Attendance> leaves = attendanceRepository.findByStatusAndUser_Team_Id("leave", teamId);

    // Grupuj po użytkowniku i sortuj po dacie
    return leaves.stream()
            .collect(Collectors.groupingBy(Attendance::getUser))
            .values().stream()
            .flatMap(userLeaves -> {
                // Sortuj daty dla użytkownika
                List<Attendance> sorted = userLeaves.stream()
                        .sorted(Comparator.comparing(Attendance::getAttendanceDate))
                        .toList();

                List<LeaveEventDTO> result = new ArrayList<>();
                int i = 0;
                while (i < sorted.size()) {
                    LocalDate start = sorted.get(i).getAttendanceDate();
                    LocalDate end = start;
                    // Rozszerz zakres póki kolejne dni są ciągłe
                    while (i + 1 < sorted.size() &&
                            !sorted.get(i + 1).getAttendanceDate().isAfter(end.plusDays(1))) {
                        end = sorted.get(++i).getAttendanceDate();
                    }
                    User leaveUser = sorted.get(i).getUser();
                    result.add(new LeaveEventDTO(
                            sorted.get(i).getUser().getFirstName() + " " +
                                    sorted.get(i).getUser().getLastName() + " - Urlop",
                            start,
                            end.plusDays(1),
                            leaveUser.getId()
                    ));
                    i++;
                }
                return result.stream();
            })
            .toList();
}







    // 🔄 Codzienne przypisanie "notloggedin" dla użytkowników bez wpisu
    @Scheduled(cron = "0 */3 * * * *") // co 3 minuty
    public void autoMarkNotLoggedUsers() {
        markNotLoggedUsers(LocalDate.now());
    }
}
