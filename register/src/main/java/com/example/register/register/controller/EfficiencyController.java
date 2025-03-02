package com.example.register.register.controller;

import com.example.register.register.service.EfficiencyService;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/efficiency")
public class EfficiencyController {

    private final EfficiencyService efficiencyService;

    public EfficiencyController(EfficiencyService efficiencyService) {
        this.efficiencyService = efficiencyService;
    }

    @PostMapping("/calculate/{userId}")
    public ResponseEntity<String> calculateEfficiency(@PathVariable Long userId, @RequestBody Map<Long, Integer> processVolumes) {
        efficiencyService.calculateAndSaveEfficiency(userId, processVolumes);
        return ResponseEntity.ok("Efficiency calculated and saved for user " + userId);
    }




}
