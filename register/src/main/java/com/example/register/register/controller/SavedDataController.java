package com.example.register.register.controller;

import com.example.register.register.DTO.MixedChartDTO;
import com.example.register.register.DTO.SavedDataDto;
import com.example.register.register.model.*;
import com.example.register.register.repository.ProcessRepository;
import com.example.register.register.repository.SavedDataRepository;
import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.SavedDataService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/saved-data")
public class SavedDataController {

    @Autowired
    private SavedDataService savedDataService;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SavedDataRepository savedDataRepository;

    // 🔵 1️⃣ Główny zapis dla całej listy procesów (przycisk "Zapisz")
    @PostMapping("/save")
    public String saveData(@RequestBody List<SavedData> dataList) {
        for (SavedData data : dataList) {
            System.out.println("🔍 Otrzymane dane: " + data);

            // ✅ Pobieranie użytkownika
            if (data.getUser() == null) {
                if (data.getUserId() == null) {
                    throw new RuntimeException("❌ user_id is missing in request payload!");
                }

                User user = userRepository.findById(data.getUserId())
                        .orElseThrow(() -> new RuntimeException("❌ User not found with ID: " + data.getUserId()));

                data.setUser(user);
            }

            // ✅ Pobieranie procesu
            if (data.getProcess() == null) {
                if (data.getProcessId() == null) {
                    throw new RuntimeException("❌ process_id is missing in request payload!");
                }

                BusinessProcess process = processRepository.findById(data.getProcessId())
                        .orElseThrow(() -> new RuntimeException("❌ Process not found with ID: " + data.getProcessId()));

                data.setProcess(process);
            }

            data.setTodaysDate(LocalDate.now());
        }

        savedDataService.saveData(dataList);
        return "✅ Dane zostały zapisane!";
    }

    // 🔵 2️⃣ Zapis dla pojedynczego procesu (kliknięcie `+`)
    @PostMapping("/save-single")
    public String saveSingleData(@RequestParam Long userId, @RequestParam Long processId, @RequestParam Long quantity) {
        System.out.println("🔍 Zapis pojedynczego procesu: user=" + userId + ", process=" + processId + ", quantity=" + quantity);

        // ✅ Pobieranie użytkownika
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("❌ User not found with ID: " + userId));

        // ✅ Pobieranie procesu
        BusinessProcess process = processRepository.findById(processId)
                .orElseThrow(() -> new RuntimeException("❌ Process not found with ID: " + processId));

        // ✅ Tworzenie nowego wpisu
        SavedData savedData = new SavedData();
        savedData.setUser(user);
        savedData.setProcess(process);
        savedData.setQuantity(quantity);
        savedData.setTodaysDate(LocalDate.now());

        // ✅ Zapis pojedynczego wpisu
        savedDataService.saveData(List.of(savedData));

        return "✅ Dane zapisane dla pojedynczego procesu!";
    }

    @GetMapping("/get-report")
    public List<SavedDataDto> getSummaryData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return savedDataRepository.findAll().stream()
                .filter(data -> {
                    LocalDate dataDate = data.getTodaysDate();

                    boolean afterStart = (startDate == null) || !dataDate.isBefore(startDate);
                    boolean beforeEnd = (endDate == null) || !dataDate.isAfter(endDate);

                    return afterStart && beforeEnd;
                })
                .collect(Collectors.groupingBy(
                        data -> new SummaryKey(
                                data.getProcess().getProcessName(),
                                data.getTodaysDate(),
                                data.getUser().getUsername()
                        ),
                        Collectors.summingLong(SavedData::getQuantity)
                ))
                .entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().todaysDate()))
                .map(entry -> new SavedDataDto(
                        entry.getKey().processName(),
                        entry.getValue(),
                        entry.getKey().todaysDate(),
                        entry.getKey().username()
                ))
                .toList();
    }

    @GetMapping("/get-report/export")
    public void exportToXlsx(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response
    ) throws IOException {
        List<SavedDataDto> filteredData = getSummaryData(startDate, endDate); // użyj tej samej metody

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=raport.xlsx");

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Raport");

        // ✅ Nagłówki
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Proces");
        header.createCell(1).setCellValue("Ilość");
        header.createCell(2).setCellValue("Data dodania");
        header.createCell(3).setCellValue("Pracownik");

        // ✅ Dane
        int rowNum = 1;
        for (SavedDataDto data : filteredData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(data.getProcess());
            row.createCell(1).setCellValue(data.getQuantity());
            row.createCell(2).setCellValue(data.getDate().toString());
            row.createCell(3).setCellValue(data.getEmployee());
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }


    @GetMapping("/summary/{userId}")
//    public List<DailySummaryDTO> getChartData(@PathVariable Long userId) {
//        return savedDataService.getSummaryForUser(userId);
//    }
    public List<MixedChartDTO> getMixedChartData(@PathVariable Long userId) {
        return savedDataService.getMixedChartForUser(userId);
    }



}






