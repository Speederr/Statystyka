package com.example.register.register.DTO;

import com.example.register.register.model.VolumeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EfficiencyRequestDTO {
    private Map<Long, Integer> processVolumes;
    private VolumeType volumeType;
    private int overtimeMinutes;
}
