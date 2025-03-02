package com.example.register.register.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "efficiency")
public class Efficiency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id",  nullable = false)
    private Long user_id;

    @Column(name = "efficency",  nullable = false)
    private Double efficiency;

    @CreationTimestamp // Automatycznie ustawia bieżącą datę
    @Temporal(TemporalType.DATE)
    @Column(name = "todays_date", updatable = false)
    private Date todaysDate;
}
