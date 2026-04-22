package com.example.register.register.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.stereotype.Controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/error")
public class CustomErrorController implements ErrorController {

    @GetMapping
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleError(HttpServletRequest request, Model model,
                              @RequestParam(value = "message", required = false) String errorMessage) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            model.addAttribute("errorCode", statusCode);

            if (errorMessage != null) {
                model.addAttribute("errorMessage", errorMessage); // ✅ Pobranie parametru z URL
            } else if (statusCode == HttpStatus.BAD_REQUEST.value()) {
                model.addAttribute("errorCode", "400");
                model.addAttribute("errorMessage", "Nieprawidłowe żądanie. Sprawdź poprawność wprowadzonych danych.");
            } else if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("errorCode", "404");
                model.addAttribute("errorMessage", "Strona, której szukasz, nie została znaleziona.");
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("errorCode", "403");
                model.addAttribute("errorMessage", "Nie masz uprawnień do tej strony.");
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("errorCode", "500");
                model.addAttribute("errorMessage", "Wystąpił błąd serwera. Spróbuj ponownie później.");
            } else {
                model.addAttribute("errorMessage", "Wystąpił niespodziewany błąd.");
            }
        } else {
            model.addAttribute("errorCode", "Błąd");
            model.addAttribute("errorMessage", "Nieznany błąd.");
        }

        return "error"; // Plik Thymeleaf: error.html
    }

}
