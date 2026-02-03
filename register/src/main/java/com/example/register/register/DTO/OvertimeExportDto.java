package com.example.register.register.DTO;

public record OvertimeExportDto(
        String firstName,
        String lastName,
        long overtimePaid,
        long overtimeOff
) {}

