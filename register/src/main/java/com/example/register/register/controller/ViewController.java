package com.example.register.register.controller;

import com.example.register.register.model.BusinessProcess;
import com.example.register.register.model.User;
import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class ViewController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProcessService processService;

    @GetMapping("/index")
    public String index(Model model, Principal principal) {
        // Pobranie aktualnie zalogowanego użytkownika
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Pobranie ulubionych procesów dla użytkownika
        List<BusinessProcess> favoriteProcesses = processService.getFavoriteProcesses(user.getId());

        // Przekazanie userId i ulubionych procesów do widoku
        model.addAttribute("userId", user.getId());
//        model.addAttribute("user", user); // <--- dodaj cały obiekt User
        model.addAttribute("favoriteProcesses", favoriteProcesses);

        return "index"; // Nazwa pliku Thymeleaf (index.html)
    }






}
