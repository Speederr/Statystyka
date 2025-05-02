package com.example.register.register.controller;

import com.example.register.register.model.Team;
import com.example.register.register.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamRepository teamRepository;

    // 🔹 Endpoint do pobierania wszystkich zespołów
    @GetMapping
    public ResponseEntity<List<Team>> getAllTeams() {
        List<Team> teams = teamRepository.findAll();
        return ResponseEntity.ok(teams);
    }


    @PostMapping("/saveNewTeam")
    public ResponseEntity<Team> saveNewTeam(@RequestBody Team team) {
        Team savedTeam = teamRepository.save(team);
        return ResponseEntity.ok(savedTeam); // Zwracamy JSON
    }
}
