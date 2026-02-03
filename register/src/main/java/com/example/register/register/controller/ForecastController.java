package com.example.register.register.controller;

import com.example.register.register.DTO.ForecastResponseDto;
import com.example.register.register.model.User;
import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.ForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/forecast")
@RequiredArgsConstructor
public class ForecastController {
    private final ForecastService forecastService;
    private final UserRepository userRepository;

    @GetMapping
    public ForecastResponseDto forecastForTeam(
            Principal principal,
            @RequestParam int workers,
            @RequestParam int days,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(defaultValue = "7") int historyDays,
            @RequestParam(defaultValue = "435") double minutesPerWorker, // <-- zmienne minuty na osobę/dzień
            @RequestParam(defaultValue = "0") double forecastedImpact
    ) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika: " + username));
        Long teamId = user.getTeam().getId();

        if (startDate == null) {
            startDate = LocalDate.now();
        }

        return forecastService.forecastTeamBacklogMinutes(teamId, workers, days, startDate, historyDays, minutesPerWorker, forecastedImpact);
    }

}
