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
@Table(name = "saved_data")
public class SavedData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long user_id;

    @Column(name = "process_id", nullable = false)
    private Long process_id;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @CreationTimestamp // Automatycznie ustawia bieżącą datę
    @Temporal(TemporalType.DATE)
    @Column(name = "todays_date", updatable = false)
    private Date todaysDate;
}
