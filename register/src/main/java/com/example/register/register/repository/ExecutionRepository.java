package com.example.register.register.repository;

import com.example.register.register.DTO.DailyUserProcessExecutionDTO;
import com.example.register.register.model.SavedData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExecutionRepository extends JpaRepository<SavedData, Long> {

    @Query("""
    SELECT new com.example.register.register.DTO.DailyUserProcessExecutionDTO(
        e.todaysDate,
        CONCAT(u.firstName, ' ', u.lastName),
        p.processName,
        e.quantity,
        COALESCE(ef.efficiency, 0.0),
        p.id
    )
    FROM SavedData e
    JOIN e.user u
    JOIN u.team t
    JOIN u.section s
    JOIN e.process p
    LEFT JOIN Efficiency ef ON ef.user = u AND ef.todaysDate = e.todaysDate
    WHERE e.todaysDate = :date
      AND t.id = :teamId
      AND (:sectionIds IS NULL OR s.id IN :sectionIds)
      AND (:userIds IS NULL OR u.id IN :userIds)
      AND (:processIds IS NULL OR p.id IN :processIds)
""")
    List<DailyUserProcessExecutionDTO> getUserProcessExecutionsForDateFiltered(
            @Param("date") LocalDate date,
            @Param("teamId") Long teamId,
            @Param("sectionIds") List<Long> sectionIds,
            @Param("userIds") List<Long> userIds,
            @Param("processIds") List<Long> processIds
    );

}
