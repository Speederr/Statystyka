package com.example.register.register.repository;

import com.example.register.register.DTO.OvertimeDTO;
import com.example.register.register.DTO.OvertimeTableDTO;
import com.example.register.register.model.OvertimeBalance;
import com.example.register.register.model.User;
import com.example.register.register.model.VolumeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OvertimeBalanceRepository extends JpaRepository <OvertimeBalance, Long> {
    Optional<OvertimeBalance> findByUserAndVolumeType(User user, VolumeType type);

    @Query("""
      SELECT new com.example.register.register.DTO.OvertimeDTO(
        ob.user.id,
        ob.user.firstName,
        ob.user.lastName,
        ob.volumeType,
        ob.overtimeMinutes,
        ob.user.section.id
      )
      FROM OvertimeBalance ob
      WHERE ob.user.team.id = :teamId
    """)
    List<OvertimeDTO> findCurrentOvertimeByTeamId(@Param("teamId") Long teamId);

    @Query("""
    SELECT new com.example.register.register.DTO.OvertimeTableDTO(
        ob.user.id,
        ob.user.firstName,
        ob.user.lastName,
        SUM(CASE WHEN ob.volumeType = 'OVERTIME_PAID' THEN ob.overtimeMinutes ELSE 0 END),
        SUM(CASE WHEN ob.volumeType = 'OVERTIME_OFF' THEN ob.overtimeMinutes ELSE 0 END),
        SUM(CASE WHEN ob.volumeType IN ('DEDUCT_PARTIAL', 'DEDUCT_FULL_DAY') THEN ob.overtimeMinutes ELSE 0 END),
        ob.user.section.id
    )
    FROM OvertimeBalance ob
    WHERE ob.user.team.id = :teamId
    GROUP BY ob.user.id, ob.user.firstName, ob.user.lastName, ob.user.section.id
""")
    List<OvertimeTableDTO> findTableDataByTeamId(@Param("teamId") Long teamId);


    @Query("""
    SELECT ob FROM OvertimeBalance ob
    WHERE ob.user.id = :userId AND ob.volumeType = :type
""")
    Optional<OvertimeBalance> findByUserIdAndVolumeType(@Param("userId") Long userId, @Param("type") VolumeType type);

}
