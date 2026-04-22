package com.example.register.register.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "user_favorites")
public class UserFavorites {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)  // Poprawione mapowanie użytkownika
    private User user;

    @ManyToOne
    @JoinColumn(name = "process_id", nullable = false)  // Mapowanie procesu
    private BusinessProcess process;

}
