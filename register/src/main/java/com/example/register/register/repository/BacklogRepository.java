package com.example.register.register.repository;

import com.example.register.register.model.Backlog;
import com.example.register.register.model.BusinessProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BacklogRepository extends JpaRepository<Backlog, Long> {
    Optional<Backlog> findByProcessAndDate(BusinessProcess process, LocalDate date);

    List<Backlog> findByDate(LocalDate date);
}
