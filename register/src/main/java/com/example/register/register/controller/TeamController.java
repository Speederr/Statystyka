package com.example.register.register.controller;

import com.example.register.register.DTO.TeamDto;
import com.example.register.register.model.Team;
import com.example.register.register.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamRepository teamRepository;

    // Endpoint do pobierania wszystkich zespołów
    @GetMapping
    public ResponseEntity<List<TeamDto>> getAllTeams() {
        List<TeamDto> teams = teamRepository.findAll().stream()
                .map(team -> new TeamDto(team.getId(), team.getTeamName()))
                .toList();

        return ResponseEntity.ok(teams);
    }


    @PostMapping("/saveNewTeam")
    public ResponseEntity<?> createNewTeam(@RequestBody NewTeamDTO newTeamDTO) {
        String name = newTeamDTO.teamName;
        if(name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Nazwa zespołu jest wymagana!"));
        }
        if(teamRepository.existsByTeamNameIgnoreCase(name.trim())) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Zespół już istnieje!"));
        }
        Team team = new Team();
        team.setTeamName(name.trim());
        teamRepository.save(team);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Collections.singletonMap("success", "Pomyślnie dodano zespół."));
    }

    public record NewTeamDTO(String teamName) {}
}
