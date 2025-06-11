package com.example.register.register.service;

import com.example.register.register.DTO.OvertimeDTO;
import com.example.register.register.DTO.OvertimeDetailDTO;
import com.example.register.register.DTO.OvertimeTableDTO;
import com.example.register.register.model.OvertimePayoutHistory;
import com.example.register.register.model.SavedData;
import com.example.register.register.model.VolumeType;
import com.example.register.register.repository.OvertimeBalanceRepository;
import com.example.register.register.repository.OvertimePayoutHistoryRepository;
import com.example.register.register.repository.OvertimeRepository;
import com.example.register.register.repository.SavedDataRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
public class OvertimeService {

    @Autowired
    private OvertimeRepository overtimeRepository;
    @Autowired
    private SavedDataRepository savedDataRepository;
    @Autowired
    private OvertimePayoutHistoryRepository overtimePayoutHistoryRepository;
    @Autowired
    private OvertimeBalanceRepository overtimeBalanceRepository;

//    public List<OvertimeDTO> getOvertimeSummary(Long teamId) {
//        return overtimeBalanceRepository.findCurrentOvertimeByTeamId(teamId);
//    }
    public List<OvertimeTableDTO> getOvertimeTableForTeam(Long teamId) {
        return overtimeBalanceRepository.findTableDataByTeamId(teamId);
    }

    public List<OvertimeDetailDTO> getDetailsForUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return overtimeRepository.findDetailsForUserAndDateRange(userId, startDate, endDate);
    }

    public void resetPaidOvertime(Long userId) {
        overtimeRepository.updatePaidOvertimeToZero(userId);
    }

    public int getPaidOvertimeForUser(Long userId) {
        return savedDataRepository.getPaidOvertimeForUser(userId);
    }

    public void savePayoutHistory(Long userId, int minutes, String admin, String note) {
        OvertimePayoutHistory history = new OvertimePayoutHistory();
        history.setUserId(userId);
        history.setPayoutMinutes(minutes);
        history.setPayoutDate(LocalDate.now());
        history.setHandledBy(admin);
        history.setNote(note);

        overtimePayoutHistoryRepository.save(history);
    }


}
