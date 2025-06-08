package com.example.register.register.service;

import com.example.register.register.DTO.BacklogExportDto;
import com.example.register.register.model.Backlog;
import com.example.register.register.model.BusinessProcess;
import com.example.register.register.model.SavedData;
import com.example.register.register.repository.BacklogRepository;
import com.example.register.register.repository.ProcessRepository;
import com.example.register.register.repository.SavedDataRepository;
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


    @Transactional
    public void saveBacklog(Map<Long, Integer> backlogData) {
        LocalDate today = LocalDate.now();

        backlogData.forEach((processId, taskCount) -> {
            if (taskCount == 0) {
                return; // Ignorujemy wartości 0
            }

            BusinessProcess process = processRepository.findById(processId)
                    .orElseThrow(() -> new RuntimeException("Nie znaleziono procesu o ID: " + processId));

            // 🔹 Pobranie backlogu dla danego procesu i daty
            Optional<Backlog> existingBacklog = backlogRepository.findByProcessAndDate(process, today);

            if (existingBacklog.isPresent()) {
                // 🔹 Jeśli backlog już istnieje → nadpisujemy wartość
                Backlog backlog = existingBacklog.get();
                backlog.setTaskCount(taskCount);
                backlogRepository.save(backlog);
            } else {
                // 🔹 Jeśli backlog dla tej daty nie istnieje → tworzymy nowy wpis
                Backlog newBacklog = new Backlog();
                newBacklog.setProcess(process);
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

}
