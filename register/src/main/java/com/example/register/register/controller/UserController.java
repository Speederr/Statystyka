package com.example.register.register.controller;

import com.example.register.register.model.User;
import com.example.register.register.model.UserDto;
import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.EmailService;
import com.example.register.register.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/user/")
public class UserController {

    @Autowired
    private final UserService userService;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PageController pageController;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final EmailService emailService;


    public UserController(UserService userService, UserRepository userRepository, PageController pageController, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.pageController = pageController;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }


    @PostMapping("/addUser")
    public ResponseEntity<Void> createUser(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam Long id_role,
            @RequestParam Long teamId,
            @RequestParam Long sectionId) {

        userService.createUser(firstName, lastName, username, email, id_role, teamId, sectionId);

        // Return a ResponseEntity with a redirect header
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/adminPanel")) // Set the redirect location
                .build();
    }

    @PostMapping("/saveUsers")
    @Transactional
    public ResponseEntity<Void> saveSelectedUsers(
            @RequestParam(value = "selectedUsers", required = false) List<Long> selectedUsers,
            @RequestParam Map<String, String> roles,
            @RequestParam Map<String, String> sections,
            Model model) {

        if (selectedUsers == null || selectedUsers.isEmpty()) {
            model.addAttribute("error", "Nie zaznaczono żadnych użytkowników. Wybierz przynajmniej jednego użytkownika");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/settings"))
                    .build();
        }

        for (Long userId : selectedUsers) {
            // 🔹 Aktualizacja roli użytkownika
            String selectedRole = roles.get("roles[" + userId + "]");
            if (selectedRole != null) {
                int roleId = switch (selectedRole) {
                    case "Admin" -> 1;
                    case "Manager" -> 2;
                    case "Coordinator" -> 3;
                    case "User" -> 4;
                    default -> -1;
                };

                if (roleId != -1) {
                    userService.updateUserRole(userId, roleId);
                    System.out.println("Updated user ID: " + userId + " with role ID: " + roleId);
                }
            }

            // 🔹 Aktualizacja sekcji użytkownika
            String selectedSection = sections.get("sections[" + userId + "]");
            if (selectedSection != null && !selectedSection.isEmpty()) {
                try {
                    Long sectionId = Long.parseLong(selectedSection);
                    userService.updateUserSection(userId, sectionId);
                    System.out.println("Updated user ID: " + userId + " with section ID: " + sectionId);
                } catch (NumberFormatException e) {
                    System.err.println("Błąd: Nieprawidłowy format ID sekcji dla użytkownika ID: " + userId);
                }
            }
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/adminPanel"))
                .build();
    }


    @PostMapping("/deleteUsers")
    @Transactional
    public ResponseEntity<Void> deleteSelectedUsers(@RequestParam(value = "selectedUsers", required = false) List<Long> selectedUsers, Model model) {
        if (selectedUsers == null || selectedUsers.isEmpty()) {
            model.addAttribute("error", "Nie zaznaczono żadnych użytkowników do usunięcia.");
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/adminPanel")).build();
        }

        for (Long userId : selectedUsers) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika o ID: " + userId));

            if (user.isSuperAdmin()) {
                model.addAttribute("error", "Nie można usunąć użytkownika Super Admin.");
                return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/adminPanel")).build();
            }

            try {
                userService.deleteUserById(userId);
                System.out.println("Deleted user ID: " + userId);
            } catch (Exception e) {
                model.addAttribute("error", "Nie udało się usunąć użytkownika o ID: " + userId);
                return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/adminPanel")).build();
            }
        }

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/adminPanel")).build();
    }



    @GetMapping("/info")
    public ResponseEntity<UserDto> getUserInfo(Principal principal) {
        // Pobierz nazwę użytkownika z Principal
        String username = principal.getName();

        // Znajdź użytkownika w bazie lub rzuć wyjątek, jeśli nie znaleziono
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Użytkownik nie znaleziony."));

        // Utwórz DTO na podstawie danych użytkownika
        UserDto userDto = new UserDto();
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setAvatarUrl(user.getAvatarUrl());  // Zakładam, że masz to pole w encji User
        userDto.setRoleId(user.getRole().getId());   // Pobranie ID roli z obiektu Role

        return ResponseEntity.ok(userDto);  // Zwróć 200 OK z danymi użytkownika
    }


    @PostMapping("/changePassword")
    @Transactional
    public ResponseEntity<Void> changePassword(
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Authentication authentication) {

        if(!newPassword.equals(confirmPassword)) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("redirect:/changePassword?error=mismatch")) // Set the redirect location
                    .build();
        }
        String username = authentication.getName();

        userService.updateUserPassword(username, newPassword);
        userService.updateFirstLoginStatus(username);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/index")) // Set the redirect location
                .build();
    }

    @PostMapping("/restorePassword")
    @Transactional
    public ResponseEntity<Void> restorePassword(@RequestParam("email") String email) {

        System.out.println("🔹 Otrzymano żądanie zmiany hasła dla użytkownika: " + email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("❌ Użytkownik nie istnieje."));

        String temporaryPassword = userService.generateTemporaryPassword();

        userService.updateUserPassword(user.getUsername(), temporaryPassword);
        System.out.println("✅ Hasło użytkownika zostało zaktualizowane!");

        emailService.sendTemporaryPasswordEmail(user.getEmail(), temporaryPassword);
        System.out.println("📧 Wysłano e-mail z nowym hasłem!");

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/restorePassword?success=emailSent"))
                .build();

    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getUserProfile(Principal principal) {

        String username = principal.getName();  // Pobranie nazwy użytkownika
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Użytkownik nie znaleziony."));

        // Konwersja User na UserDto, aby nie zwracać wszystkich danych (np. hasła)
        UserDto userDto = new UserDto(
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail()
        );

        return ResponseEntity.ok(userDto);
    }


    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(@RequestParam("avatar")MultipartFile file, Principal principal) {

        String username = principal.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Użytkownik nie znaleziony"));

        try {
            user.setAvatarUrl(file.getBytes());
            userRepository.save(user);
            return ResponseEntity.ok("Avatar zapisany pomyślnie.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Błąd podczas zapisu avatara.");
        }
    }

    @GetMapping("/avatar")
    public ResponseEntity<String> getUserAvatar(Principal principal) {
        String username = principal.getName();
        System.out.println("Żądanie avatara dla użytkownika: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Użytkownik nie znaleziony"));

        if (user.getAvatarUrl() == null) {
            System.out.println("Brak avatara dla użytkownika: " + username);
            return ResponseEntity.ok("");
        }

        String base64Avatar = Base64.getEncoder().encodeToString(user.getAvatarUrl());
        System.out.println("Avatar wczytany dla użytkownika: " + username);
        return ResponseEntity.ok(base64Avatar);
    }

    @PostMapping("/changePasswordInSettings")
    public ResponseEntity<Map<String, String>> changePasswordInSettings(
            @RequestParam String settingsCurrentPassword,
            @RequestParam String settingsNewPassword,
            Principal principal) {

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Użytkownik nie znaleziony."));

        if (!passwordEncoder.matches(settingsCurrentPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Aktualne hasło jest nieprawidłowe."));
        }

        user.setPassword(passwordEncoder.encode(settingsNewPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Collections.singletonMap("success", "Hasło zostało pomyślnie zmienione!"));
    }
    @GetMapping("/users")
    public String getUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "users"; // Nazwa pliku HTML w folderze templates
    }

}

