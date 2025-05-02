package com.example.register.register.repository;

import com.example.register.register.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    Optional<Position> findById(Long id);

}
