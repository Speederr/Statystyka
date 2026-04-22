package com.example.register.register.controller;

import com.example.register.register.DTO.SaveSingleRequestDTO;
import com.example.register.register.DTO.UserProcessLevelDTO;
import com.example.register.register.model.*;
import com.example.register.register.repository.UserProcessLevelRepository;
import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.ProcessService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
public class MatrixController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProcessService processService;
    @Autowired
    private UserProcessLevelRepository userProcessLevelRepository;


    @PostMapping("/matrix/saveSingle")
    @ResponseBody
    public ResponseEntity<String> saveSingleCompetency(@RequestBody SaveSingleRequestDTO request) {
        processService.saveSingleUserLevel(request.getUserId(), request.getProcessId(), request.getLevel());
        return ResponseEntity.ok("OK");
    }

    private void prepareMatrixData(User user, Model model) throws JsonProcessingException {
        Team team = user.getTeam();
        if (team == null) {
            throw new RuntimeException("User has no team assigned.");
        }

        List<BusinessProcess> processes = processService.getProcessesByTeamId(team.getId());
        List<UserProcessLevel> userLevels = userProcessLevelRepository.findByUserId(user.getId());

        List<UserProcessLevelDTO> userLevelDtos = userLevels.stream()
                .map(level -> new UserProcessLevelDTO(level.getProcess().getId(), level.getLevel()))
                .toList();

        ObjectMapper objectMapper = new ObjectMapper();
        String userLevelsJson = objectMapper.writeValueAsString(userLevelDtos);

        model.addAttribute("user", user);
        model.addAttribute("processes", processes);
        model.addAttribute("userLevelsJson", userLevelsJson);
        model.addAttribute("userId", user.getId());
    }

    @GetMapping("/matrix")
    public String getCompetencyMatrix(Model model, Principal principal) throws JsonProcessingException {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found."));
        model.addAttribute("browsedUser", user); // <-- dodaj to, jeśli nie ma w prepareMatrixData
        prepareMatrixData(user, model);
        return "matrix";
    }

    @GetMapping("/matrix/{userId}")
    public String getOneUserCompetencyMatrix(@PathVariable Long userId, Model model) throws JsonProcessingException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));
        model.addAttribute("browsedUser", user); // <-- dodaj to, jeśli nie ma w prepareMatrixData
        prepareMatrixData(user, model);
        return "matrix";
    }





}
