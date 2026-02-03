package com.example.register.register.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveSingleAppRequestDTO {

    private Long userId;
    private Long applicationId;
    private Integer level;
}
