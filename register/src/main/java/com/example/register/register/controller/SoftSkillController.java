package com.example.register.register.controller;

import com.example.register.register.DTO.SaveSingleSoftSkillRequestDTO;
import com.example.register.register.DTO.UserSoftSkillLevelDTO;
import com.example.register.register.model.*;
import com.example.register.register.repository.SoftSkillLevelRepository;
import com.example.register.register.repository.SoftSkillRepository;
import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.SoftSkillService;
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
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SoftSkillController {

    private final UserRepository userRepository;
    private final SoftSkillRepository softSkillRepository;
    private final SoftSkillService softSkillService;
    private final SoftSkillLevelRepository softSkillLevelRepository;

    @GetMapping("/soft-skills")
    public String showSoftSkills(Model model, Principal principal) throws JsonProcessingException {

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Username not found."));

        model.addAttribute("browsedUser", user);
        prepareSoftSkillMatrixData(user, model);
        return "softSkills";
    }

    @GetMapping("/soft-skills/{userId}")
    @PreAuthorize("hasRole('ADMIN)")
    public String showSoftSkillsPerUser(@PathVariable Long userId, Model model) throws JsonProcessingException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("browsedUser", user);
        prepareSoftSkillMatrixData(user, model);
        return "softSkills";
    }

    @PostMapping("/saveNewSoftSkill")
    public ResponseEntity<?> createNewSoftSkill(@RequestBody Map<String, String> payload) {

        String skillType = payload.get("skillType");
        String skillName = payload.get("skillName");

        if(skillType == null || skillName == null || skillType.trim().isEmpty() || skillName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Pole nie może być puste."));
        }
        if (softSkillRepository.existsBySkillNameIgnoreCase(skillName.trim())) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Taka umiejętność już istnieje."));
        }


        SoftSkills softSkills = new SoftSkills();
        softSkills.setSkillType(skillType);
        softSkills.setSkillName(skillName);

        softSkillRepository.save(softSkills);

        return ResponseEntity.ok(Collections.singletonMap("message", "Umiejętność została dodana"));
    }


    @PostMapping("/soft-skills/saveSingle")
    @ResponseBody
    public ResponseEntity<String> saveSingleSoftSkillLevel(@RequestBody SaveSingleSoftSkillRequestDTO request) {
        softSkillService.saveSingleUserSoftSkillLevel(request.getUserId(), request.getSkillId(), request.getLevel());
        return ResponseEntity.ok("OK");
    }

    private void prepareSoftSkillMatrixData(User user, Model model) throws JsonProcessingException {
        List<SoftSkills> softSkills = softSkillRepository.findAll();
        List<UserSoftSkillLevel> userSoftSkillLevel = softSkillLevelRepository.findByUser(user);

        // Stwórz DTO do JSON-a
        List<UserSoftSkillLevelDTO> userSoftSkillLevelDtos = userSoftSkillLevel.stream()
                .map(level -> new UserSoftSkillLevelDTO(level.getSoftSkills().getId(), level.getLevel()))
                .toList();

        ObjectMapper objectMapper = new ObjectMapper();
        String userSoftSkillsLevelJson = objectMapper.writeValueAsString(userSoftSkillLevelDtos);

        model.addAttribute("softSkills", softSkills);
        model.addAttribute("userSoftSkillsLevelJson", userSoftSkillsLevelJson);
        model.addAttribute("userId", user.getId());
    }



}
