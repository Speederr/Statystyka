package com.example.register.register.repository;

import com.example.register.register.model.Section;
import com.example.register.register.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByTeamId(Long teamId);

    Optional<Section> findBySectionName(String sectionName);

    boolean existsBySectionNameIgnoreCaseAndTeam(String sectionName, Team team);

}
