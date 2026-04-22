package com.example.register.register.service;

import com.example.register.register.DTO.BacklogProcessWithHoursDto;
import com.example.register.register.model.Backlog;
import com.example.register.register.model.BusinessProcess;
import com.example.register.register.model.Team;
import com.example.register.register.repository.BacklogRepository;
import com.example.register.register.repository.ProcessRepository;
import com.example.register.register.repository.SavedDataRepository;
import com.example.register.register.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BacklogService {

    @Autowired
    private  BacklogRepository backlogRepository;
    @Autowired
    private  ProcessRepository processRepository;
    @Autowired
    private SavedDataRepository savedDataRepository;
    @Autowired
    private TeamRepository teamRepository;

    @Transactional
    public void saveBacklog(Team team, Map<Long, Integer> backlogData) {
        LocalDate today = LocalDate.now();
        backlogData.forEach((processId, taskCount) -> {
            if (taskCount == 0) return;
            BusinessProcess process = processRepository.findById(processId)
                    .orElseThrow(() -> new RuntimeException("Nie znaleziono procesu o ID: " + processId));
            Optional<Backlog> existing = backlogRepository.findByProcessAndTeamAndDate(process, team, today);
            if (existing.isPresent()) {
                Backlog backlog = existing.get();
                backlog.setTaskCount(taskCount);
                backlogRepository.save(backlog);
            } else {
                Backlog newBacklog = new Backlog();
                newBacklog.setProcess(process);
                newBacklog.setTeam(team); // <--- tutaj ustawiasz team
                newBacklog.setDate(today);
                newBacklog.setTaskCount(taskCount);
                backlogRepository.save(newBacklog);
            }
        });
    }



    public List<Backlog> getBacklogBetweenDates(LocalDate start, LocalDate end) {
        return backlogRepository.findByDateBetween(start, end);
    }

    public Map<Long, Integer> getBacklogForTeam(Long teamId) {
        List<Backlog> backlogList = backlogRepository.findByProcess_Team_Id(teamId);

        Map<Long, Integer> backlogData = new HashMap<>();
        for (Backlog backlog : backlogList) {
            backlogData.put(backlog.getProcess().getId(), backlog.getTaskCount());
        }
        return backlogData;
    }


    public Map<LocalDate, Double> getBacklogByDateForTeam(Long teamId) {
        // Pobieramy backlog tylko dla danego zespołu
        List<Backlog> teamBacklog = backlogRepository.findByTeamId(teamId);

        // Grupujemy wpisy backlogu po dacie i sumujemy liczbę spraw
        return teamBacklog.stream()
                .collect(Collectors.groupingBy(
                        Backlog::getDate,
                        Collectors.summingDouble(
                                b -> b.getTaskCount() * b.getProcess().getAverageTime() / 60.0
                        )
                ));
    }

    public List<BacklogProcessWithHoursDto> getProcessesForBacklogWithHours(Long teamId, LocalDate date) {
        List<Object[]> rows = backlogRepository.findProcessesBacklogWithHours(teamId, date);
        List<BacklogProcessWithHoursDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            String name = (String) row[0];
            int taskCount = ((Number) row[1]).intValue();
            double hours = ((Number) row[2]).doubleValue();
            result.add(new BacklogProcessWithHoursDto(name, taskCount, hours));
        }
        return result;
    }



}
