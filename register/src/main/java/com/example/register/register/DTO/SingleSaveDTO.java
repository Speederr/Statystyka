package com.example.register.register.DTO;

import com.example.register.register.model.VolumeType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SingleSaveDTO {

    private Long userId;
    private Long processId;
    private Long quantity;
    private VolumeType volumeType;
    private Integer overtimeMinutes;

    @JsonProperty("todaysDate")
    private LocalDate todaysDate;
}
