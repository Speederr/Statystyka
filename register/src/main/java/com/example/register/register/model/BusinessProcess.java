package com.example.register.register.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "processes")
public class BusinessProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "process_name")
    private String processName;

    @Column(name = "average_time")
    private Double averageTime;

    @Column(name = "is_non_operational", nullable = false)
    private boolean nonOperational; // ✅ Zmiana nazwy pola na `nonOperational`

    @ManyToMany(mappedBy = "favoriteProcesses")
    @JsonIgnore  // ⬅️ Ignorowanie serializacji w Jacksonie
    private Set<User> users = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
}
