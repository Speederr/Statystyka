package com.example.register.register.controller;

import com.example.register.register.model.Section;
import com.example.register.register.model.Team;
import com.example.register.register.repository.SectionRepository;
import com.example.register.register.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sections")
public class SectionController {

    @Autowired
    private final SectionRepository sectionRepository;

    @Autowired
    private final TeamRepository teamRepository;

    public SectionController(SectionRepository sectionRepository, TeamRepository teamRepository) {
        this.sectionRepository = sectionRepository;
        this.teamRepository = teamRepository;
    }

    @GetMapping("/{teamId}")
    public List<Section> getSectionByTeam(@PathVariable Long teamId) {
        return sectionRepository.findByTeamId(teamId);
    }

    @PostMapping("/saveNewSection")
    public ResponseEntity<?> createSection(@RequestBody Map<String, Object> payload) {
        Long teamId = Long.parseLong(payload.get("teamId").toString());
        String sectionName = payload.get("sectionName").toString();

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono zespołu o ID: " + teamId));

        Section newSection = new Section();
        newSection.setSectionName(sectionName);
        newSection.setTeam(team);

        sectionRepository.save(newSection);

        return ResponseEntity.ok(Collections.singletonMap("message", "Sekcja została dodana."));
    }
}
