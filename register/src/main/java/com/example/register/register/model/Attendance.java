package com.example.register.register.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "status", nullable = false)
    private String status; // "present" lub "leave"

    @Column(name = "work_mode")
    private String workMode; // np. "office", "homeoffice", "hybrid"


    public Attendance(User user, LocalDate attendanceDate, String status, String workMode) {
        this.user = user;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.workMode = workMode;
    }

}
