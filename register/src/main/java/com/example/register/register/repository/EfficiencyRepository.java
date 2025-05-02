package com.example.register.register.repository;

import com.example.register.register.model.Efficiency;
import com.example.register.register.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EfficiencyRepository extends JpaRepository<Efficiency, Long> {

    List<Efficiency> findByUserId(Long userId);

    @Query("SELECT e FROM Efficiency e WHERE e.user = :user AND e.todaysDate = :date")
    List<Efficiency> findAllByUserAndTodaysDate(@Param("user") User user, @Param("date") LocalDate date);

    @Query("SELECT e FROM Efficiency e WHERE e.user.section.id = :sectionId AND e.todaysDate = :date")
    List<Efficiency> findAllBySectionIdAndDate(@Param("sectionId") Long sectionId, @Param("date") LocalDate date);

    @Query("SELECT e FROM Efficiency e WHERE e.user.team.id = :teamId AND e.todaysDate = :date")
    List<Efficiency> findAllByTeamIdAndDate(@Param("teamId") Long teamId, @Param("date") LocalDate date);

    @Query("SELECT e FROM Efficiency e WHERE e.todaysDate = :date")
    List<Efficiency> findAllByDate(@Param("date") LocalDate date);

    @Query("SELECT AVG(e.efficiency) FROM Efficiency e WHERE e.user.id = :userId")
    Double findAverageEfficiencyByUserId(@Param("userId") Long userId);
}
