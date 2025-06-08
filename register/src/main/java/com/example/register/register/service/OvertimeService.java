package com.example.register.register.service;

import com.example.register.register.DTO.OvertimeDTO;
import com.example.register.register.DTO.OvertimeDetailDTO;
import com.example.register.register.repository.OvertimeRepository;
import com.example.register.register.repository.SavedDataRepository;
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


    public List<OvertimeDTO> getOvertimeSummary() {
        return overtimeRepository.findOvertimeSummaries();
    }


    public List<OvertimeDetailDTO> getDetailsForUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return overtimeRepository.findDetailsForUserAndDateRange(userId, startDate, endDate);
    }




}
