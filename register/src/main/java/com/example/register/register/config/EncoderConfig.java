package com.example.register.register.config;

import com.example.register.register.model.*;
import com.example.register.register.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class EncoderConfig {


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository,
                                RoleRepository roleRepository,
                                PositionRepository positionRepository,
                                TeamRepository teamRepository,
                                SectionRepository sectionRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${app.bootstrap.admin.username}") String adminUsername,
                                @Value("${app.bootstrap.admin.email}") String adminEmail,
                                @Value("${app.bootstrap.admin.password}") String adminPassword) {
        return args -> {
            boolean adminExists = userRepository.existsByUsername(adminUsername);

            if (!adminExists) {
                Role adminRole = roleRepository.findByRoleName("ADMIN")
                        .orElseThrow(() -> new RuntimeException("Brak roli ADMIN"));

                Position defaultPosition = positionRepository.findByPositionName("Administrator")
                        .orElseGet(() -> {
                            Position position = new Position();
                            position.setPositionName("Administrator");
                            return positionRepository.save(position);
                        });

                Team defaultTeam = teamRepository.findByTeamName("IT")
                        .orElseGet(() -> {
                            Team team = new Team();
                            team.setTeamName("IT");
                            return teamRepository.save(team);
                        });

                Section defaultSection = sectionRepository.findBySectionName("Administracja")
                        .orElseGet(() -> {
                            Section section = new Section();
                            section.setSectionName("Administracja");
                            section.setTeam(defaultTeam);
                            return sectionRepository.save(section);
                        });

                User admin = new User();
                admin.setFirstName("System");
                admin.setLastName("Administrator");
                admin.setUsername(adminUsername);
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole(adminRole);
                admin.setPosition(defaultPosition);
                admin.setTeam(defaultTeam);
                admin.setSection(defaultSection);
                admin.setFirstLogin(true);
                admin.setSuperAdmin(true);
                admin.setCreateByAdmin(false);
                admin.setPasswordChanged(false);

                userRepository.save(admin);

                System.out.println("Utworzono konto administratora: " + adminUsername);
            } else {
                System.out.println("Użytkownik " + adminUsername + " już istnieje.");
            }
        };
    }



}
