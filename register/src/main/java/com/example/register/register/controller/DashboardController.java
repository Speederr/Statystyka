package com.example.register.register.controller;

import org.springframework.stereotype.Controller;
import com.example.register.register.model.Attendance;
import com.example.register.register.model.User;
import com.example.register.register.service.AttendanceService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;


@Controller
public class DashboardController {

    private final AttendanceService attendanceService;

    public DashboardController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }
    @GetMapping("/efficiency")
    public String showEfficiency(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        LocalDate today = LocalDate.now();
        List<Attendance> presentAttendance = attendanceService.getPresentEmployees(today, principal);
        List<Attendance> leaveAttendance = attendanceService.getEmployeesOnLeave(today, principal);

        List<User> presentEmployees = presentAttendance.stream()
                .map(Attendance::getUser)
                .toList();
        List<User> onLeaveEmployees = leaveAttendance.stream()
                .map(Attendance::getUser)
                .toList();

        model.addAttribute("presentEmployees", presentEmployees);
        model.addAttribute("onLeaveEmployees", onLeaveEmployees);
        model.addAttribute("presentCount", presentEmployees.size());
        model.addAttribute("onLeaveCount", onLeaveEmployees.size());

        return "efficiency";
    }

}


