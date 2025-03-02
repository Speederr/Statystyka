package com.example.register.register.service;

import com.example.register.register.model.BusinessProcess;
import com.example.register.register.model.Efficiency;
import com.example.register.register.repository.EfficiencyRepository;
import com.example.register.register.repository.ProcessRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EfficiencyService {

    private final ProcessRepository processRepository;
    private final EfficiencyRepository efficiencyRepository;

    public EfficiencyService(ProcessRepository processRepository, EfficiencyRepository efficiencyRepository) {
        this.processRepository = processRepository;
        this.efficiencyRepository = efficiencyRepository;
    }

    public void calculateAndSaveEfficiency(Long userId, Map<Long, Integer> processVolumes) {

        List<BusinessProcess> processes = processRepository.findAll();

        double totalEfficiency = Math.round(
                processes.stream()
                        .mapToDouble(process -> processVolumes.getOrDefault(process.getId(), 0) * process.getAverageTime())
                        .sum() / 465 * 100 * 100.0
        ) / 100.0;



        Efficiency efficiency = new Efficiency();
        efficiency.setUser_id(userId);
        efficiency.setEfficiency(totalEfficiency);

        efficiencyRepository.save(efficiency);


    }
}
