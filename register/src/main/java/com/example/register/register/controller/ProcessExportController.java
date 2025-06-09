package com.example.register.register.controller;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class ProcessExportController {


    @PostMapping("/export/processes")
    public ResponseEntity<byte[]> exportSelectedProcessesToExcel(@RequestBody List<Map<String, Object>> selectedProcesses) throws IOException {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Processes");
        int rowCount = 0;

        Row headerRow = sheet.createRow(rowCount++);
        headerRow.createCell(0).setCellValue("Nazwa procesu");
        headerRow.createCell(1).setCellValue("Czas w minutach");
        headerRow.createCell(2).setCellValue("Czas w sekundach");
        headerRow.createCell(3).setCellValue("Czy proces nieoperacyjny?");

        for (Map<String, Object> process : selectedProcesses) {
            Row row = sheet.createRow(rowCount++);
            String processName = (String) process.get("processName");
            boolean nonOperational = process.get("nonOperational") != null && Boolean.parseBoolean(process.get("nonOperational").toString());

            // ✅ Pobieramy wartość z JSON-a poprawnie (averageTimeMinutes zamiast averageTime)
            double averageTimeMinutes = process.get("averageTimeMinutes") != null
                    ? Double.parseDouble(process.get("averageTimeMinutes").toString())
                    : 0.0;

            double averageTimeSeconds = process.get("averageTimeSeconds") != null
                    ? Double.parseDouble(process.get("averageTimeSeconds").toString())
                    : (averageTimeMinutes * 60); // Domyślnie przeliczamy sekundy

            row.createCell(0).setCellValue(processName);
            row.createCell(1).setCellValue(averageTimeMinutes);
            row.createCell(2).setCellValue(averageTimeSeconds);
            row.createCell(3).setCellValue(nonOperational ? "TAK" : "NIE");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=filtered_processes.xlsx")
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(outputStream.toByteArray());
    }

}
