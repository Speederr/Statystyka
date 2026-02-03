package com.example.register.register.service;

import com.example.register.register.DTO.ForecastPointDto;

import com.example.register.register.DTO.ForecastResponseDto;
import com.example.register.register.repository.BacklogRepository;
import com.example.register.register.repository.EfficiencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ForecastService {

    private final EfficiencyRepository efficiencyRepository;
    private final BacklogRepository backlogRepository;

    public ForecastResponseDto forecastTeamBacklogMinutes(
            Long teamId, int workers, int days, LocalDate startDate, int historyDays, double minutesPerWorker, double forecastedImpact
    ) {
        // Średnia efektywność zespołu (%)
        LocalDate fromDate = LocalDate.now().minusDays(historyDays);
        Double avgEff = efficiencyRepository.findTeamAverageEfficiency(teamId, fromDate);
        double avgEfficiency = avgEff != null ? avgEff : 100.0;

        // Startowy backlog (w godzinach)
        Double backlogStart = backlogRepository.findLatestTeamBacklogHours(teamId);
        double backlogHours = backlogStart != null ? backlogStart : 0.0;

        backlogHours += forecastedImpact;

        // Dzienny przerób zespołu (w godzinach)
        double dailyThroughput = workers * minutesPerWorker * (avgEfficiency / 100.0) / 60.0; // [minuty] -> [godziny]

        List<ForecastPointDto> points = new ArrayList<>();
        points.add(new ForecastPointDto(startDate, backlogHours)); // punkt początkowy

        for (int i = 1; i <= days && backlogHours > 0; i++) {
            backlogHours = Math.max(0, backlogHours - dailyThroughput);
            points.add(new ForecastPointDto(startDate.plusDays(i), backlogHours));
        }

        double safeBacklogStart = backlogStart != null ? backlogStart : 0.0;

        return new ForecastResponseDto(
                new ForecastResponseDto.Meta(avgEfficiency, safeBacklogStart, forecastedImpact),
                points
        );
    }

}


