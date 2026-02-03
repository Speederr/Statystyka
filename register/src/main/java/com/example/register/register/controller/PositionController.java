package com.example.register.register.controller;

import com.example.register.register.DTO.PositionDTO;
import com.example.register.register.model.Position;
import com.example.register.register.model.User;
import com.example.register.register.repository.PositionRepository;
import com.example.register.register.repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("/api/position")
@RequiredArgsConstructor
public class PositionController {

    private final PositionRepository positionRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<PositionDTO> getAllPositions() {
        return positionRepository.findAll()
                .stream()
                .map(pos -> new PositionDTO(pos.getId(), pos.getPositionName()))
                .toList();
    }

    @PostMapping("/addNewPosition")
    public ResponseEntity<?> createNewPosition(@RequestBody PositionDTO positionDTO) {
        String name = positionDTO.getPositionName();
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Nazwa stanowiska jest wymagana!"));
        }

        // Sprawdź czy już istnieje (case-insensitive)
        if (positionRepository.existsByPositionNameIgnoreCase(name.trim())) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Stanowisko już istnieje!"));
        }

        Position position = new Position();
        position.setPositionName(name.trim());
        positionRepository.save(position);

        return ResponseEntity.ok(Collections.singletonMap("message", "Pomyślnie dodano nowe stanowisko"));
    }


    @PostMapping("/update-position")
    public ResponseEntity<String> updateUserPosition(
            @RequestParam Long userId,
            @RequestParam Long positionId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Position pos = positionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Position not found"));

        user.setPosition(pos);
        userRepository.save(user);

        return ResponseEntity.ok("Stanowisko zaktualizowane!");
    }

}
