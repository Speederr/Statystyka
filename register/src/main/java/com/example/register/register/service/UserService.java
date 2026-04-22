package com.example.register.register.service;

import com.example.register.register.DTO.CreateUserRequest;
import com.example.register.register.DTO.UserTableDto;
import com.example.register.register.model.*;
import com.example.register.register.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EntityManager entityManager;
    private final TeamRepository teamRepository;
    private final SectionRepository sectionRepository;
    private final PositionRepository positionRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Trying to load user with username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found in users table."));

        log.info("User found: {} with role: {}", user.getUsername(), user.getRole().getRoleName());

        // Tworzenie listy autoryzacji na podstawie roli użytkownika
        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName().toUpperCase()));

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
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);

        // Jeśli to pierwsze logowanie
        if (user.isFirstLogin()) {
            user.setFirstLogin(false);
            user.setPasswordChanged(true);
        }

        userRepository.save(user);
        entityManager.flush();
        log.info("Password changed and saved in the database for user: {}", username);
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
        // Pobranie obiektu Position na podstawie positionId
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

        userRepository.save(user);

        // Wysłanie e-maila do użytkownika
        emailService.sendUserCreationMail(
                email, firstName, lastName, username, temporaryPassword
        );
    }

    public String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 12); // Generuje losowe hasło o długości 12 znaków
    }


    public boolean isPasswordComplexEnough(String password) {
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_])[A-Za-z\\d@$!%*?&_]{8,}$";
        return password.matches(passwordPattern);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika: " + username));
    }

    public List<User> findByTeamWithSections(Team team) {
        return userRepository.findByTeamWithSections(team);
    }

    public UserImportResult importUsersFromFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Plik jest pusty.");
        }

        UserImportResult result = new UserImportResult();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;
            boolean headerSkipped = false;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (!headerSkipped) {
                    headerSkipped = true;
                    continue; // pomijamy nagłówek
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    CreateUserRequest request = parseLine(line, lineNumber);
                    createUserInBulk(request);
                    result.addSuccess();
                } catch (Exception e) {
                    result.addError("Linia " + lineNumber + ": " + e.getMessage());
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Nie udało się odczytać pliku.", e);
        }

        return result;
    }

    private CreateUserRequest parseLine(String line, int lineNumber) {
        String[] parts = line.split(";");

        if (parts.length != 8) {
            throw new IllegalArgumentException(
                    "nieprawidłowa liczba kolumn. Oczekiwano 8, otrzymano: " + parts.length
            );
        }

        try {
            return new CreateUserRequest(
                    parts[0].trim(),
                    parts[1].trim(),
                    parts[2].trim(),
                    parts[3].trim(),
                    Long.parseLong(parts[4].trim()),
                    Long.parseLong(parts[5].trim()),
                    Long.parseLong(parts[6].trim()),
                    Long.parseLong(parts[7].trim())
            );
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("roleId, teamId, sectionId lub positionId nie są liczbą.");
        }
    }

    private void validateBeforeCreate(CreateUserRequest request) {
        if (request.firstName() == null || request.firstName().isBlank()) {
            throw new IllegalArgumentException("imię jest puste.");
        }

        if (request.lastName() == null || request.lastName().isBlank()) {
            throw new IllegalArgumentException("nazwisko jest puste.");
        }

        if (request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException("username jest pusty.");
        }

        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("email jest pusty.");
        }

        if (userRepository.existsByUsername(request.username().trim())) {
            throw new IllegalArgumentException("username już istnieje: " + request.username());
        }

        if (userRepository.existsByEmail(request.email().trim())) {
            throw new IllegalArgumentException("email już istnieje: " + request.email());
        }
    }


    public void createUserInBulk(CreateUserRequest request) {
        validateBeforeCreate(request);

        String temporaryPassword = generateTemporaryPassword();

        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono roli o ID: " + request.roleId()));

        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono zespołu o ID: " + request.teamId()));

        Section section = sectionRepository.findById(request.sectionId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono sekcji o ID: " + request.sectionId()));

        Position position = positionRepository.findById(request.positionId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono stanowiska o ID: " + request.positionId()));

        User user = new User();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim());
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setRole(role);
        user.setTeam(team);
        user.setSection(section);
        user.setPosition(position);
        user.setFirstLogin(true);
        user.setCreateByAdmin(true);
        user.setPasswordChanged(false);

        userRepository.save(user);

        emailService.sendUserCreationMail(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                temporaryPassword
        );
    }
}
