package com.example.register.register.controller;

import com.example.register.register.model.BusinessProcess;
import com.example.register.register.model.Team;
import com.example.register.register.model.User;
import com.example.register.register.repository.ProcessRepository;
import com.example.register.register.repository.TeamRepository;
import com.example.register.register.repository.UserFavoritesRepository;
import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;


@RestController
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
        System.out.println("🔵 Wywołano showProcesses()");

        if (principal != null) {
            String username = principal.getName();
            System.out.println("👤 Zalogowany użytkownik: " + username);

            Long userId = userRepository.findUserIdByUsername(username);
            System.out.println("🔎 ID użytkownika: " + userId);

            // Pobierz zespół użytkownika
            Team userTeam = userRepository.findTeamByUsername(username);
            if (userTeam == null) {
                System.out.println("❌ Użytkownik " + username + " nie ma przypisanego zespołu!");
                model.addAttribute("allProcesses", Collections.emptyList());
                model.addAttribute("favoriteProcesses", Collections.emptyList());
                return "processes";
            }

            Long teamId = userTeam.getId();
            System.out.println("✅ Użytkownik " + username + " należy do zespołu ID: " + teamId);

            // Pobierz tylko procesy z jego zespołu
            List<BusinessProcess> allProcesses = processService.getProcessesByTeamId(teamId);
            System.out.println("📌 Znalezione procesy dla teamId " + teamId + ": " + allProcesses.size());

            // Pobierz ulubione procesy
            List<BusinessProcess> favoriteProcesses = processService.getFavoriteProcesses(userId);
            List<Long> favoriteProcessIds = favoriteProcesses.stream()
                    .map(BusinessProcess::getId)
                    .toList();

            List<BusinessProcess> filteredProcesses = allProcesses.stream()
                    .filter(process -> !favoriteProcessIds.contains(process.getId()))
                    .toList();

            model.addAttribute("allProcesses", filteredProcesses);
            model.addAttribute("favoriteProcesses", favoriteProcesses);
        } else {
            System.out.println("⚠ Brak zalogowanego użytkownika!");
            model.addAttribute("userId", null);
            model.addAttribute("allProcesses", Collections.emptyList());
            model.addAttribute("favoriteProcesses", Collections.emptyList());
        }

        return "processes";
    }

    @GetMapping("/team/{userId}")
    public ResponseEntity<List<BusinessProcess>> getProcessesForUserTeam(@PathVariable Long userId) {
        System.out.println("🔍 Pobieranie procesów dla userId: " + userId);

        // Pobierz użytkownika
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getTeam() == null) {
            System.out.println("⚠ Brak użytkownika lub brak przypisanego zespołu!");
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        Long teamId = user.getTeam().getId();
        System.out.println("✅ Użytkownik należy do teamId: " + teamId);

        // Pobierz procesy tylko dla tego zespołu
        List<BusinessProcess> processes = processService.getProcessesByTeamId(teamId);
        System.out.println("📌 Znaleziono procesy: " + processes.size());

        return ResponseEntity.ok(processes);
    }

@PostMapping("/saveNewProcess")
@ResponseBody
public ResponseEntity<String> saveNewProcess(@RequestParam("teamId") Long teamId, @ModelAttribute BusinessProcess process) {
    Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Nie znaleziono zespołu: " + teamId));
    process.setTeam(team);
    processRepository.save(process);
    return ResponseEntity.ok("Proces został dodany");
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

    @GetMapping("/by-logged-user")
    public ResponseEntity<List<Map<String, Object>>> getProcessesForLoggedUser(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null || user.getTeam() == null) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        List<BusinessProcess> processes = processService.getProcessesByTeamId(user.getTeam().getId());

        // Posortuj alfabetycznie po nazwie procesu i od razu zamapuj potrzebne pola
        List<Map<String, Object>> response = processes.stream()
                .sorted(Comparator.comparing(BusinessProcess::getProcessName))
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId());
                    map.put("processName", p.getProcessName());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(response);
    }



}
