package com.example.register.register.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

public record SavedDataDto(
        String processName,
        long quantity,
        LocalDate todaysDate,
        String username,
        String volumeType,
        int overtimeMinutes
) {}
