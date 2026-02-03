package com.example.register.register.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class BacklogProcessWithHoursDto {
    private String processName;
    private int taskCount;
    private double hours;
}

