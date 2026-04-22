package com.example.register.register.service;

import com.example.register.register.model.*;
import com.example.register.register.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProcessService {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFavoritesRepository userFavoritesRepository;

    @Autowired
    private UserProcessLevelRepository userProcessLevelRepository;

    @Autowired
    private TeamRepository teamRepository;

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

        // 🔹 Pobierz tylko aktywne procesy z przekazanej listy
        List<BusinessProcess> favoriteProcesses = processRepository.findAllById(processIds).stream()
                .filter(BusinessProcess::isActive)
                .toList();

        // 🔹 Usuń stare ulubione procesy użytkownika
        userFavoritesRepository.deleteByUser(user);

        // 🔹 Utwórz nowe ulubione procesy (tylko aktywne)
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
        List<BusinessProcess> processes = processRepository.findByTeamIdAndActiveTrueOrderByProcessNameAsc(teamId);
        log.info("✅ Aktywne procesy znalezione dla teamId {}: {}", teamId, processes.size());
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

    public void updateStatus(Long processId, boolean active) {
        BusinessProcess process = processRepository.findById(processId)
                .orElseThrow(() -> new EntityNotFoundException("Proces nie istnieje."));

        process.setActive(active);
        processRepository.save(process);
    }


    private void setAppUser(String username) {
        // true = lokalnie dla transakcji
        em.createNativeQuery("select set_config('app.user', :u, true)")
                .setParameter("u", username)
                .getSingleResult();
    }

    @Transactional
    public void saveNewProcess(Long teamId, BusinessProcess process, String username) {
        setAppUser(username);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono zespołu: " + teamId));

        process.setTeam(team);
        process.setActive(true);

        processRepository.save(process);

    }

    @Transactional
    public void updateProcess(BusinessProcess updatedProcess, String username) {
        setAppUser(username);

        BusinessProcess process = processRepository.findById(updatedProcess.getId())
                .orElseThrow(() -> new RuntimeException("Process not found"));

        process.setAverageTime(updatedProcess.getAverageTime());
        process.setNonOperational(updatedProcess.isNonOperational());

        processRepository.save(process);

    }


}

