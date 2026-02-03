package com.example.register.register.repository;

import com.example.register.register.model.Section;
import com.example.register.register.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByTeamId(Long teamId);

    boolean existsBySectionNameIgnoreCaseAndTeam(String sectionName, Team team);

}
