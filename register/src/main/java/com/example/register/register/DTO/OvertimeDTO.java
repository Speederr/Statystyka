package com.example.register.register.DTO;

import com.example.register.register.model.VolumeType;

public record OvertimeDTO(
        Long userId,
        String firstName,
        String lastName,
        VolumeType volumeType,
        long totalOvertime,
        long sectionId
) {}


