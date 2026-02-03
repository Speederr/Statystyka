package com.example.register.register.service;

import com.example.register.register.model.Application;
import com.example.register.register.model.User;
import com.example.register.register.model.UserApplicationLevel;
import com.example.register.register.repository.ApplicationRepository;
import com.example.register.register.repository.UserApplicationLevelRepository;
import com.example.register.register.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final UserApplicationLevelRepository userApplicationLevelRepository;

    public void saveSingleUserAppLevel(Long userId, Long appId, Integer level) {
        User user = userRepository.findById(userId).orElseThrow();
        Application app = applicationRepository.findById(appId).orElseThrow();

        UserApplicationLevel entity = userApplicationLevelRepository.findByUserAndApplication(user, app)
                .orElseGet(() -> {
                    UserApplicationLevel newObj = new UserApplicationLevel();
                    newObj.setUser(user);
                    newObj.setApplication(app);
                    return newObj;
                });
        entity.setLevel(level);
        userApplicationLevelRepository.save(entity);
    }

}
