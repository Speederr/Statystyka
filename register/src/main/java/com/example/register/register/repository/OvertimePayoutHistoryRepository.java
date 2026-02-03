package com.example.register.register.repository;

import com.example.register.register.DTO.OvertimePayoutHistoryDTO;
import com.example.register.register.model.OvertimePayoutHistory;
import com.example.register.register.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface OvertimePayoutHistoryRepository extends JpaRepository<OvertimePayoutHistory, Long> {

    @Query("""
    SELECT new com.example.register.register.DTO.OvertimePayoutHistoryDTO(
        h.id,
        u.firstName,
        u.lastName,
        h.payoutMinutes,
        h.payoutDate,
        CONCAT(hu.firstName, ' ', hu.lastName),
        h.note
    )
    FROM OvertimePayoutHistory h
    JOIN User u ON u.id = h.userId
    LEFT JOIN User hu ON hu.username = h.handledBy
    WHERE u.team.id = :teamId
    ORDER BY h.payoutDate DESC
""")
    List<OvertimePayoutHistoryDTO> findAllWithUserNamesByTeamId(@Param("teamId") Long teamId);

}
