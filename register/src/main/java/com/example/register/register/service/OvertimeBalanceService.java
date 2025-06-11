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

    public void addOrUpdateBalance(User user, VolumeType type, int minutes) {
        OvertimeBalance balance = overtimeBalanceRepository.findByUserAndVolumeType(user, type)
                .orElseGet(() -> {
                    OvertimeBalance ob = new OvertimeBalance();
                    ob.setUser(user);
                    ob.setVolumeType(type);
                    ob.setOvertimeMinutes(0);
                    ob.setBalanceDate(LocalDate.now());
                    return ob;
                });

        // Sprawdź, czy to typ "dodający" czy "odejmujący"
        int newValue = balance.getOvertimeMinutes();

        if (type == VolumeType.DEDUCT_PARTIAL || type == VolumeType.DEDUCT_FULL_DAY) {
            newValue -= minutes; // ODEJMUJEMY odebrane godziny!
        } else {
            newValue += minutes; // DODAJEMY jeśli przyznajemy nadgodziny
        }

        balance.setOvertimeMinutes(newValue);
        balance.setBalanceDate(LocalDate.now());
        overtimeBalanceRepository.save(balance);
    }


}
