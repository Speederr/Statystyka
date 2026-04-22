package com.example.register.register.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ForecastResponseDto {
    private Meta meta;
    private List<ForecastPointDto> values;

    @Data
    @AllArgsConstructor
    public static class Meta {
        private double avgEfficiency;
        private double startBacklogHours;
        private double forecastedImpact;
    }
}