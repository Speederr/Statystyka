package com.example.register.register.controller;

import com.example.register.register.model.FormUsers;
import com.example.register.register.model.UserDto;
import com.example.register.register.service.ItemService;
import com.example.register.register.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user/")
public class UserController {

    @Autowired
    private final UserService userService;

    @Autowired
    private PageController pageController;

    public UserController(UserService userService, ItemService itemService) {
        this.userService = userService;

    }

    @PostMapping("/settings")
    public ResponseEntity<Void> createUser(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam Long id_role) {

        FormUsers user = new FormUsers();
 //       userService.saveUserForm(List.of(user));
        userService.createUser(firstName, lastName, username, email, id_role);

        // Return a ResponseEntity with a redirect header
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/settings")) // Set the redirect location
                .build();
    }

    @PostMapping("/saveUsers")
    @Transactional
    public ResponseEntity<Void> saveSelectedUsers(@RequestParam(value = "selectedUsers", required = false) List<Long> selectedUsers,
                                    @RequestParam Map<String, String> roles, Model model) {
        // Sprawdzenie, czy nie zaznaczono żadnych użytkowników
        if (selectedUsers == null || selectedUsers.isEmpty()) {
            model.addAttribute("error", "Nie zaznaczono żadnych użytkowników. Wybierz przynajmniej jednego użytkownika");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/settings")) // Set the redirect location
                    .build();
        }

        // Pętla przez zaznaczonych użytkowników
        for (Long userId : selectedUsers) {
            String selectedRole = roles.get("roles[" + userId + "]");

            // Mapowanie nazw ról na ID
            int roleId;
            switch (selectedRole) {
                case "Admin":
                    roleId = 1; // ID dla Admin
                    break;
                case "Manager":
                    roleId = 2; // ID dla Manager
                    break;
                case "Coordinator":
                    roleId = 3; // ID dla Koordynator
                    break;
                case "User":
                    roleId = 4; // ID dla Użytkownik
                    break;
                default:
                    continue; // Nieznana rola, pomiń aktualizację
            }

            try {
                userService.updateUserRole(userId, roleId);
                System.out.println("Updated user ID: " + userId + " with role ID: " + roleId); // Logowanie
            } catch (Exception e) {
                model.addAttribute("error", "Nie udało się zaktualizować roli dla użytkownika o ID: " + userId);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("/settings")) // Set the redirect location
                        .build();
            }
        }

        // Return a ResponseEntity with a redirect header
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/settings")) // Set the redirect location
                .build();
    }

    @PostMapping("/deleteUsers")
    @Transactional
    public ResponseEntity<Void> deleteSelectedUsers(@RequestParam(value = "selectedUsers", required = false) List<Long> selectedUsers, Model model) {

        if (selectedUsers == null || selectedUsers.isEmpty()) {
            model.addAttribute("error", "Nie zaznaczono żadnych użytkowników do usunięcia. Wybierz przynajmniej jednego użytkownika");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/settings")) // Set the redirect location
                    .build();
        }

        for (Long userId : selectedUsers) {
            try {
                userService.deleteUserById(userId);
                System.out.println("Deleted user ID: " + userId);
            } catch (Exception e) {
                model.addAttribute("error", "Nie udało się usunąć użytkownika o ID: " + userId);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("/settings")) // Set the redirect location
                        .build();
            }
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/settings")) // Set the redirect location
                .build();
    }

    @PostMapping("/changePassword")
    @Transactional
    public ResponseEntity<Void> changePassword(@RequestParam("newPassword") String newPassword, @RequestParam("confirmPassword") String confirmPassword, Authentication authentication) {

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

    @GetMapping("/info")
    public ResponseEntity<UserDto> getUserInfo(Principal principal) {
        String username = principal.getName();

        // Znajdź użytkownika w bazie lub rzuć wyjątek, jeśli nie znaleziono
        FormUsers user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Użytkownik nie znaleziony."));

        // Utwórz DTO na podstawie danych użytkownika
        UserDto userDto = new UserDto();
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setAvatar_url(user.getAvatar_url());  // Zakładam, że masz to pole w encji FormUsers
        userDto.setRoleId(user.getId_role());

        return ResponseEntity.ok(userDto);  // Zwróć 200 OK z danymi użytkownika
    }

}

