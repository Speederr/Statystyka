package com.example.register.register.service;

import com.example.register.register.model.*;
import com.example.register.register.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.util.*;


@Service
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EntityManager entityManager;
    private final TeamRepository teamRepository;
    private final SectionRepository sectionRepository;
    private final PositionRepository positionRepository;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, EmailService emailService, EntityManager entityManager, TeamRepository teamRepository, SectionRepository sectionRepository, PositionRepository positionRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.entityManager = entityManager;
        this.teamRepository = teamRepository;
        this.sectionRepository = sectionRepository;
        this.positionRepository = positionRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Trying to load user with username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found in users table."));

        log.info("User found: {} with role: {}", user.getUsername(), user.getRole().getRoleName());

        // Tworzenie listy autoryzacji na podstawie roli użytkownika
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName().toUpperCase()));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }


    @Transactional
    public void updateUserRole(Long userId, int newRoleId) {
        // Zaktualizuj rolę użytkownika w bazie danych
        userRepository.updateUserRoleById(userId, newRoleId);
        log.info("Updated role for userId: {} to roleId {}", userId, newRoleId);
    }

    @Transactional
    public void updateUserSection(Long userId, Long newSectionId) {
        userRepository.updateUserSectionById(userId, newSectionId);
        log.info("Updated section for userId: {} to sectionId {}", userId, newSectionId);
    }

    @Transactional
    public void updateUserTeam(Long userId, Long newTeamId) {
        userRepository.updateUserTeamById(userId, newTeamId);
        log.info("Updated team for userId: {} to teamId {}", userId, newTeamId);
    }

    @Transactional
    public void deleteUserById(Long userId) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isPresent()) {
            userRepository.deleteUserById(userId);
        } else {
            throw new EntityNotFoundException("User with id " + userId + " not found.");
        }
    }

    @Transactional
    public void updateUserPassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("❌ Użytkownik nie znaleziony"));

        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);

        // Jeśli to pierwsze logowanie
        if (user.isFirstLogin()) {
            user.setFirstLogin(false); // Zmień na false
            user.setPasswordChanged(true); // Oznacz że hasło zostało zmienione
        }

        userRepository.save(user);
        entityManager.flush();
        log.info("Hasło zmienione i zapisane w bazie dla użytkownika: {}", username);
    }

    //funckja dla admina
    public void createUser(String firstName, String lastName, String username, String email,
                           Long roleId, Long teamId, Long sectionId, Long positionId) {

        // Generowanie tymczasowego hasła
        String temporaryPassword = generateTemporaryPassword();

        // Pobranie obiektu Role na podstawie roleId
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono roli o ID: " + roleId));

        // Pobranie obiektu Team na podstawie teamId
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono zespołu o ID: " + teamId));

        // Pobranie obiektu Section na podstawie sectionId
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono sekcji o ID: " + sectionId));

        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono stanowiska o ID: " + positionId));

        // Tworzenie nowego użytkownika
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);

        // Haszowanie hasła
        String hashedPassword = passwordEncoder.encode(temporaryPassword);
        user.setPassword(hashedPassword);

        user.setRole(role);
        user.setTeam(team); // Ustawienie zespołu
        user.setSection(section); // Ustawienie sekcji
        user.setPosition(position);
        user.setFirstLogin(true);
        user.setCreateByAdmin(true);
        user.setPasswordChanged(false);

        userRepository.save(user); // Zapisz użytkownika w bazie danych

        // Wysłanie e-maila do użytkownika
        emailService.sendUserCreationMail(
                email, firstName, lastName, username, temporaryPassword
        );
    }
    //funkcja dla endpointu /register
    public void createUser(String firstName, String lastName, String username, String email, Long id_role) {

        // Generowanie tymczasowego hasła
        String temporaryPassword = generateTemporaryPassword();

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(roleRepository.findById(id_role).orElseThrow());

        // Haszowanie hasła
        String hashedPassword = passwordEncoder.encode(temporaryPassword);
        user.setPassword(hashedPassword);
        user.setFirstLogin(true);
        user.setCreateByAdmin(false);
        user.setPasswordChanged(false);


        userRepository.save(user);

        emailService.sendUserCreationMail(
                email, firstName, lastName, username, temporaryPassword
        );
    }

    public String generateTemporaryPassword() {
        // Możesz użyć bardziej złożonego algorytmu, jeśli to konieczne
        return UUID.randomUUID().toString().substring(0, 12); // Generuje losowe hasło o długości 12 znaków
    }


    public boolean isPasswordComplexEnough(String password) {
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_])[A-Za-z\\d@$!%*?&_]{8,12}$";
        return password.matches(passwordPattern);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika: " + username));
    }

    public List<User> findByTeam(Team team) {
        return userRepository.findAllByTeam(team);
    }

}
