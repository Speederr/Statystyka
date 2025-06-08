package com.example.register.register.service;

import com.example.register.register.DTO.DailyUserProcessExecutionDTO;
import com.example.register.register.repository.ExecutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExecutionChartService {

    @Autowired
    private ExecutionRepository executionRepository;

    public Map<String, Object> buildChartData(LocalDate date, Long teamId, List<Long> sectionIds, List<Long> userIds, List<Long> processIds) {
        List<DailyUserProcessExecutionDTO> rawData = executionRepository
                .getUserProcessExecutionsForDateFiltered(date, teamId, sectionIds, userIds, processIds);


    Set<String> labels = new LinkedHashSet<>();
        Set<String> processNames = new LinkedHashSet<>();

        Map<String, Map<String, Long>> processToDataMap = new LinkedHashMap<>();
        Map<String, Double> efficiencyMap = new LinkedHashMap<>();
        Map<String, Long> processNameToId = new HashMap<>();

        for (DailyUserProcessExecutionDTO dto : rawData) {
            String labelKey = dto.getFullName() + " | " + dto.getDate();
            labels.add(labelKey);
            processNames.add(dto.getProcessName());

            processToDataMap.putIfAbsent(dto.getProcessName(), new LinkedHashMap<>());
            Map<String, Long> dateMap = processToDataMap.get(dto.getProcessName());
            processNameToId.put(dto.getProcessName(), dto.getProcessId());

            dateMap.put(labelKey, dto.getQuantity());

            efficiencyMap.put(labelKey, dto.getEfficiency());
        }

        List<Map<String, Object>> datasets = new ArrayList<>();
        String[] colors = {
                "rgba(251, 177, 189, 0.65)",  // #FBB1BD
                "rgba(178, 230, 162, 0.65)",  // #B2E6A2
                "rgba(188, 180, 248, 0.65)",  // #BCB4F8
                "rgba(207, 242, 241, 0.65)",  // #CFF2F1
                "rgba(249, 219, 155, 0.65)",  // #F9DB9B
                "rgba(255, 154, 162, 0.65)",  // #FF9AA2
                "rgba(164, 221, 237, 0.65)",  // #A4DDED
                "rgba(193, 200, 228, 0.65)",  // #C1C8E4
                "rgba(255, 218, 193, 0.65)",  // #FFDAC1
                "rgba(226, 240, 203, 0.65)"   // #E2F0CB
        };

        int colorIndex = 0;
        for (String process : processNames) {
            Map<String, Long> dataForProcess = processToDataMap.getOrDefault(process, Map.of());

            List<Long> data = labels.stream()
                    .map(label -> {
                        Long qty = dataForProcess.get(label);
                        return (qty != null && qty > 0) ? qty : null;
                    })
                    .toList();

            boolean allNull = data.stream().allMatch(Objects::isNull);
            if (allNull) continue;

            Map<String, Object> dataset = new HashMap<>();
            dataset.put("label", process);
            dataset.put("data", data);
            dataset.put("backgroundColor", colors[colorIndex % colors.length]);
            dataset.put("stack", "stack1");
            dataset.put("processId", processNameToId.get(process)); // ✅ NOWE
            datasets.add(dataset);
            colorIndex++;
        }
        // Efektywność jako linia
        Map<String, Object> lineDataset = new HashMap<>();
        lineDataset.put("label", "Efektywność (%)");
        List<Double> efficiencyData = labels.stream()
                .map(label -> Math.round(efficiencyMap.getOrDefault(label, 0.0) * 100.0) / 100.0)
                .toList();
        lineDataset.put("data", efficiencyData);
        lineDataset.put("type", "line");
        lineDataset.put("borderColor", "#f06c9b");
        lineDataset.put("borderWidth", 2);
        lineDataset.put("tension", 0.4);
        lineDataset.put("yAxisID", "y1");
        lineDataset.put("fill", false);
        datasets.add(lineDataset);

        Map<String, Object> result = new HashMap<>();
        result.put("labels", new ArrayList<>(labels));
        result.put("datasets", datasets);

        return result;
    }
}
