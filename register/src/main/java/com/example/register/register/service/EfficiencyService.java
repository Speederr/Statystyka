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

    private final ProcessRepository processRepository;
    private final EfficiencyRepository efficiencyRepository;
    private final UserRepository userRepository;
    private final SavedDataRepository savedDataRepository;

    public EfficiencyService(ProcessRepository processRepository, EfficiencyRepository efficiencyRepository, UserRepository userRepository, SavedDataRepository savedDataRepository) {
        this.processRepository = processRepository;
        this.efficiencyRepository = efficiencyRepository;
        this.userRepository = userRepository;
        this.savedDataRepository = savedDataRepository;
    }

//    @Transactional
//    public void calculateAndSaveEfficiency(Long userId) {
//        LocalDate today = LocalDate.now();
//
//        // 1) Pobierz wszystkie SavedData na dziś
//        List<SavedData> allEntries = savedDataRepository
//                .findByUser_IdAndTodaysDate(userId, today);
//
//        // 2) Numerator: suma czasu na procesy operacyjne
//        double totalTaskTime = allEntries.stream()
//                .filter(e -> !e.getProcess().isNonOperational())
//                .mapToDouble(e -> e.getQuantity() * e.getProcess().getAverageTime())
//                .sum();
//
//        // 3) Czas na procesy nieoperacyjne
//        double totalNonOpTime = allEntries.stream()
//                .filter(e -> e.getProcess().isNonOperational())
//                .mapToDouble(e -> e.getQuantity() * e.getProcess().getAverageTime())
//                .sum();
//
//        // 4) Suma wszystkich zapisanych minut nadgodzin
//        int totalOvertimeMinutes = allEntries.stream()
//                .mapToInt(SavedData::getOvertimeMinutes)
//                .sum();
//
//        // 5) Denominator: 465 + overtime – nonOp
//        final int baseTime = 465;
//        double operationalWindow = Math.max(baseTime + totalOvertimeMinutes - totalNonOpTime, 1);
//
//        // 6) Oblicz procent efektywności
//        double efficiencyPercent = totalTaskTime / operationalWindow * 100;
//        efficiencyPercent = Math.round(efficiencyPercent * 100.0) / 100.0;
//
//        // 7) Upsert rekordu Efficiency
//        Efficiency eff = efficiencyRepository
//                .findByUser_IdAndTodaysDate(userId, today)
//                .orElseGet(() -> {
//                    // stworzenie nowego wpisu
//                    User user = userRepository.findById(userId)
//                            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
//                    Efficiency e = new Efficiency();
//                    e.setUser(user);
//                    e.setTodaysDate(today);
//                    return e;
//                });
//
//        // 8) Ustaw nową wartość i zapisz
//        eff.setEfficiency(efficiencyPercent);
//        efficiencyRepository.save(eff);
//    }
@Transactional
public void calculateAndSaveEfficiency(Long userId) {
    LocalDate today = LocalDate.now();

    // 1) Wszystkie wpisy today
    List<SavedData> allEntries = savedDataRepository
            .findByUser_IdAndTodaysDate(userId, today);

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

    // 6) Częściowe odebranie – odejmujemy bezpośrednio od bazowego 465
    int sumDeduct = allEntries.stream()
            .filter(e -> e.getVolumeType() == VolumeType.DEDUCT_PARTIAL)
            .mapToInt(SavedData::getOvertimeMinutes)
            .sum();

    // 7) Ustal bazowe okno: 465 – odebrane
    final int BASE = 465;
    int baseWindow = Math.max(BASE - sumDeduct, 0);

    // 8) Całkowite okno = (465-deduct) + paid + off – nonOp
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
