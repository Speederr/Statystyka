package com.example.register.register.controller;

import com.example.register.register.model.Position;
import com.example.register.register.repository.PositionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/position")
public class PositionController {

    private final PositionRepository positionRepository;

    public PositionController(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    @GetMapping
    public List<Position> getAllPositions() {
        return positionRepository.findAll();
    }

}
