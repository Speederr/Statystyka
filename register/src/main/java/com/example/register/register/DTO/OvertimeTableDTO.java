package com.example.register.register.DTO;

public record OvertimeTableDTO(
        Long userId,
        String firstName,
        String lastName,
        Long overtimePaid,
        Long overtimeOff,
        Long deducted,
        Long sectionId
) {}
