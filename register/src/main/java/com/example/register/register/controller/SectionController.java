package com.example.register.register.controller;

import com.example.register.register.DTO.SectionDto;
import com.example.register.register.model.Attendance;
import com.example.register.register.model.Section;
import com.example.register.register.model.Team;
import com.example.register.register.model.User;
import com.example.register.register.repository.AttendanceRepository;
import com.example.register.register.repository.SectionRepository;
import com.example.register.register.repository.TeamRepository;
import com.example.register.register.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/api/sections")
public class SectionController {

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;


    @GetMapping("/{teamId}")
    public List<Section> getSectionByTeam(@PathVariable Long teamId) {
        return sectionRepository.findByTeamId(teamId);
    }

    @PostMapping("/saveNewSection")
    public ResponseEntity<?> createSection(@RequestBody Map<String, Object> payload) {
        Object teamIdObj = payload.get("teamId");
        Object sectionNameObj = payload.get("sectionName");

        if(teamIdObj == null || sectionNameObj == null ) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Wszystkie pola są wymagane!"));
        }
        long teamId;
        try {
            teamId = Long.parseLong(teamIdObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Nieprawidłowy ID zespołu."));
        }
        String sectionName = sectionNameObj.toString().trim();
        if(sectionName.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Nazwa sekcji jest wymagana!"));
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono zespołu o ID: " + teamId));

        if(sectionRepository.existsBySectionNameIgnoreCaseAndTeam(sectionName.trim(), team)) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Sekcja już istnieje!"));
        }

        Section newSection = new Section();
        newSection.setSectionName(sectionName);
        newSection.setTeam(team);

        sectionRepository.save(newSection);

        return ResponseEntity.ok(Collections.singletonMap("message", "Sekcja została dodana."));
    }

    @GetMapping
    public ResponseEntity<List<Section>> getSectionsForUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Pobranie zalogowanego użytkownika
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Użytkownik nie znaleziony"));

        // Pobranie sekcji związanych z zespołem użytkownika
        List<Section> sections = sectionRepository.findByTeamId(user.getTeam().getId());

        return ResponseEntity.ok(sections);
    }

    @GetMapping("/by-team/{teamId}")
    public ResponseEntity<List<SectionDto>> getSectionsByTeam(@PathVariable Long teamId) {
        List<SectionDto> sections = sectionRepository.findByTeamId(teamId).stream()
                .map(section -> new SectionDto(section.getId(), section.getSectionName()))
                .toList();

        return ResponseEntity.ok(sections);
    }

    @GetMapping("/availability/{sectionId}")
    public ResponseEntity<Map<String, Object>> getAvailabilityForSection(
            @PathVariable String sectionId, Principal principal) {

        LocalDate today = LocalDate.now(); // 📅 Bieżąca data

        List<User> allEmployees;
        List<User> presentEmployees;
        List<User> onLeaveEmployees;
        List<User> notLoggedEmployees;
        List<User> officeEmployees;
        List<User> homeofficeEmployees;

        long totalEmployees;

        // 🟡 Pobieramy obecności według statusów
        List<Attendance> todaysLeaves = attendanceRepository.findByAttendanceDateAndStatus(today, "leave");
        List<Attendance> todaysNotLogged = attendanceRepository.findByAttendanceDateAndStatus(today, "notloggedin");
        List<Attendance> todaysPresent = attendanceRepository.findByAttendanceDateAndStatus(today, "present");

        Set<Long> onLeaveIds = todaysLeaves.stream().map(a -> a.getUser().getId()).collect(Collectors.toSet());
        Set<Long> notLoggedIds = todaysNotLogged.stream().map(a -> a.getUser().getId()).collect(Collectors.toSet());
        Set<Long> officeIds = todaysPresent.stream().filter(a -> "office".equalsIgnoreCase(a.getWorkMode())).map(a -> a.getUser().getId()).collect(Collectors.toSet());
        Set<Long> homeofficeIds = todaysPresent.stream().filter(a -> "homeoffice".equalsIgnoreCase(a.getWorkMode())).map(a -> a.getUser().getId()).collect(Collectors.toSet());

        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nie znaleziono użytkownika"));

        if ("all".equalsIgnoreCase(sectionId)) {
            if (currentUser.getTeam() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Użytkownik nie jest przypisany do żadnego zespołu"));
            }

            Long teamId = currentUser.getTeam().getId();

            allEmployees = userRepository.findAllEmployees().stream()
                    .filter(u -> u.getTeam() != null && u.getTeam().getId().equals(teamId))
                    .toList();
        } else {
            long id;
            try {
                id = Long.parseLong(sectionId);
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Nieprawidłowe sectionId"));
            }


            allEmployees = userRepository.findAllEmployees().stream()
                    .filter(u -> u.getSection() != null && u.getSection().getId().equals(id))
                    .toList();
        }

        totalEmployees = allEmployees.size();

        // 📦 Przypisanie do grup
        presentEmployees = allEmployees.stream()
                .filter(u -> !onLeaveIds.contains(u.getId()) && !notLoggedIds.contains(u.getId()))
                .toList();

        onLeaveEmployees = allEmployees.stream()
                .filter(u -> onLeaveIds.contains(u.getId()))
                .toList();

        notLoggedEmployees = allEmployees.stream()
                .filter(u -> notLoggedIds.contains(u.getId()))
                .toList();

        officeEmployees = allEmployees.stream()
                .filter(u -> officeIds.contains(u.getId()))
                .toList();

        homeofficeEmployees = allEmployees.stream()
                .filter(u -> homeofficeIds.contains(u.getId()))
                .toList();

        log.info("✅ Obecnych: {}, Nieobecnych: {}, Niezalogowanych: {}, Biuro: {}, Zdalnie: {}, Całkowita liczba: {}", presentEmployees.size(), onLeaveEmployees.size(), notLoggedEmployees.size(), officeEmployees.size(), homeofficeEmployees.size(), totalEmployees);

        Map<String, Object> response = new HashMap<>();
        response.put("presentCount", presentEmployees.size());
        response.put("onLeaveCount", onLeaveEmployees.size());
        response.put("notLoggedCount", notLoggedEmployees.size());
        response.put("officeCount", officeEmployees.size());
        response.put("homeofficeCount", homeofficeEmployees.size());

        response.put("presentEmployees", presentEmployees.stream().map(u -> u.getFirstName() + " " + u.getLastName()).toList());
        response.put("onLeaveEmployees", onLeaveEmployees.stream().map(u -> u.getFirstName() + " " + u.getLastName()).toList());
        response.put("notLoggedEmployees", notLoggedEmployees.stream().map(u -> u.getFirstName() + " " + u.getLastName()).toList());
        response.put("officeEmployees", officeEmployees.stream().map(u -> u.getFirstName() + " " + u.getLastName()).toList());
        response.put("homeofficeEmployees", homeofficeEmployees.stream().map(u -> u.getFirstName() + " " + u.getLastName()).toList());

        response.put("totalEmployees", totalEmployees);
        response.put("date", today.toString());

        return ResponseEntity.ok(response);
    }








}
