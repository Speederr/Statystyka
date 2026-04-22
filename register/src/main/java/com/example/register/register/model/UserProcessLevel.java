package com.example.register.register.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_process_levels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProcessLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "process_id", nullable = false)
    private BusinessProcess process;

    @Column(name = "level", nullable = false)
    private Integer level;
}
