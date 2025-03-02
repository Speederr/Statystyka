package com.example.register.register.controller;

import com.example.register.register.model.BusinessProcess;
import com.example.register.register.service.ProcessService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
public class ProcessExportController {

    private final ProcessService processService;

    public ProcessExportController(ProcessService processService) {
        this.processService = processService;
    }


    @GetMapping("/export/processes")
    public ResponseEntity<byte[]> exportProcessesToExcel() throws IOException {
        List<BusinessProcess> processes = processService.getAllProcesses();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Processes");
        int rowCount = 0;

        Row headerRow = sheet.createRow(rowCount++);
        headerRow.createCell(0).setCellValue("Nazwa procesu");
        headerRow.createCell(1).setCellValue("Czas");

        for(BusinessProcess businessProcess : processes) {
            Row row = sheet.createRow(rowCount++);
            row.createCell(0).setCellValue(businessProcess.getProcessName());
            row.createCell(1).setCellValue(businessProcess.getAverageTime());
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=processes.xlsx")
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(outputStream.toByteArray());

    }
}
