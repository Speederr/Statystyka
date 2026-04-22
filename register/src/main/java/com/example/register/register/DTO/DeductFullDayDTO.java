package com.example.register.register.DTO;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public record DeductFullDayDTO(
        Long userId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
) {}
