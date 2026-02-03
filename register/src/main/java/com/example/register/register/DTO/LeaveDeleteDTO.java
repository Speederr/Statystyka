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
public class LeaveDeleteDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long userId;

}
