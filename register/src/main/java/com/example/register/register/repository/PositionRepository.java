package com.example.register.register.repository;

import com.example.register.register.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    @Override
    @NonNull
    Optional<Position> findById(@NonNull Long id);

    Optional<Position> findByPositionName(String positionName);


    boolean existsByPositionNameIgnoreCase(String positionName);

}
