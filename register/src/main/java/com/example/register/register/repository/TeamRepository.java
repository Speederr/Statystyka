package com.example.register.register.repository;

import com.example.register.register.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    @Override
    @NonNull
    Optional<Team> findById(@NonNull Long id);

    Optional<Team> findByTeamName(String teamName);

    boolean existsByTeamNameIgnoreCase(String teamName);
}
