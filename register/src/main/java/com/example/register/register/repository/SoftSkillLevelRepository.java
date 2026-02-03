package com.example.register.register.repository;

import com.example.register.register.model.SoftSkills;
import com.example.register.register.model.User;
import com.example.register.register.model.UserSoftSkillLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SoftSkillLevelRepository extends JpaRepository<UserSoftSkillLevel, Long > {

    List<UserSoftSkillLevel> findByUser(User user);
    Optional<UserSoftSkillLevel> findByUserAndSoftSkills(User user, SoftSkills softSkills);
}
