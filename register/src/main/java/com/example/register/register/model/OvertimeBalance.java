package com.example.register.register.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "overtime_balance")
public class OvertimeBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "volume_type", nullable = false)
    private VolumeType volumeType;

    @Column(name = "overtime_minutes", nullable = false )
    private Integer overtimeMinutes = 0;

    @Column(name = "balance_date", nullable = false)
    private LocalDate balanceDate;  // <---- DODANA DATA

}
