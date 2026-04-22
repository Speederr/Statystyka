package com.example.register.register.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserTableDto {

    private Long id;
    private String firstName;
    private String lastName;
    private Double efficiency;
    private Double nonOperational;
    private Long positionId;
    private String positionName;
    private String attendanceStatus;

}
