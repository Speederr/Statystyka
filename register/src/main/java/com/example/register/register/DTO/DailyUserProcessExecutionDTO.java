package com.example.register.register.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DailyUserProcessExecutionDTO {
    private LocalDate date;
    private String fullName;
    private String processName;
    private Long quantity;
    private Double efficiency;
    private Long processId;

    public DailyUserProcessExecutionDTO(LocalDate date, String fullName, String processName, Long quantity, Double efficiency, Long processId) {
        this.date = date;
        this.fullName = fullName;
        this.processName = processName;
        this.quantity = quantity;
        this.efficiency = efficiency;
        this.processId = processId;
    }
}




