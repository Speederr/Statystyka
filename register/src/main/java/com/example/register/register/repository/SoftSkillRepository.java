package com.example.register.register.repository;

import com.example.register.register.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoftSkillRepository extends JpaRepository<SoftSkills, Long> {

    boolean existsBySkillNameIgnoreCase(String skillName);
}
