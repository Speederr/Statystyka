package com.example.register.register.controller;

import com.example.register.register.model.*;
import com.example.register.register.repository.ProcessRepository;
import com.example.register.register.repository.TeamRepository;
import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.AttendanceService;
import com.example.register.register.service.BacklogService;
import com.example.register.register.service.ProcessService;
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
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequestMapping("/")
public class PageController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProcessRepository processRepository;
    @Autowired
    private BacklogService backlogService;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private ProcessService processService;

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
                log.info("Użytkownik nie został znaleziony w bazie.");
                model.addAttribute("userId", "");
            }
        } else {
            model.addAttribute("userId", "");
        }
        return "processes";
    }

    @GetMapping("/adminPanel")
    public String showAdminPage(Model model, Principal principal) {
        String username = principal.getName();
        User loggedUser = userService.findByUsername(username);

        Team team = loggedUser.getTeam();
        List<User> usersFromTeam = userService.findByTeam(team);

        List<Team> teams = teamRepository.findAll();

        model.addAttribute("users", usersFromTeam);
        model.addAttribute("teams", teams);
        model.addAttribute("loggedUser", loggedUser);


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
public String showProcessTimes(Model model, Principal principal) {
    User loggedUser = userService.findByUsername(principal.getName());

    List<BusinessProcess> processes;
    if (loggedUser.getRole().getRoleName().equalsIgnoreCase("Manager") ||
            loggedUser.getRole().getRoleName().equalsIgnoreCase("Coordinator")) {

        processes = processService.getProcessesByTeamId(loggedUser.getTeam().getId());

        // 👇 tylko zespół zalogowanego użytkownika
        model.addAttribute("teams", List.of(loggedUser.getTeam()));
        model.addAttribute("isRestricted", true);

    } else {
        processes = processService.getAllProcesses();
        model.addAttribute("teams", teamRepository.findAll());
        model.addAttribute("isRestricted", false);
    }

    model.addAttribute("userId", loggedUser.getId());
    model.addAttribute("loggedUser", loggedUser);
    model.addAttribute("processes", processes);
    model.addAttribute("process", new BusinessProcess());

    return "averageTime";
}



    @GetMapping("/notifications")
    public String getAllNotifications() {
        return "notifications";
    }

    @SuppressWarnings("unused")
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

        attendanceService.recordAttendanceAfterLogin(username, clientIp);

        String workMode = (clientIp.startsWith("192.168.")) ? "office" : "homeoffice";
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

    @GetMapping("/chartDetails")
    public String getChartDetails() {
        return "chartDetails";
    }


    @GetMapping("/overtimeReport")
    public String getOvertimeDetails() {
        return "overtimeReport";
    }

    @GetMapping("/userOvertimeDetails/overtime/{userId}")
    public String showOvertimeDetailsPage(@PathVariable Long userId, Model model) {
        model.addAttribute("userId", userId);
        return "UserOvertimeDetails";
    }

}
