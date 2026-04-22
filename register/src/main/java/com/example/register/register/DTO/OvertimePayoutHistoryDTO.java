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
public class OvertimePayoutHistoryDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private int payoutMinutes;
    private LocalDate payoutDate;
    private String handledByFullName;
    private String note;
}
