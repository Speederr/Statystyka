package com.example.register.register.controller;

import com.example.register.register.DTO.*;
import com.example.register.register.model.User;
import com.example.register.register.repository.OvertimePayoutHistoryRepository;
import com.example.register.register.repository.SavedDataRepository;
import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.OvertimeService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;


@RestController
@RequestMapping("/api/overtime")
public class OvertimeController {

    @Autowired
    private OvertimeService overtimeService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SavedDataRepository savedDataRepository;
    @Autowired
    private OvertimePayoutHistoryRepository overtimePayoutHistoryRepository;

    @GetMapping("/get-all-overtime")
    public ResponseEntity<List<OvertimeTableDTO>> getAllOvertime(Principal principal) {
        String username = principal.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Użytkownik nie znaleziony."));

        Long teamId = user.getTeam().getId();
        List<OvertimeTableDTO> list = overtimeService.getOvertimeTableForTeam(teamId);

        return ResponseEntity.ok(list);
    }

    @GetMapping("/get-data")
    public List<OvertimeDetailDTO> getAllOvertimeData(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateOvertime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateOvertime,
            Principal principal,
            Authentication authentication
    ) {
        Long resolvedUserId = resolveAllowedUserId(userId, principal, authentication);

        LocalDate today = LocalDate.now();
        LocalDate effectiveEndDate = (endDateOvertime != null) ? endDateOvertime : today;
        LocalDate effectiveStartDate = (startDateOvertime != null) ? startDateOvertime : effectiveEndDate.minusDays(6);

        return overtimeService.getDetailsForUserAndDateRange(resolvedUserId, effectiveStartDate, effectiveEndDate);
    }


    @GetMapping("/{userId}/details")
    public List<OvertimeDetailDTO> getUserOvertimeDetails(@PathVariable Long userId, Principal principal, Authentication authentication) {
        Long resolvedUserId = resolveAllowedUserId(userId, principal, authentication);
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);

        return overtimeService.getDetailsForUserAndDateRange(resolvedUserId, startDate, today);
    }

    @PostMapping("/exportAll")
    public void exportOvertimeToXlsx(@RequestBody List<OvertimeExportDto> exportData, HttpServletResponse response) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Nadgodziny_podsumowanie.xlsx");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Podsumowanie");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Imię");
            header.createCell(1).setCellValue("Nazwisko");
            header.createCell(2).setCellValue("Nadgodziny płatne(min)");
            header.createCell(3).setCellValue("Nadgodziny do odbioru(min)");

            int rowNum = 1;
            for (OvertimeExportDto entry : exportData) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.firstName());
                row.createCell(1).setCellValue(entry.lastName());
                row.createCell(2).setCellValue(entry.overtimePaid());
                row.createCell(3).setCellValue(entry.overtimeOff());

            }

            try (ServletOutputStream out = response.getOutputStream()) {
                workbook.write(out);
                response.flushBuffer();
            }
        } catch (IOException e) {
            // nie próbuj pisać do response jeśli już otwarty
            System.err.println("❌ Błąd eksportu do Excela: " + e.getMessage());
        }
    }


    @PostMapping("/exportOvertimeForDate")
    public void exportOvertimeForDate(@RequestBody List<SavedDataDto> filteredData, HttpServletResponse response) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Nadgodziny_szczegoly.xlsx");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Szczegóły");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Proces");
            header.createCell(1).setCellValue("Ilość");
            header.createCell(2).setCellValue("Data dodania");
            header.createCell(3).setCellValue("Pracownik");
            header.createCell(4).setCellValue("Rodzaj czasu pracy");
            header.createCell(5).setCellValue("Czas(min)");

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

            try (ServletOutputStream out = response.getOutputStream()) {
                workbook.write(out);
                response.flushBuffer();
            }
        } catch (IOException e) {
            System.err.println("❌ Błąd eksportu do Excela: " + e.getMessage());
        }
    }

    @PostMapping("/archive-paid")
    @Transactional
    public ResponseEntity<String> archivePaidOvertime(@RequestBody List<Long> userIds,
                                                      @RequestParam(required = false) String note,
                                                      Principal principal) {
        String adminName = principal.getName();

        userIds.forEach(userId -> {
            overtimeService.archivePaidOvertime(userId, adminName, note);
            overtimeService.resetPaidOvertime(userId);
        });

        return ResponseEntity.ok("Zarchiwizowano nadgodziny dla wybranych użytkowników.");
    }


    @GetMapping("/archive")
    public List<OvertimePayoutHistoryDTO> getAllOvertimeHistory(Principal principal) {
        return overtimeService.getAll(principal);
    }

    private Long resolveAllowedUserId(Long requestedUserId, Principal principal, Authentication authentication) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Użytkownik nie znaleziony."));

        boolean elevatedRole = authentication.getAuthorities().stream().anyMatch(authority ->
                "ROLE_ADMIN".equals(authority.getAuthority())
                        || "ROLE_MANAGER".equals(authority.getAuthority())
                        || "ROLE_COORDINATOR".equals(authority.getAuthority())
        );

        Long effectiveUserId = requestedUserId != null ? requestedUserId : currentUser.getId();

        if (!elevatedRole) {
            if (requestedUserId != null && !requestedUserId.equals(currentUser.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Brak dostępu do danych innego użytkownika.");
            }
            return currentUser.getId();
        }

        if (effectiveUserId.equals(currentUser.getId())) {
            return effectiveUserId;
        }

        User targetUser = userRepository.findById(effectiveUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono użytkownika."));

        Long currentTeamId = currentUser.getTeam() != null ? currentUser.getTeam().getId() : null;
        Long targetTeamId = targetUser.getTeam() != null ? targetUser.getTeam().getId() : null;
        if (!Objects.equals(currentTeamId, targetTeamId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Brak dostępu do użytkownika spoza zespołu.");
        }

        return effectiveUserId;
    }



}
