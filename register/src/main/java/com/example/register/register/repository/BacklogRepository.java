package com.example.register.register.repository;

import com.example.register.register.model.Backlog;
import com.example.register.register.model.BusinessProcess;

import com.example.register.register.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BacklogRepository extends JpaRepository<Backlog, Long> {
    Optional<Backlog> findByProcessAndDate(BusinessProcess process, LocalDate date);
    Optional<Backlog> findByProcessAndTeamAndDate(BusinessProcess process, Team team, LocalDate date);

    List<Backlog> findByDateBetween(LocalDate start, LocalDate end);
    List<Backlog> findByProcess_Team_Id(Long teamId);

    @Query("SELECT b FROM Backlog b WHERE b.process.team.id = :teamId")
    List<Backlog> findByTeamId(Long teamId);

    @Query("SELECT SUM(b.taskCount * bp.averageTime / 60.0) FROM Backlog b " +
            "JOIN b.process bp WHERE bp.team.id = :teamId AND b.date = " +
            "(SELECT MAX(b2.date) FROM Backlog b2 WHERE b2.process.team.id = :teamId)")
    Double findLatestTeamBacklogHours(@Param("teamId") Long teamId);

    @Query(value = """
    SELECT p.process_name, b.task_count
    FROM backlog b
    JOIN processes p ON b.process_id = p.id
    WHERE b.date = :date
      AND b.task_count > 0
      AND b.team_id = :teamId
  """, nativeQuery = true)
    List<Object[]> findSimpleProcessesBacklog(@Param("teamId") Long teamId, @Param("date") LocalDate date);


    @Query(
            value = "SELECT MAX(date) FROM backlog WHERE team_id = :teamId",
            nativeQuery = true
    )
    Optional<LocalDate> findLatestDateForTeam(@Param("teamId") Long teamId);


    @Query(
            value = """
    SELECT p.process_name, b.task_count, (b.task_count * p.average_time / 60.0) AS hours
    FROM backlog b
    JOIN processes p ON b.process_id = p.id
    WHERE b.date = :date
      AND b.task_count > 0
      AND b.team_id = :teamId
  """, nativeQuery = true)
    List<Object[]> findProcessesBacklogWithHours(@Param("teamId") Long teamId, @Param("date") LocalDate date);

}
