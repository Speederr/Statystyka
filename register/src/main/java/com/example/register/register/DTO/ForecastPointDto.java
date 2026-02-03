package com.example.register.register.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ForecastPointDto {
    private LocalDate date;
    private double backlogHours;
}