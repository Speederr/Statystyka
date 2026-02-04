package com.example.register.register.controller;

import com.example.register.register.DTO.UserDto;
import com.example.register.register.DTO.UserSummaryDTO;
import com.example.register.register.DTO.UserTableDto;
import com.example.register.register.model.*;
import com.example.register.register.repository.*;
import com.example.register.register.service.EmailService;
import com.example.register.register.service.PasswordResetService;
import com.example.register.register.service.UserService;
import com.example.register.register.service.UserSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PositionRepository positionRepository;
    private final AttendanceRepository attendanceRepository;
    private final EfficiencyRepository efficiencyRepository;
    private final SavedDataRepository savedDataRepository;
    private final TeamRepository teamRepository;
    private final SectionRepository sectionRepository;
    private final PasswordResetService passwordResetService;


    public UserController(UserService userService, UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService, PositionRepository positionRepository, AttendanceRepository attendanceRepository, EfficiencyRepository efficiencyRepository, SavedDataRepository savedDataRepository, TeamRepository teamRepository, SectionRepository sectionRepository, PasswordResetService passwordResetService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.positionRepository = positionRepository;
        this.attendanceRepository = attendanceRepository;
        this.efficiencyRepository = efficiencyRepository;
        this.savedDataRepository = savedDataRepository;
        this.teamRepository = teamRepository;
        this.sectionRepository = sectionRepository;
        this.passwordResetService = passwordResetService;
    }

    @Autowired
    private UserSummaryService userSummaryService;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    @PostMapping("/addUser")
    public ResponseEntity<Void> createUser(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam Long id_role,
            @RequestParam Long teamId,
            @RequestParam Long sectionId,
            @RequestParam Long positionId) {

        userService.createUser(firstName, lastName, username, email, id_role, teamId, sectionId, positionId);

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
            @RequestParam Map<String, String> teams,
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
                    case "ADMIN" -> 1;
                    case "MANAGER" -> 2;
                    case "COORDINATOR" -> 3;
                    case "USER" -> 4;
                    default -> -1;
                };

                if (roleId != -1) {
                    userService.updateUserRole(userId, roleId);
                    log.info("Updated user ID: {} with role ID: {}", userId, roleId);
                }
            }

            // 🔹 Aktualizacja sekcji użytkownika
            String selectedSection = sections.get("sections[" + userId + "]");
            if (selectedSection != null && !selectedSection.isEmpty()) {
                try {
                    Long sectionId = Long.parseLong(selectedSection);
                    userService.updateUserSection(userId, sectionId);
                    log.info("Updated user ID: {} with section ID: {}", userId, sectionId);
                } catch (NumberFormatException e) {
                    System.err.println("Błąd: Nieprawidłowy format ID sekcji dla użytkownika ID: " + userId);
                }
            }

            String selectedTeam = teams.get("teams[" + userId + "]");
            if(selectedTeam != null && !selectedTeam.isEmpty()) {
                try {
                    Long teamId = Long.parseLong(selectedTeam);
                    userService.updateUserTeam(userId, teamId);
                    log.info("Updated user ID: {} with team ID: {}", userId, teamId);
                } catch (NumberFormatException e) {
                    System.err.println("Błąd: Nieprawidłowy format ID zespołu dla użytkownika ID: " + userId);

                }
            }
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/adminPanel"))
                .build();
    }

    @PostMapping("/deleteUsers")
    public ResponseEntity<String> deleteSelectedUsers(
            @RequestParam(value = "selectedUsers", required = false) List<Long> selectedUsers) {

        if (selectedUsers == null || selectedUsers.isEmpty()) {
            return ResponseEntity.badRequest().body("❌ Nie zaznaczono żadnych użytkowników do usunięcia.");
        }

        for (Long userId : selectedUsers) {
            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika o ID: " + userId));

                if (user.isSuperAdmin()) {
                    return ResponseEntity.badRequest().body("❌ Nie można usunąć użytkownika Super Admin.");
                }

                userService.deleteUserById(userId);

            } catch (DataIntegrityViolationException dive) {
                return ResponseEntity.badRequest()
                        .body("❌ Nie można usunąć użytkownika o ID: " + userId + ", ponieważ ma powiązane dane.");
            } catch (IllegalArgumentException iae) {
                return ResponseEntity.badRequest().body("❌ " + iae.getMessage());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("❌ Błąd podczas usuwania użytkownika o ID: " + userId);
            }
        }

        return ResponseEntity.ok("✅ Użytkownicy zostali usunięci.");
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

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("redirect:/changePassword?error=mismatch"))
                    .build();
        }
        String username = authentication.getName();

        User user = userService.findByUsername(username);
        userService.updateUserPassword(username, newPassword);

        // Jeśli to pierwsze logowanie, zaktualizuj odpowiednie flagi
        if (user.isFirstLogin()) {
            user.setFirstLogin(false); // Zmień na false
            user.setPasswordChanged(true); // Ustaw że hasło zostało zmienione
            userRepository.save(user);
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/index"))
                .build();
    }
    @PostMapping("/restorePassword")
    @Transactional
    public ResponseEntity<Void> restorePassword(@RequestParam("email") String email) {
        passwordResetService.createResetTokenForEmail(email).ifPresent(token -> {
            String resetLink = appBaseUrl + "/reset-password?token=" + token;
            emailService.sendPasswordResetLinkEmail(email, resetLink);
        });

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/restorePassword?success=requestAccepted"))
                .build();
    }

    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<Void> resetPasswordWithToken(
            @RequestParam("token") String token,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword
    ) {
        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/reset-password?token=" + token + "&error=mismatch"))
                    .build();
        }

        if (!userService.isPasswordComplexEnough(newPassword)) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/reset-password?token=" + token + "&error=weakPassword"))
                    .build();
        }

        boolean resetSuccessful = passwordResetService.resetPasswordWithToken(token, newPassword);
        if (!resetSuccessful) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/reset-password?error=invalidToken"))
                    .build();
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/login?success=passwordReset"))
                .build();
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getUserProfile(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Użytkownik nie znaleziony."));

        // Pobierz nazwę sekcji
        String sectionName = null;
        if (user.getSection() != null) {
            sectionName = user.getSection().getSectionName();
        }

        String teamName = null;
        if (user.getTeam() != null) {
            teamName = user.getTeam().getTeamName();
        }

        UserDto userDto = new UserDto(
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail(),
                teamName,
                sectionName
        );

        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(@RequestParam("avatar") MultipartFile file, Principal principal) {

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

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Użytkownik nie znaleziony"));

        if (user.getAvatarUrl() == null) {
            log.info("Brak avatara dla użytkownika: {}", username);
            return ResponseEntity.ok("");
        }

        String base64Avatar = Base64.getEncoder().encodeToString(user.getAvatarUrl());
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

@GetMapping("/all-users")
public List<UserTableDto> getAllUsers(Principal principal) {
    LocalDate today = LocalDate.now();

    // 1. Pobierz zalogowanego użytkownika
    User currentUser = userRepository.findByUsername(principal.getName())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    Long teamId = currentUser.getTeam().getId();

    // 2. Pobierz tylko użytkowników z tego samego zespołu
    return userRepository.findByTeamId(teamId).stream()
            .filter(user -> {
                String role = user.getRole().getRoleName().toLowerCase();
                return !role.equals("admin") && !role.equals("manager");
            })
            .sorted(Comparator.comparing(User::getLastName)
                    .thenComparing(User::getFirstName))
            .map(user -> new UserTableDto(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    getUserEfficiency(user, today),
                    getNonOperationalTime(user, today),
                    user.getPosition().getId(),
                    positionRepository.findById(user.getPosition().getId())
                            .map(Position::getPositionName).orElse("Brak"),
                    getAttendanceStatus(user, today)
            ))
            .toList();
}

    private String getAttendanceStatus(User user, LocalDate date) {
        return attendanceRepository.findByUserAndAttendanceDate(user, date)
                .map(Attendance::getStatus) // 🟢 Pobiera bez zmian ("present" lub "leave")
                .orElse("leave"); // Domyślnie "leave", jeśli brak wpisu
    }


    private Double getUserEfficiency(User user, LocalDate todaysDate) {
        List<Efficiency> efficiencies = efficiencyRepository.findAllByUserAndTodaysDate(user, todaysDate);

        // Jeśli lista nie jest pusta, zwróć efektywność z pierwszego rekordu, inaczej zwróć 0.0
        return efficiencies.isEmpty() ? 0.0 : efficiencies.getFirst().getEfficiency();
    }

    private Double getNonOperationalTime(User user, LocalDate date) {
        double totalMinutes = savedDataRepository.findNonOperationalSavedDataByUserIdAndDate(user.getId(), date).stream()
                .mapToDouble(sd -> sd.getProcess().getAverageTime() * sd.getQuantity())
                .sum();

        double totalHours = totalMinutes / 60.0; // Konwersja minut na godziny
        return Math.round(totalHours * 100.0) / 100.0; // Zaokrąglenie do 2 miejsc po przecinku
    }

    @GetMapping("/by-section/{sectionId}")
    public ResponseEntity<List<UserDto>> getEmployeesBySection(@PathVariable Long sectionId) {
        List<User> users = userRepository.findBySection_Id(sectionId);
        List<UserDto> result = users.stream()
                .filter(user -> {
                    String role = user.getRole().getRoleName().toLowerCase();
                    return !role.equals("admin") && !role.equals("manager");
                })
                .map(user -> new UserDto(user.getId(), user.getFirstName(), user.getLastName()))
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserTableDto> getUserById(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Użytkownik nie istnieje"));

        LocalDate today = LocalDate.now();

        UserTableDto userTableDto = new UserTableDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                getUserEfficiency(user, today),
                getNonOperationalTime(user, today),
                user.getPosition().getId(),
                positionRepository.findById(user.getPosition().getId())
                        .map(Position::getPositionName).orElse("Brak"),
                getAttendanceStatus(user, today)
        );

        return ResponseEntity.ok(userTableDto);
    }

    @GetMapping("/filter-by-ids")
    public List<UserTableDto> getUsersByIds(@RequestParam List<Long> ids) {
        LocalDate today = LocalDate.now();

        // Pobieramy użytkowników na podstawie listy ID
        return userRepository.findAllById(ids).stream()
                .filter(user -> {
                    String role = user.getRole().getRoleName().toLowerCase();
                    return !role.equals("admin") && !role.equals("manager");
                })
                .map(user -> new UserTableDto(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        getUserEfficiency(user, today),  // Metoda do pobierania efektywności
                        getNonOperationalTime(user, today),  // Metoda do pobierania czasu nieoperacyjnego
                        user.getPosition().getId(),
                        positionRepository.findById(user.getPosition().getId()).map(Position::getPositionName).orElse("Brak"),
                        getAttendanceStatus(user, today)  // Metoda do pobierania statusu obecności
                ))
                .collect(Collectors.toList());
    }

    @GetMapping("/table/by-section/{sectionId}")
    public ResponseEntity<List<UserTableDto>> getEmployeesToTableBySection(@PathVariable Long sectionId) {
        LocalDate today = LocalDate.now();
        List<User> users = userRepository.findBySection_Id(sectionId);

        List<UserTableDto> result = users.stream()
                .filter(user -> {
                    String role = user.getRole().getRoleName().toLowerCase();
                    return !role.equals("admin") && !role.equals("manager");
                })
                .map(user -> new UserTableDto(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        getUserEfficiency(user, today),
                        getNonOperationalTime(user, today),
                        user.getPosition().getId(),
                        positionRepository.findById(user.getPosition().getId()).map(Position::getPositionName).orElse("Brak"),
                        getAttendanceStatus(user, today)
                ))
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/setup-data")
    public ResponseEntity<Map<String, Object>> getSetupData() {
        Map<String, Object> response = new HashMap<>();
        response.put("teams", teamRepository.findAll());
        response.put("sections", sectionRepository.findAll());
        response.put("positions", positionRepository.findAll());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/complete-setup")
    public ResponseEntity<Void> completeSetup(
            Principal principal,
            @RequestParam Long teamId,
            @RequestParam Long sectionId,
            @RequestParam Long positionId) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setTeam(teamRepository.findById(teamId).orElseThrow());
        user.setSection(sectionRepository.findById(sectionId).orElseThrow());
        user.setPosition(positionRepository.findById(positionId).orElseThrow());

        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/whoami")
    public ResponseEntity<Map<String, Object>> whoAmI(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("firstLogin", user.isFirstLogin());
        data.put("isCreateByAdmin", user.isCreateByAdmin());
        data.put("passwordChanged", user.isPasswordChanged());

        // Dodajemy informacje o polach, które mogą być null
        data.put("team", user.getTeam() != null ? user.getTeam().getId() : null);
        data.put("section", user.getSection() != null ? user.getSection().getId() : null);
        data.put("position", user.getPosition() != null ? user.getPosition().getId() : null);

        return ResponseEntity.ok(data);
    }

    @GetMapping("/summary/{userId}")
    public UserSummaryDTO getUserSummary(@PathVariable Long userId) {
        return userSummaryService.getUsersSummary(userId);
    }

}
