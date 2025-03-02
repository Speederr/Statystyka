package com.example.register.register.controller;

import com.example.register.register.model.BusinessProcess;
import com.example.register.register.repository.ProcessRepository;
import com.example.register.register.service.BacklogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/backlog")
public class BacklogController {

    private final BacklogService backlogService;
    private final ProcessRepository processRepository;

    @Autowired
    public BacklogController(BacklogService backlogService, ProcessRepository processRepository) {
        this.backlogService = backlogService;
        this.processRepository = processRepository;
    }

    @GetMapping
    public String showBacklogForm(Model model) {
        List<BusinessProcess> processes = processRepository.findAll();
        model.addAttribute("processes", processes);
        model.addAttribute("backlogData", new HashMap<Long, Integer>());
        return "backlog";
    }
    @PostMapping("/save")
    public String saveBacklog(@RequestParam Map<String, String> backlogData) {
        Map<Long, Integer> backlogMap = backlogData.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("process_"))
                .collect(Collectors.toMap(
                        entry -> Long.parseLong(entry.getKey().replace("process_", "")),
                        entry -> entry.getValue().isEmpty() ? 0 : Integer.parseInt(entry.getValue()) // 🔹 Ustaw domyślne 0 dla pustych pól
                ));
        backlogService.saveBacklog(backlogMap);
        return "redirect:/backlog";
    }
}
