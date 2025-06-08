package com.example.register.register.controller;

import com.example.register.register.DTO.OvertimeDTO;
import com.example.register.register.DTO.OvertimeDetailDTO;
import com.example.register.register.DTO.OvertimeExportDto;
import com.example.register.register.DTO.SavedDataDto;
import com.example.register.register.model.User;
import com.example.register.register.repository.SavedDataRepository;
import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.OvertimeService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/overtime")
public class OvertimeController {

    @Autowired
    private OvertimeService overtimeService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SavedDataRepository savedDataRepository;

    @GetMapping("/get-all-overtime")
    public ResponseEntity<List<OvertimeDTO>> getAllOvertime() {
       List<OvertimeDTO> list = overtimeService.getOvertimeSummary();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/get-data")
    public List<OvertimeDetailDTO> getAllOvertimeData(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateOvertime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateOvertime,
            Principal principal
    ) {

        if (userId == null) {
            String username = principal.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Użytkownik nie znaleziony"));
            userId = user.getId();
        }

        LocalDate today = LocalDate.now();
        LocalDate effectiveEndDate = (endDateOvertime != null) ? endDateOvertime : today;
        LocalDate effectiveStartDate = (startDateOvertime != null) ? startDateOvertime : effectiveEndDate.minusDays(6);

        return overtimeService.getDetailsForUserAndDateRange(userId, effectiveStartDate, effectiveEndDate);
    }


    @GetMapping("/{userId}/details")
    public List<OvertimeDetailDTO> getUserOvertimeDetails(@PathVariable Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);

        return overtimeService.getDetailsForUserAndDateRange(userId, startDate, today);
    }

    @PostMapping("/exportAll")
    public void exportOvertimeToXlsx(@RequestBody List<OvertimeExportDto> exportData, HttpServletResponse response) throws IOException {

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Nadgodziny podsumowanie.xlsx");

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Podsumowanie");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Imię");
        header.createCell(1).setCellValue("Nazwisko");
        header.createCell(2).setCellValue("Nadgodziny płatne");
        header.createCell(3).setCellValue("Nadgodziny do odbioru");
        header.createCell(4).setCellValue("Odebrane");

        int rowNum = 1;
        for (OvertimeExportDto entry : exportData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.firstName());
            row.createCell(1).setCellValue(entry.lastName());
            row.createCell(2).setCellValue(entry.overtimePaid());
            row.createCell(3).setCellValue(entry.overtimeOff());
            row.createCell(4).setCellValue(entry.deductPartial());
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @PostMapping("/exportOvertimeForDate")
    public void exportOvertimeForDate(@RequestBody List<SavedDataDto> filteredData, HttpServletResponse response) throws IOException {

        // 🔽 Konfiguracja odpowiedzi
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Nadgodziny_szczegoly.xlsx");

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Szczegóły");

        // ✅ Nagłówki
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Proces");
        header.createCell(1).setCellValue("Ilość");
        header.createCell(2).setCellValue("Data dodania");
        header.createCell(3).setCellValue("Pracownik");
        header.createCell(4).setCellValue("Rodzaj czasu pracy");
        header.createCell(5).setCellValue("Czas(min)");

        // ✅ Dane
        int rowNum = 1;
        for (SavedDataDto data : filteredData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(data.processName());
            row.createCell(1).setCellValue(data.quantity());
            row.createCell(2).setCellValue(data.todaysDate().toString());
            row.createCell(3).setCellValue(data.username());
            row.createCell(4).setCellValue(data.volumeType());
            row.createCell(5).setCellValue(data.overtimeMinutes());
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }


}
