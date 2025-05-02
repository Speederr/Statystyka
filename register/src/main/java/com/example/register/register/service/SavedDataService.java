package com.example.register.register.service;

import com.example.register.register.DTO.MixedChartDTO;
import com.example.register.register.model.*;
import com.example.register.register.repository.EfficiencyRepository;
import com.example.register.register.repository.ProcessRepository;
import com.example.register.register.repository.SavedDataRepository;
import com.example.register.register.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SavedDataService {

    @Autowired
    private SavedDataRepository savedDataRepository;

    @Autowired
    private EfficiencyRepository efficiencyRepository;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private UserRepository userRepository;

    public void saveData(List<SavedData> dataList) {
        if (dataList.isEmpty()) {
            throw new RuntimeException("❌ Lista danych jest pusta!");
        }

        User user = dataList.get(0).getUser();
        LocalDate todaysDate = LocalDate.now();

        // 🔹 1️⃣ Zapisanie danych do `saved_data`
        savedDataRepository.saveAll(dataList);

        // 🔹 2️⃣ Pobranie wpisów efektywności (powinien być JEDEN)
        List<Efficiency> efficiencies = efficiencyRepository.findAllByUserAndTodaysDate(user, todaysDate);

        Efficiency efficiency;
        if (efficiencies.isEmpty()) {
            // 🔹 Jeśli brak wpisu, tworzymy nowy
            efficiency = new Efficiency();
            efficiency.setUser(user);
            efficiency.setTodaysDate(todaysDate);
            efficiency.setEfficiency(0.0);
        } else if (efficiencies.size() == 1) {
            // 🔹 Jeśli jest JEDEN wpis, aktualizujemy go
            efficiency = efficiencies.get(0);
        } else {
            // 🔹 Jeśli są duplikaty, usuwamy wszystkie poza pierwszym
            System.out.println("⚠️ Usuwanie duplikatów efektywności dla użytkownika " + user.getId());
            for (int i = 1; i < efficiencies.size(); i++) {
                efficiencyRepository.delete(efficiencies.get(i));
            }
            efficiency = efficiencies.get(0);
        }

        // 🔹 3️⃣ Pobranie wszystkich zapisanych ilości dla użytkownika i daty
        Long totalQuantity = savedDataRepository.sumQuantityByUserAndDate(user, todaysDate);
        if (totalQuantity == null) totalQuantity = 0L; // Zapobiega `NullPointerException`

        // 🔹 4️⃣ Obliczenie nowej efektywności
        double newEfficiency = calculateEfficiency(user, todaysDate, totalQuantity);

        // 🔹 5️⃣ Aktualizacja efektywności
        efficiency.setEfficiency(newEfficiency);

        // 🔹 6️⃣ Zapisanie nowej wartości do bazy danych (UPDATE zamiast INSERT)
        efficiencyRepository.save(efficiency);
    }


    /**
    * ✅ Oblicza efektywność dla użytkownika i danego dnia
    */
    private double calculateEfficiency(User user, LocalDate todaysDate, Long totalQuantity) {
    if (totalQuantity == null || totalQuantity == 0) {
        return 0.0;
    }

    // 🔹 Pobranie listy procesów wraz z ich czasami operacyjnymi
    Map<Long, Double> processTimes = processRepository.findAll().stream()
            .collect(Collectors.toMap(BusinessProcess::getId, BusinessProcess::getAverageTime));

    // 🔹 Pobranie wszystkich zapisanych danych użytkownika
    List<SavedData> savedDataList = savedDataRepository.findByUserAndTodaysDate(user, todaysDate);

    // 🔹 Obliczenie całkowitego czasu operacyjnego
    double totalOperationalTime = savedDataList.stream()
            .filter(sd -> !sd.getProcess().isNonOperational()) // Tylko operacyjne
            .mapToDouble(sd -> sd.getQuantity() * processTimes.getOrDefault(sd.getProcess().getId(), 0.0))
            .sum();

    // 🔹 Obliczenie całkowitego czasu nieoperacyjnego
    double totalNonOperationalTime = savedDataList.stream()
            .filter(sd -> sd.getProcess().isNonOperational()) // Tylko nieoperacyjne
            .mapToDouble(sd -> sd.getQuantity() * processTimes.getOrDefault(sd.getProcess().getId(), 0.0))
            .sum();

    // 🔹 Czas operacyjny uwzględniający nieoperacyjne zadania (max 1 min, żeby uniknąć dzielenia przez 0)
    double operationalTime = Math.max(465 - totalNonOperationalTime, 1);

    // 🔹 Wyliczenie efektywności
    double efficiency = (totalOperationalTime / operationalTime) * 100;
    return Math.round(efficiency * 100.0) / 100.0; // Zaokrąglenie do 2 miejsc po przecinku
    }

//    public List<DailySummaryDTO> getSummaryForUser(Long userId) {
//        return savedDataRepository.getStackedChartData(userId).stream()
//                .map(obj -> new DailySummaryDTO(
//                        (LocalDate) obj[0],
//                        (String) obj[1],
//                        (Long) obj[2]
//                ))
//                .toList();
//    }

    public List<MixedChartDTO> getMixedChartForUser(Long userId) {
        // Dane z saved_data
        List<Object[]> barData = savedDataRepository.getStackedChartData(userId);
        // Dane z efficiency
        List<Efficiency> efficiencyList = efficiencyRepository.findByUserId(userId);

        // Mapowanie efektywności: data → wartość
        Map<LocalDate, Double> efficiencyMap = efficiencyList.stream()
                .collect(Collectors.toMap(Efficiency::getTodaysDate, Efficiency::getEfficiency));

        return barData.stream()
                .map(obj -> {
                    LocalDate date = (LocalDate) obj[0];
                    String processName = (String) obj[1];
                    Long quantity = (Long) obj[2];
                    Double efficiency = efficiencyMap.getOrDefault(date, null);

                    return new MixedChartDTO(date, processName, quantity, efficiency);
                })
                .toList();
    }



}