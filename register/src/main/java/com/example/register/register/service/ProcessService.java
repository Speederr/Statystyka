package com.example.register.register.service;

import com.example.register.register.model.BusinessProcess;
import com.example.register.register.model.User;
import com.example.register.register.model.UserFavorites;
import com.example.register.register.model.UserProcessLevel;
import com.example.register.register.repository.ProcessRepository;
import com.example.register.register.repository.UserFavoritesRepository;
import com.example.register.register.repository.UserProcessLevelRepository;
import com.example.register.register.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProcessService {
    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFavoritesRepository userFavoritesRepository;

    @Autowired
    private UserProcessLevelRepository userProcessLevelRepository;

    public List<BusinessProcess> getAllProcesses() {
        return processRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(BusinessProcess::getProcessName)) // Sortowanie alfabetyczne
                .collect(Collectors.toList());
    }

    public List<BusinessProcess> getFavoriteProcesses(Long userId) {
        return processRepository.findFavoritesByUserId(userId)
                .stream()
                .sorted(Comparator.comparing(BusinessProcess::getProcessName))
                .collect(Collectors.toList());
    }


@Transactional
public void saveFavoriteProcesses(Long userId, List<Long> processIds) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));

    List<BusinessProcess> favoriteProcesses = processRepository.findAllById(processIds);

    // 🔹 Usuń stare ulubione procesy użytkownika
    userFavoritesRepository.deleteByUser(user);

    // 🔹 Utwórz nowe ulubione procesy
    Set<UserFavorites> favoriteProcessesSet = favoriteProcesses.stream()
            .map(process -> {
                UserFavorites uf = new UserFavorites();
                uf.setUser(user);
                uf.setProcess(process);
                return uf;
            })
            .collect(Collectors.toSet());

    // 🔹 Zapisz nowe ulubione procesy
    userFavoritesRepository.saveAll(favoriteProcessesSet);
}

    public List<BusinessProcess> getProcessesByTeamId(Long teamId) {
        List<BusinessProcess> processes = processRepository.findByTeamId(teamId);
        log.info("Procesy znalezione dla teamId {}: {}", teamId, processes.size());
        return processes;
    }


    @Transactional
    public void saveSingleUserLevel(Long userId, Long processId, Integer level) {
        // Usuń stary poziom dla danego procesu (jeśli istnieje)
        userProcessLevelRepository.deleteByUserIdAndProcessId(userId, processId);

        // Pobierz użytkownika
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Pobierz proces
        BusinessProcess process = processRepository.findById(processId)
                .orElseThrow(() -> new RuntimeException("Process not found"));

        // Stwórz nowy wpis
        UserProcessLevel userProcessLevel = new UserProcessLevel();
        userProcessLevel.setUser(user);
        userProcessLevel.setProcess(process);
        userProcessLevel.setLevel(level);

        // Zapisz do bazy
        userProcessLevelRepository.save(userProcessLevel);
    }



}

