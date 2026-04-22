package com.example.register.register.service;

import com.example.register.register.model.SoftSkills;
import com.example.register.register.model.User;
import com.example.register.register.model.UserSoftSkillLevel;
import com.example.register.register.repository.SoftSkillLevelRepository;
import com.example.register.register.repository.SoftSkillRepository;
import com.example.register.register.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SoftSkillService {

    private final UserRepository userRepository;
    private final SoftSkillRepository softSkillRepository;
    private final SoftSkillLevelRepository softSkillLevelRepository;

    public void saveSingleUserSoftSkillLevel(Long userId, Long skillId, Integer level) {
        User user = userRepository.findById(userId).orElseThrow();
        SoftSkills softSkills = softSkillRepository.findById(skillId).orElseThrow();

        UserSoftSkillLevel entity = softSkillLevelRepository.findByUserAndSoftSkills(user, softSkills)
                .orElseGet(() -> {
                    UserSoftSkillLevel newObj = new UserSoftSkillLevel();
                    newObj.setUser(user);
                    newObj.setSoftSkills(softSkills);
                    return newObj;
                });
        entity.setLevel(level);
        softSkillLevelRepository.save(entity);
    }



}
