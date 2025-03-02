package com.example.register.register.controller;

import com.example.register.register.model.BusinessProcess;
import com.example.register.register.model.Team;
import com.example.register.register.repository.ProcessRepository;
import com.example.register.register.repository.TeamRepository;
import com.example.register.register.repository.UserFavoritesRepository;
import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/api/processes")
public class ProcessController {

    @Autowired
    private final ProcessService processService;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final ProcessRepository processRepository;

    @Autowired
    private final UserFavoritesRepository userFavoritesRepository;

    @Autowired
    private final TeamRepository teamRepository;

    public ProcessController(ProcessService processService, UserRepository userRepository, ProcessRepository processRepository, UserFavoritesRepository userFavoritesRepository, TeamRepository teamRepository) {
        this.processService = processService;
        this.userRepository = userRepository;
        this.processRepository = processRepository;
        this.userFavoritesRepository = userFavoritesRepository;
        this.teamRepository = teamRepository;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllProcesses() {
        List<BusinessProcess> processes = processService.getAllProcesses();

        // Konwersja na mapę tylko z nazwą procesu
        List<Map<String, Object>> response = processes.stream()
                .map(process -> Map.of(
                        "id", process.getId(),
                        "processName", (Object) process.getProcessName()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/favorites/{userId}")
    public ResponseEntity<List<BusinessProcess>> getFavoriteProcesses(@PathVariable Long userId) {
        System.out.println("Otrzymano zapytanie o ulubione procesy dla userId: " + userId);
        List<BusinessProcess> favorites = processService.getFavoriteProcesses(userId);

        if (favorites == null || favorites.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(favorites);
    }

    @PostMapping("/favorites/{userId}")
    public ResponseEntity<Map<String, String>> saveFavoriteProcesses(@PathVariable Long userId, @RequestBody List<Long> processIds) {
        processService.saveFavoriteProcesses(userId, processIds);

        // ✅ Zwracamy poprawny JSON zamiast stringa
        Map<String, String> response = new HashMap<>();
        response.put("message", "Ulubione procesy zapisane!");

        return ResponseEntity.ok(response);
    }


    @GetMapping("/processes")
    public String showProcesses(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            Long userId = userRepository.findUserIdByUsername(username);

            model.addAttribute("userId", userId);

            // Pobierz wszystkie procesy
            List<BusinessProcess> allProcesses = processService.getAllProcesses();

            // Pobierz ulubione procesy
            List<BusinessProcess> favoriteProcesses = processService.getFavoriteProcesses(userId);

            // Usunięcie ulubionych procesów z listy wszystkich procesów
            List<Long> favoriteProcessIds = favoriteProcesses.stream()
                    .map(BusinessProcess::getId)
                    .toList();
            List<BusinessProcess> filteredProcesses = allProcesses.stream()
                    .filter(process -> !favoriteProcessIds.contains(process.getId()))
                    .toList();

            model.addAttribute("allProcesses", filteredProcesses);
            model.addAttribute("favoriteProcesses", favoriteProcesses);
        } else {
            model.addAttribute("userId", null);
            model.addAttribute("allProcesses", Collections.emptyList());
            model.addAttribute("favoriteProcesses", Collections.emptyList());
        }

        return "processes";
    }

    @PostMapping("/saveNewProcess")
    public String saveNewProcess(@ModelAttribute("process") BusinessProcess process,
                                 @RequestParam("team") Long teamId) {

        Team team = teamRepository.findById(teamId)
                        .orElseThrow(() -> new RuntimeException("Nie znaleziono zespołu: " + teamId));

        process.setTeam(team);
        processRepository.save(process);
        return "redirect:/averageTime";
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateProcess(@RequestBody BusinessProcess updatedProcess) {
        Optional<BusinessProcess> existingProcess = processRepository.findById(updatedProcess.getId());

        if (existingProcess.isPresent()) {
            BusinessProcess process = existingProcess.get();
            process.setAverageTime(updatedProcess.getAverageTime());
            processRepository.save(process);
            return ResponseEntity.ok().body("{\"message\": \"Process updated successfully\"}");
        } else {
            return ResponseEntity.badRequest().body("{\"error\": \"Process not found\"}");
        }
    }

    @GetMapping("/new")
    public String showProcessForm(Model model) {
        model.addAttribute("process", new BusinessProcess());
        model.addAttribute("teams", teamRepository.findAll());
        return "process-form";
    }

}
