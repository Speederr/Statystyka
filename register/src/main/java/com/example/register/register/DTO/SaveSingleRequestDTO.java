package com.example.register.register.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SaveSingleRequestDTO {

    private Long userId;
    private Long processId;
    private Integer level;
}
