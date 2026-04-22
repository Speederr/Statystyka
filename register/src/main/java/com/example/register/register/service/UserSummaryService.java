package com.example.register.register.service;

import com.example.register.register.DTO.UserSummaryDTO;
import com.example.register.register.repository.EfficiencyRepository;
import com.example.register.register.repository.SavedDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserSummaryService {

    @Autowired
    private EfficiencyRepository efficiencyRepository;

    @Autowired
    private SavedDataRepository savedDataRepository;

    public UserSummaryDTO getUsersSummary(Long userId) {
        Double averageEfficiency = efficiencyRepository.findAverageEfficiencyByUserId(userId);
        Double totalNonOperationalHours = savedDataRepository.sumNonOperationalHoursByUserId(userId);

        return new UserSummaryDTO(
                averageEfficiency != null ? Math.round(averageEfficiency * 100) / 100.0 : 0.0,
                totalNonOperationalHours != null ? Math.round(totalNonOperationalHours * 100.0) / 100.0 : 0.0
        );
    }
}
