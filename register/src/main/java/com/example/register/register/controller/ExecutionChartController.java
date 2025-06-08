package com.example.register.register.controller;

import com.example.register.register.model.User;
import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.ExecutionChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chart")
public class ExecutionChartController {

    @Autowired
    private ExecutionChartService chartService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/stacked-summary")
    public ResponseEntity<Map<String, Object>> getChartData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "sectionIds", required = false) List<Long> sectionIds,
            @RequestParam(value = "userIds", required = false) List<Long> userIds,
            @RequestParam(value = "processId", required = false) List<Long> processIds,
            Principal principal
    ) {
        User loggedUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Long teamId = loggedUser.getTeam().getId();

        Map<String, Object> chartData = chartService.buildChartData(date, teamId, sectionIds, userIds, processIds);
        return ResponseEntity.ok(chartData);
    }

}


