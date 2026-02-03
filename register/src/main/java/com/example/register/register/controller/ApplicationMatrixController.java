package com.example.register.register.controller;

import com.example.register.register.DTO.SaveSingleAppRequestDTO;
import com.example.register.register.DTO.UserAppLevelDTO;
import com.example.register.register.model.Application;
import com.example.register.register.model.User;
import com.example.register.register.model.UserApplicationLevel;
import com.example.register.register.repository.ApplicationRepository;
import com.example.register.register.repository.UserApplicationLevelRepository;
import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.ApplicationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ApplicationMatrixController {


    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final ApplicationService applicationService;
    private final UserApplicationLevelRepository userApplicationLevelRepository;


    @GetMapping("/app-matrix")
    public String getAppMatrix(Model model, Principal principal) throws JsonProcessingException {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found."));
        model.addAttribute("browsedUser", user);
        prepareAppMatrixData(user, model);
        return "apps"; // Twój szablon z macierzą aplikacji
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/app-matrix/{userId}")
    public String getAppMatrixPerUser(@PathVariable Long userId, Model model) throws JsonProcessingException {

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("browsedUser", user);
        prepareAppMatrixData(user, model);
        return "apps";
    }



    @PostMapping("/app-matrix/saveSingle")
    @ResponseBody
    public ResponseEntity<String> saveSingleAppLevel(@RequestBody SaveSingleAppRequestDTO request) {

        applicationService.saveSingleUserAppLevel(request.getUserId(), request.getApplicationId(), request.getLevel());
        return ResponseEntity.ok("OK");
    }

    private void prepareAppMatrixData(User user, Model model) throws JsonProcessingException {
        // Zakładamy, że nie musisz sprawdzać teamu dla aplikacji – jeśli potrzebujesz, dodaj.
        List<Application> apps = applicationRepository.findAll();
        List<UserApplicationLevel> userAppLevels = userApplicationLevelRepository.findByUser(user);

        // Stwórz DTO do JSON-a
        List<UserAppLevelDTO> userAppLevelDtos = userAppLevels.stream()
                .map(level -> new UserAppLevelDTO(level.getApplication().getId(), level.getLevel()))
                .toList();

        ObjectMapper objectMapper = new ObjectMapper();
        String userAppLevelsJson = objectMapper.writeValueAsString(userAppLevelDtos);

        model.addAttribute("apps", apps);
        model.addAttribute("userAppLevelsJson", userAppLevelsJson);
        model.addAttribute("userId", user.getId());
    }

    @PostMapping("/saveNewAppName")
    public ResponseEntity<?> createNewApplication(@RequestBody AppDTO appDTO) {

        String name = appDTO.appName;
        if(name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Nazwa aplikacji jest wymagana."));
        }
        if(applicationRepository.existsByAppNameIgnoreCase(name.trim())) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Nazwa aplikacji już istnieje."));
        }

        Application application = new Application();
        application.setAppName(name.trim());
        applicationRepository.save(application);

        return ResponseEntity.ok(Collections.singletonMap("success", "Pomyślnie dodano aplikację."));
    }

    public record AppDTO(String appName) {}


}
