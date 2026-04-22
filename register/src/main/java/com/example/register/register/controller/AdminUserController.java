package com.example.register.register.controller;

import com.example.register.register.model.UserImportResult;
import com.example.register.register.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/admin/users/import")
    public String importUsers(@RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        try {
            UserImportResult result = userService.importUsersFromFile(file);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Import zakończony. Dodano użytkowników: " + result.getSuccessCount()
            );

            if (!result.getErrors().isEmpty()) {
                redirectAttributes.addFlashAttribute("importErrors", result.getErrors());
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Błąd importu: " + e.getMessage());
        }

        return "redirect:/adminPanel";
    }
}
