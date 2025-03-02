package com.example.register.register.service;

import com.example.register.register.model.BusinessProcess;
import com.example.register.register.model.User;
import com.example.register.register.model.UserFavorites;
import com.example.register.register.repository.ProcessRepository;
import com.example.register.register.repository.UserFavoritesRepository;
import com.example.register.register.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProcessService {
    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFavoritesRepository userFavoritesRepository;

    public List<BusinessProcess> getAllProcesses() {
        return processRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(BusinessProcess::getProcessName)) // Sortowanie alfabetyczne
                .collect(Collectors.toList());
    }

    public List<BusinessProcess> getFavoriteProcesses(Long userId) {
        List<BusinessProcess> favorites = processRepository.findFavoritesByUserId(userId)
                .stream()
                .sorted(Comparator.comparing(BusinessProcess::getProcessName))
                .collect(Collectors.toList());

        System.out.println("Ulubione procesy dla userId " + userId + ": " + favorites);
        return favorites;
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





}

