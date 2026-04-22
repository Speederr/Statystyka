package com.example.register.register.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeaveUpdateDTO {
    private LocalDate oldStartDate;
    private LocalDate oldEndDate;
    private LocalDate newStartDate;
    private LocalDate newEndDate;
}
