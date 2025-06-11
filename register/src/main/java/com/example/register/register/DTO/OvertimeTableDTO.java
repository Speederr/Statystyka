package com.example.register.register.DTO;

public record OvertimeTableDTO(
        Long userId,
        String firstName,
        String lastName,
        Long paid,
        Long off,
        Long deducted,
        Long sectionId
) {}
