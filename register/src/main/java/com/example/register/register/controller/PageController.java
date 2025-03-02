package com.example.register.register.controller;

import com.example.register.register.model.BusinessProcess;
import com.example.register.register.model.Team;
import com.example.register.register.model.User;
import com.example.register.register.repository.ProcessRepository;
import com.example.register.register.repository.TeamRepository;
import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/")
public class PageController {

    @Autowired
    final private UserService userService;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final ProcessRepository processRepository;


    public PageController(UserService userService, UserRepository userRepository, ProcessRepository processRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.processRepository = processRepository;
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
                @RequestParam Long id_role,
                @RequestParam Long teamId,
                @RequestParam Long sectionId) {

            userService.createUser(firstName, lastName, username, email, id_role, teamId, sectionId);

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
    public String showSettingsPage(Model model) {
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
        List<BusinessProcess> processes = processRepository.findAll();
        model.addAttribute("processes", processes);
        model.addAttribute("process", new BusinessProcess()); // ✅ Dodaj pusty obiekt
        return "averageTime";
    }

    @GetMapping("/notifications")
    public String getAllNotifications() {
        return "notifications";
    }

}
