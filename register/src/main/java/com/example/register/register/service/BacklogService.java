package com.example.register.register.service;

import com.example.register.register.model.Backlog;
import com.example.register.register.model.BusinessProcess;
import com.example.register.register.repository.BacklogRepository;
import com.example.register.register.repository.ProcessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BacklogService {

    private final BacklogRepository backlogRepository;
    private final ProcessRepository processRepository;

    @Autowired
    public BacklogService(BacklogRepository backlogRepository, ProcessRepository processRepository) {
        this.backlogRepository = backlogRepository;
        this.processRepository = processRepository;
    }

    public List<Backlog> getBacklogForDate(LocalDate date) {
        return backlogRepository.findByDate(date);
    }

    @Transactional
    public void saveBacklog(Map<Long, Integer> backlogData) {
        LocalDate today = LocalDate.now();

        backlogData.forEach((processId, taskCount) -> {
            // 🔹 Pomijamy zapisywanie, jeśli wartość backlogu to 0
            if (taskCount == 0) {
                return;
            }

            BusinessProcess process = processRepository.findById(processId)
                    .orElseThrow(() -> new RuntimeException("Nie znaleziono procesu o ID: " + processId));

            Optional<Backlog> existingBacklog = backlogRepository.findByProcessAndDate(process, today);

            Backlog backlog = existingBacklog.orElse(new Backlog());
            backlog.setProcess(process);
            backlog.setDate(today);
            backlog.setTaskCount(taskCount);

            backlogRepository.save(backlog);
        });
    }


}
