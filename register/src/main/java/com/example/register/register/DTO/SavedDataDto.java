package com.example.register.register.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class SavedDataDto {

    private String process;
    private Long quantity;
    private LocalDate date;
    private String employee;


}
