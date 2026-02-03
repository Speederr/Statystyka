package com.example.register.register.repository;

import com.example.register.register.DTO.OvertimeDTO;
import com.example.register.register.DTO.OvertimeDetailDTO;
import com.example.register.register.DTO.OvertimeTableDTO;
import com.example.register.register.model.SavedData;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OvertimeRepository extends JpaRepository<SavedData, Long> {

    @Query("""
  SELECT new com.example.register.register.DTO.OvertimeDTO(
    u.id,
    u.firstName,
    u.lastName,
    sd.volumeType,
    SUM(sd.overtimeMinutes),
    u.section.id
  )
  FROM SavedData sd
  JOIN sd.user u
  WHERE u.team.id = :teamId
    AND sd.volumeType <> com.example.register.register.model.VolumeType.BASIC
    AND sd.volumeType <> com.example.register.register.model.VolumeType.DEDUCT_FULL_DAY
  GROUP BY u.id, u.firstName, u.lastName, sd.volumeType, u.section.id
""")
    List<OvertimeDTO> findOvertimeSummariesByTeamId(@Param("teamId") Long teamId);



    @Query("""
    SELECT new com.example.register.register.DTO.OvertimeDetailDTO(
        sd.process.processName,
        sd.quantity,
        sd.todaysDate,
        CONCAT(sd.user.firstName, ' ', sd.user.lastName),
        sd.volumeType,
        sd.overtimeMinutes
    )
    FROM SavedData sd
    WHERE sd.user.id = :userId
      AND sd.volumeType <> com.example.register.register.model.VolumeType.BASIC
      AND sd.todaysDate BETWEEN :startDate AND :endDate
    ORDER BY sd.todaysDate
""")
    List<OvertimeDetailDTO> findDetailsForUserAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Modifying
    @Transactional
    @Query("UPDATE OvertimeBalance ob SET ob.overtimeMinutes = 0, ob.balanceDate = CURRENT_DATE " +
            "WHERE ob.user.id = :userId AND ob.volumeType = 'OVERTIME_PAID'")
    void updatePaidOvertimeToZero(@Param("userId") Long userId);


}
