package com.example.register.register.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProcessLevelDTO {
    private Long processId;
    private Integer level;
}