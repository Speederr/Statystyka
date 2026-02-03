package com.example.register.register.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveSingleSoftSkillRequestDTO {
    private Long userId;
    private Long skillId;
    private Integer level;
}
