package com.example.register.register.controller;

import com.example.register.register.model.FormUsers;
import com.example.register.register.model.User;
import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/")
public class PageController {

    @Autowired
    final private UserService userService;

    @Autowired
    private UserRepository userRepository;

    public PageController(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Zwraca widok logowania (login.html)
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        // Add an empty User object to the model to bind the form
        model.addAttribute("users", new User());
        return "register";  // Thymeleaf template name (register.html)
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("users") User user) {
        // Encrypt the password and save the user (same logic as before)
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return "redirect:/login";  // Redirect after successful registration
    }


    @GetMapping("/settings")
    public String showSettingsPage(Model model) {
        List<FormUsers> users = userService.getAllUsersWithRoles();
        model.addAttribute("form_users", users);
        return "settings";
    }

    @GetMapping("/index")
    public String showWelcomePage() {
        return "index"; // Zwraca widok strony głównej (index.html)
    }
    @GetMapping("/firstLogin")
    public String showFirstLoginForm() {
        return "firstLogin";
    }

    @GetMapping("/efficiency")
    public String showEfficiencyPage() {
        return "efficiency";
    }

}
