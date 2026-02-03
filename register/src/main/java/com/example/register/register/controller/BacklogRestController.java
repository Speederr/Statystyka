package com.example.register.register.controller;

import com.example.register.register.DTO.BacklogProcessWithHoursDto;
import com.example.register.register.model.User;
import com.example.register.register.repository.BacklogRepository;
import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.BacklogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/backlog")
@RequiredArgsConstructor
public class BacklogRestController {

    private final BacklogService backlogService;
    private final UserRepository userRepository;
    private final BacklogRepository backlogRepository;

    @GetMapping("/processes")
    public List<BacklogProcessWithHoursDto> getBacklogProcesses(Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        Long teamId = user.getTeam().getId();
        LocalDate date = backlogRepository.findLatestDateForTeam(teamId).orElse(LocalDate.now());
        return backlogService.getProcessesForBacklogWithHours(teamId, date);
    }


}
