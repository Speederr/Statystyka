package com.example.register.register.controller;

import com.example.register.register.model.*;
import com.example.register.register.repository.ProcessRepository;
import com.example.register.register.repository.TeamRepository;
import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.AttendanceService;
import com.example.register.register.service.BacklogService;
import com.example.register.register.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class PageController {

    final private UserService userService;
    private final UserRepository userRepository;
    private final ProcessRepository processRepository;
    private final BacklogService backlogService;
    private final TeamRepository teamRepository;
    private final AttendanceService attendanceService;

    public PageController(UserService userService, UserRepository userRepository, ProcessRepository processRepository, BacklogService backlogService, TeamRepository teamRepository, AttendanceService attendanceService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.processRepository = processRepository;
        this.backlogService = backlogService;
        this.teamRepository = teamRepository;
        this.attendanceService = attendanceService;
    }

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Zwraca widok logowania (login.html)
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("users", new User());
        return "register";
    }

    @PostMapping("/register")
    public ResponseEntity<Void> createUser(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam Long id_role) {

        userService.createUser(firstName, lastName, username, email, id_role);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/login"))
                .build();
    }



    @GetMapping("/processes")
    public String showProcesses(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            Long userId = userRepository.findUserIdByUsername(username);

            if (userId != null) {
                model.addAttribute("userId", userId);
            } else {
                System.out.println("Użytkownik nie został znaleziony w bazie.");
                model.addAttribute("userId", "");
            }
        } else {
            model.addAttribute("userId", "");
        }
        return "processes";
    }


    @GetMapping("/adminPanel")
    public String showAdminPage(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "adminPanel";
    }

    @GetMapping("/firstLogin")
    public String showFirstLoginForm() {
        return "firstLogin";
    }

    @GetMapping("/restorePassword")
    public String showRestorePasswordPage(){
        return "restorePassword";
    }

    @GetMapping("/settings")
    public String showSettingsPage() {
        return "settings";
    }

    @GetMapping("/profile")
    public String showProfile() {
        return "profile";
    }

    @GetMapping("/averageTime")
    public String showAverageTime(Model model) {
        List<Team> teams = teamRepository.findAll();
        List<BusinessProcess> processes = processRepository.findAll();

        model.addAttribute("teams", teams);
        model.addAttribute("processes", processes);
        model.addAttribute("process", new BusinessProcess()); // ✅ Dodaj pusty obiekt
        return "averageTime";
    }


    @GetMapping("/notifications")
    public String getAllNotifications() {
        return "notifications";
    }

    @GetMapping("/backlogDetails")
    public String showBacklog(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model, Principal principal) {

        if(principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        Optional<User> optionalUser = userRepository.findByUsername(username);

        if(optionalUser.isEmpty()) {
            return "redirect:/error";
        }
        User user = optionalUser.get();
        if(user.getTeam() == null) {
            return "redirect:/error";
        }
        Long teamId = user.getTeam().getId();

        LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate) : LocalDate.now().minusDays(6);
        LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : LocalDate.now();

        List<Backlog> backlogData = backlogService.getBacklogBetweenDates(start, end)
                .stream()
                .filter(b -> b.getProcess().getTeam().getId().equals(teamId))
                .toList();

        // 🔹 Najpierw sortujemy po nazwie procesu
        List<BusinessProcess> sortedProcesses = backlogData.stream()
                .map(Backlog::getProcess)
                .distinct()
                .sorted(Comparator.comparing(BusinessProcess::getProcessName)) // Sortowanie alfabetyczne
                .toList();

        // 🔹 Tworzymy mapę posortowaną według nazw procesów
        Map<BusinessProcess, Map<LocalDate, Integer>> groupedBacklog = new LinkedHashMap<>();
        for (BusinessProcess process : sortedProcesses) {
            Map<LocalDate, Integer> processBacklog = backlogData.stream()
                    .filter(b -> b.getProcess().equals(process))
                    .collect(Collectors.toMap(
                            Backlog::getDate,
                            Backlog::getTaskCount,
                            (oldVal, newVal) -> newVal
                    ));
            groupedBacklog.put(process, processBacklog);
        }

        List<LocalDate> dateRange = start.datesUntil(end.plusDays(1)).collect(Collectors.toList());

        model.addAttribute("groupedBacklog", groupedBacklog);
        model.addAttribute("last7Days", dateRange);
        model.addAttribute("startDate", start.toString());
        model.addAttribute("endDate", end.toString());

        return "backlogDetails";
    }

    @GetMapping("/details")
    public String getUserDetails() {
        return "details";
    }

    @GetMapping("/attendance")
    public String showAttendance(Model model, Principal principal) {
        LocalDate today = LocalDate.now();

        List<Attendance> presentEmployees = attendanceService.getPresentEmployees(today, principal);
        List<Attendance> employeesOnLeave = attendanceService.getEmployeesOnLeave(today, principal);

        model.addAttribute("presentEmployees", presentEmployees);
        model.addAttribute("employeesOnLeave", employeesOnLeave);
        return "attendance";
    }

    @GetMapping("/executionReport")
    public String showExecutionReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model
    ) {
        LocalDate today = LocalDate.now();

        if (endDate == null) {
            endDate = today;
        }

        if (startDate == null) {
            startDate = endDate.minusDays(6); // ostatnie 7 dni
        }

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "executionReport";
    }

    @GetMapping("/check-connection")
    public ResponseEntity<String> checkConnection(HttpServletRequest request, Principal principal) {
        String clientIp = request.getRemoteAddr();
        String username = principal.getName();

        attendanceService.recordAttendanceWithIp(username, clientIp);

        String workMode = (clientIp.startsWith("192.168.10.")) ? "office" : "homeoffice";
        return ResponseEntity.ok(workMode);
    }

    @GetMapping("/userDetails/{userId}")
    public String getOneUserDetails(@PathVariable Long userId, Model model) {

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            model.addAttribute("user", userOpt.get());
            return "userDetails";
        } else {
            return "redirect:/error"; // lub własna strona błędu
        }
    }





}
