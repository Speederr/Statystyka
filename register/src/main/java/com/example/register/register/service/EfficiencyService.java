package com.example.register.register.service;

import com.example.register.register.model.Efficiency;
import com.example.register.register.model.User;
import com.example.register.register.model.VolumeType;
import com.example.register.register.repository.*;
import com.example.register.register.model.SavedData;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EfficiencyService {

    private final EfficiencyRepository efficiencyRepository;
    private final UserRepository userRepository;
    private final SavedDataRepository savedDataRepository;

    public EfficiencyService(EfficiencyRepository efficiencyRepository, UserRepository userRepository, SavedDataRepository savedDataRepository) {
        this.efficiencyRepository = efficiencyRepository;
        this.userRepository = userRepository;
        this.savedDataRepository = savedDataRepository;
    }

@Transactional
public void calculateAndSaveEfficiency(Long userId) {
    LocalDate today = LocalDate.now();

    // 1) Wszystkie wpisy today
    List<SavedData> allEntries = savedDataRepository
            .findByUser_IdAndTodaysDate(userId, today);

    // Dodaj obsługę odbioru całego dnia – NAJPIERW!
    final int BASE = 435;
    int sumDeductFullDay = allEntries.stream()
            .filter(e -> e.getVolumeType() == VolumeType.DEDUCT_FULL_DAY)
            .mapToInt(SavedData::getOvertimeMinutes)
            .sum();

    if (sumDeductFullDay >= BASE) {
        // Cały dzień odebrany – efektywność = 0%
        Efficiency eff = efficiencyRepository
                .findByUser_IdAndTodaysDate(userId, today)
                .orElseGet(() -> {
                    User u = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
                    Efficiency ne = new Efficiency();
                    ne.setUser(u);
                    ne.setTodaysDate(today);
                    return ne;
                });
        eff.setEfficiency(0.0);
        efficiencyRepository.save(eff);
        return;
    }

    // Dalej normalne liczenie efektywności:
    // 2) Czas operacyjny (taski)
    double totalTaskTime = allEntries.stream()
            .filter(e -> !e.getProcess().isNonOperational())
            .mapToDouble(e -> e.getQuantity() * e.getProcess().getAverageTime())
            .sum();

    // 3) Czas przerw (non‐operational)
    double totalNonOpTime = allEntries.stream()
            .filter(e -> e.getProcess().isNonOperational())
            .mapToDouble(e -> e.getQuantity() * e.getProcess().getAverageTime())
            .sum();

    // 4) Nadgodziny płatne – zawsze dodajemy
    int sumPaid = allEntries.stream()
            .filter(e -> e.getVolumeType() == VolumeType.OVERTIME_PAID)
            .mapToInt(SavedData::getOvertimeMinutes)
            .sum();

    // 5) Nadgodziny do odbioru – też dodajemy do okna
    int sumOff = allEntries.stream()
            .filter(e -> e.getVolumeType() == VolumeType.OVERTIME_OFF)
            .mapToInt(SavedData::getOvertimeMinutes)
            .sum();

    // 6) Częściowe odebranie – odejmujemy bezpośrednio od bazowego
    int sumDeduct = allEntries.stream()
            .filter(e -> e.getVolumeType() == VolumeType.DEDUCT_PARTIAL)
            .mapToInt(SavedData::getOvertimeMinutes)
            .sum();

    int baseWindow = Math.max(BASE - sumDeduct, 0);

    // 8) Całkowite okno = (435-deduct) + paid + off – nonOp
    double operationalWindow = Math.max(baseWindow + sumPaid + sumOff - totalNonOpTime, 1);

    // 9) % efektywności
    double efficiencyPercent = totalTaskTime / operationalWindow * 100;
    efficiencyPercent = Math.round(efficiencyPercent * 100.0) / 100.0;

    // 10) Upsert do tabeli efficiency
    Efficiency eff = efficiencyRepository
            .findByUser_IdAndTodaysDate(userId, today)
            .orElseGet(() -> {
                User u = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found: " + userId));
                Efficiency ne = new Efficiency();
                ne.setUser(u);
                ne.setTodaysDate(today);
                return ne;
            });
    eff.setEfficiency(efficiencyPercent);
    efficiencyRepository.save(eff);
}

}
