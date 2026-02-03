package com.example.register.register.DTO;

import com.example.register.register.model.VolumeType;

import java.time.LocalDate;

public record OvertimeDetailDTO(
        String processName,
        Long quantity,
        LocalDate date,
        String userFullName,
        VolumeType volumeType,
        Integer overtimeMinutes
) {}
