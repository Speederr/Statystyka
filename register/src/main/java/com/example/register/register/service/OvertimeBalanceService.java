package com.example.register.register.service;

import com.example.register.register.model.OvertimeBalance;
import com.example.register.register.model.User;
import com.example.register.register.model.VolumeType;
import com.example.register.register.repository.OvertimeBalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class OvertimeBalanceService {

    @Autowired
    private OvertimeBalanceRepository overtimeBalanceRepository;

public void addOvertimeEvent(User user, VolumeType type, int minutes, LocalDate date) {
    if (minutes < 0) {
        minutes = Math.abs(minutes);
    }
    OvertimeBalance ob = new OvertimeBalance();
    ob.setUser(user);
    ob.setVolumeType(type);
    ob.setOvertimeMinutes(minutes); // zawsze dodatnia
    ob.setBalanceDate(date);
    overtimeBalanceRepository.save(ob);
}


}
