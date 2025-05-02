package com.example.register.register.controller;

import com.example.register.register.model.BusinessProcess;
import com.example.register.register.model.User;
import com.example.register.register.repository.ProcessRepository;
import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.BacklogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/backlog")
public class BacklogController {

    private final BacklogService backlogService;
    private final ProcessRepository processRepository;
    private final UserRepository userRepository;

    @Autowired
    public BacklogController(BacklogService backlogService, ProcessRepository processRepository, UserRepository userRepository) {
        this.backlogService = backlogService;
        this.processRepository = processRepository;
        this.userRepository = userRepository;
    }

@GetMapping
public String showBacklogForm(Model model, Principal principal) {
    if (principal == null) {
        return "redirect:/login";
    }

    String username = principal.getName();
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika: " + username));

    Long teamId = user.getTeam().getId();
    List<BusinessProcess> processes = processRepository.findByTeamId(teamId);
    Map<Long, Integer> backlogData = backlogService.getBacklogForTeam(teamId);

    Map<Long, Double> backlogHoursData = new HashMap<>();
    for (BusinessProcess process : processes) {
        int taskCount = backlogData.getOrDefault(process.getId(), 0);
        double backlogHours = taskCount * process.getAverageTime();
        backlogHoursData.put(process.getId(), backlogHours);
    }

    model.addAttribute("processes", processes);
    model.addAttribute("backlogData", backlogData);
    model.addAttribute("backlogHoursData", backlogHoursData);

    return "backlog";
}

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<Map<String, String>> saveBacklog(@RequestParam Map<String, String> backlogData) {
        Map<Long, Integer> backlogMap = backlogData.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("process_"))
                .collect(Collectors.toMap(
                        entry -> Long.parseLong(entry.getKey().replace("process_", "")),
                        entry -> entry.getValue().isEmpty() ? 0 : Integer.parseInt(entry.getValue())
                ));

        backlogService.saveBacklog(backlogMap);

        // ✅ Zwrot JSON jako odpowiedź
        Map<String, String> response = new HashMap<>();
        response.put("message", "Backlog został zapisany!");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/hours")
    public ResponseEntity<Map<LocalDate, Double>> getBacklogHours(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika: " + username));

        Long teamId = user.getTeam().getId();
        Map<LocalDate, Double> backlogHoursData = backlogService.getBacklogByDateForTeam(teamId);

        return ResponseEntity.ok(backlogHoursData);
    }

}
