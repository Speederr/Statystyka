package com.example.register.register.controller;

import com.example.register.register.DTO.BacklogExportDto;
import com.example.register.register.model.Backlog;
import com.example.register.register.model.BusinessProcess;
import com.example.register.register.model.User;
import com.example.register.register.repository.ProcessRepository;
import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.BacklogService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/backlog")
public class BacklogController {

    private final BacklogService backlogService;
    private final ProcessRepository processRepository;
    private final UserRepository userRepository;

    @Autowired
    public BacklogController(BacklogService backlogService, ProcessRepository processRepository, UserRepository userRepository) {
        this.backlogService = backlogService;
        this.processRepository = processRepository;
        this.userRepository = userRepository;
    }

@GetMapping
public String showBacklogForm(Model model, Principal principal) {
    if (principal == null) {
        return "redirect:/login";
    }

    String username = principal.getName();
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika: " + username));

    Long teamId = user.getTeam().getId();
    List<BusinessProcess> processes = processRepository.findByTeamId(teamId);
    Map<Long, Integer> backlogData = backlogService.getBacklogForTeam(teamId);

    Map<Long, Double> backlogHoursData = new HashMap<>();
    for (BusinessProcess process : processes) {
        int taskCount = backlogData.getOrDefault(process.getId(), 0);
        double backlogHours = taskCount * process.getAverageTime();
        backlogHoursData.put(process.getId(), backlogHours);
    }

    model.addAttribute("processes", processes);
    model.addAttribute("backlogData", backlogData);
    model.addAttribute("backlogHoursData", backlogHoursData);

    return "backlog";
}

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<Map<String, String>> saveBacklog(@RequestParam Map<String, String> backlogData) {
        Map<Long, Integer> backlogMap = backlogData.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("process_"))
                .collect(Collectors.toMap(
                        entry -> Long.parseLong(entry.getKey().replace("process_", "")),
                        entry -> entry.getValue().isEmpty() ? 0 : Integer.parseInt(entry.getValue())
                ));

        backlogService.saveBacklog(backlogMap);

        // ✅ Zwrot JSON jako odpowiedź
        Map<String, String> response = new HashMap<>();
        response.put("message", "Backlog został zapisany!");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/hours")
    public ResponseEntity<Map<LocalDate, Double>> getBacklogHours(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika: " + username));

        Long teamId = user.getTeam().getId();
        Map<LocalDate, Double> backlogHoursData = backlogService.getBacklogByDateForTeam(teamId);

        return ResponseEntity.ok(backlogHoursData);
    }

    @GetMapping("/export")
    public void exportBacklogToXlsx(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Principal principal,
            HttpServletResponse response
    ) throws IOException {

        String username = principal.getName();
        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty() || optionalUser.get().getTeam() == null) {
            response.sendRedirect("/error");
            return;
        }

        Long teamId = optionalUser.get().getTeam().getId();

        LocalDate start = (startDate != null) ? startDate : LocalDate.now().minusDays(6);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();

        // ⏬ Pobierz dane backlogu z serwisu
        List<Backlog> backlogData = backlogService.getBacklogBetweenDates(start, end).stream()
                .filter(b -> b.getProcess().getTeam().getId().equals(teamId))
                .toList();

        // Można to przekształcić na DTO jeśli wolisz (opcjonalnie)
        List<BacklogExportDto> exportData = backlogData.stream()
                .map(b -> new BacklogExportDto(
                        b.getProcess().getProcessName(),
                        b.getDate(),
                        b.getTaskCount()
                ))
                .toList();

        // 🧾 Przygotowanie pliku XLSX
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=backlog.xlsx");

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Backlog");

        // ✅ Nagłówki
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Proces");
        header.createCell(1).setCellValue("Data");
        header.createCell(2).setCellValue("Liczba zadań");

        int rowNum = 1;
        for (BacklogExportDto entry : exportData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getProcess());
            row.createCell(1).setCellValue(entry.getDate().toString());
            row.createCell(2).setCellValue(entry.getQuantity());
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }



}




