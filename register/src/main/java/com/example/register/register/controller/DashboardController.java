package com.example.register.register.controller;

import com.example.register.register.model.Attendance;
import com.example.register.register.model.User;
import com.example.register.register.service.AttendanceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    private final AttendanceService attendanceService;

    public DashboardController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/efficiency")
    public String showEfficiency(Model model) {
        LocalDate today = LocalDate.now(); // Pobranie dzisiejszej daty

        // Pobranie list użytkowników obecnych i na urlopie z AttendanceService
        List<Attendance> presentAttendance = attendanceService.getPresentEmployees(today);
        List<Attendance> leaveAttendance = attendanceService.getEmployeesOnLeave(today);

        // Konwersja Attendance → User (aby Thymeleaf miał listę użytkowników)
        List<User> presentEmployees = presentAttendance.stream()
                .map(Attendance::getUser)
                .toList();
        List<User> onLeaveEmployees = leaveAttendance.stream()
                .map(Attendance::getUser)
                .toList();

        // Przekazanie danych do modelu
        model.addAttribute("presentEmployees", presentEmployees);
        model.addAttribute("onLeaveEmployees", onLeaveEmployees);
        model.addAttribute("presentCount", presentEmployees.size());
        model.addAttribute("onLeaveCount", onLeaveEmployees.size());

        return "efficiency"; // Nazwa szablonu Thymeleaf
    }
}
