package com.example.register.register.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_role", nullable = false)
    private Role role;

    @Column(name = "is_first_login", nullable = false)
    private boolean firstLogin;

    @ManyToOne
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @Column(name = "avatar_url", columnDefinition = "bytea")
    private byte[] avatarUrl;


    @Column(name = "is_super_admin", nullable = false)
    private boolean isSuperAdmin;

    @Column(name = "is_create_by_admin", nullable = false)
    private boolean isCreateByAdmin;

    @Column(name = "isPasswordChanged", nullable = false)
    private boolean passwordChanged = false;


    @ManyToMany
    @JsonBackReference
    @JoinTable(
            name = "user_favorites",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "process_id")
    )
    private Set<BusinessProcess> favoriteProcesses = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;


}