package com.example.register.register.DTO;


import java.time.LocalDate;

public record MixedChartDTO(LocalDate date, String processName, Long quantity, Double efficiency) {

}
