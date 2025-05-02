package com.example.register.register.controller;

import com.example.register.register.model.Efficiency;
import com.example.register.register.model.SavedData;
import com.example.register.register.model.User;
import com.example.register.register.repository.*;
import com.example.register.register.service.EfficiencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/efficiency/")
public class EfficiencyController {

    @Autowired
    private EfficiencyRepository efficiencyRepository;

    @Autowired
    private EfficiencyService efficiencyService;

    @Autowired
    private SavedDataRepository savedDataRepository;

    @Autowired
    private UserRepository userRepository;


    @PostMapping("/calculate/{userId}")
    public ResponseEntity<String> calculateEfficiency(@PathVariable Long userId, @RequestBody Map<Long, Integer> processVolumes) {
        efficiencyService.calculateAndSaveEfficiency(userId, processVolumes);
        return ResponseEntity.ok("Efficiency calculated and saved for user " + userId);
    }

    @GetMapping("/average/section")
    public ResponseEntity<Map<String, Double>> showSectionEfficiency(@RequestParam String sectionId) {
        LocalDate today = LocalDate.now();
        List<Efficiency> efficiencies;

        if ("all".equalsIgnoreCase(sectionId)) {
            efficiencies = efficiencyRepository.findAllByDate(today);
        } else {
            Long id;
            try {
                id = Long.parseLong(sectionId);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of("averageSectionEfficiency", 0.0));
            }

            efficiencies = efficiencyRepository.findAllBySectionIdAndDate(id, today);
        }

        double averageEfficiencyForSection = efficiencies.stream()
                .mapToDouble(Efficiency::getEfficiency)
                .average()
                .orElse(0.0);

        Map<String, Double> result = Map.of("averageSectionEfficiency", averageEfficiencyForSection);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/section/non-operational")
    public ResponseEntity<Map<String, Double>> showNonOperationalTime(@RequestParam String sectionId) {
        LocalDate today = LocalDate.now();

        List<User> userInSection;

        if ("all".equalsIgnoreCase(sectionId)) {
            userInSection = userRepository.findAll();
        } else {
            Long id;
            try {
                id = Long.parseLong(sectionId);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().build(); // Zwróć 400 jeśli sectionId nieprawidłowe
            }
            userInSection = userRepository.findBySection_Id(id);
        }


        // ⏱️ Policz całkowity czas nieoperacyjny (w minutach) dla wszystkich pracowników
        double totalMinutes = userInSection.stream()
                .flatMap(user -> savedDataRepository.findNonOperationalSavedDataByUserIdAndDate(user.getId(), today).stream())
                .mapToDouble(sd -> sd.getProcess().getAverageTime() * sd.getQuantity())
                .sum();

        // 🧮 Średni czas na jednego użytkownika
        double avgPerUserMinutes = userInSection.isEmpty() ? 0.0 : totalMinutes / userInSection.size();
        double avgPerUserHours = Math.round((avgPerUserMinutes / 60.0) * 100.0) / 100.0;

        Map<String, Double> result = Map.of("averageNonOperationalTime", avgPerUserHours);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/weekly")
    public ResponseEntity<List<Map<String, Object>>> getWeeklyEfficiencyAndNonOperational(
            @RequestParam(required = false) String sectionId) {

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6); // Ostatnie 7 dni

        List<User> userInSection;
        if ("all".equalsIgnoreCase(sectionId) || sectionId == null) {
            userInSection = userRepository.findAll();
        } else {
            Long id;
            try {
                id = Long.parseLong(sectionId);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().build();
            }
            userInSection = userRepository.findBySection_Id(id);
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);

            List<Efficiency> efficiencies = userInSection.stream()
                    .flatMap(user -> efficiencyRepository.findAllByUserAndTodaysDate(user, date).stream())
                    .toList();

            double avgEfficiency = efficiencies.stream()
                    .mapToDouble(Efficiency::getEfficiency)
                    .average().orElse(0.0);

            double totalMinutes = userInSection.stream()
                    .flatMap(user -> savedDataRepository.findNonOperationalSavedDataByUserIdAndDate(user.getId(), date).stream())
                    .mapToDouble(sd -> sd.getProcess().getAverageTime() * sd.getQuantity())
                    .sum();

            double avgNonOperational = userInSection.isEmpty() ? 0.0 : totalMinutes / userInSection.size();
            double avgNonOperationalHours = Math.round((avgNonOperational / 60.0) * 100.0) / 100.0;

            Map<String, Object> dayData = Map.of(
                    "date", date.toString(),
                    "efficiency", avgEfficiency,
                    "nonOperational", avgNonOperationalHours
            );
            result.add(dayData);
        }

        return ResponseEntity.ok(result);
    }

}
