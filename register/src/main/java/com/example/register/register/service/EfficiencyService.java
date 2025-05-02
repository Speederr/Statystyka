package com.example.register.register.service;

import com.example.register.register.model.BusinessProcess;
import com.example.register.register.model.Efficiency;
import com.example.register.register.model.User;
import com.example.register.register.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EfficiencyService {

    private final ProcessRepository processRepository;
    private final EfficiencyRepository efficiencyRepository;
    private final UserRepository userRepository;

    public EfficiencyService(ProcessRepository processRepository, EfficiencyRepository efficiencyRepository,
                             UserRepository userRepository) {
        this.processRepository = processRepository;
        this.efficiencyRepository = efficiencyRepository;
        this.userRepository = userRepository;
    }

    public void calculateAndSaveEfficiency(Long userId, Map<Long, Integer> processVolumes) {
        // 🔹 Pobieramy tylko procesy operacyjne
        double totalOperationalTime = processRepository.findAll().stream()
                .filter(process -> !process.isNonOperational()) // ✅ Tylko procesy operacyjne
                .mapToDouble(process -> processVolumes.getOrDefault(process.getId(), 0) * process.getAverageTime())
                .sum();

        // 🔹 Pobieramy czas nieoperacyjny (z `BusinessProcess`)
        double totalNonOperationalTime = processRepository.findAll().stream()
                .filter(BusinessProcess::isNonOperational) // ✅ Tylko nieoperacyjne
                .mapToDouble(process -> processVolumes.getOrDefault(process.getId(), 0) * process.getAverageTime())
                .sum();

        // 🔹 Ustalamy czas operacyjny (uwzględniając nieoperacyjne zadania)
        double operationalTime = Math.max(465 - totalNonOperationalTime, 1);

        // 🔹 Obliczamy efektywność
        double totalEfficiency = totalOperationalTime / operationalTime * 100;
        totalEfficiency = Math.round(totalEfficiency * 100.0) / 100.0;

        // 🔹 Zapisujemy do bazy danych
        Efficiency efficiency = new Efficiency();
        efficiency.setUser(userRepository.findById(userId).orElseThrow());
        efficiency.setEfficiency(totalEfficiency);
        efficiencyRepository.save(efficiency);
    }
}
