package com.example.register.register.controller;

import com.example.register.register.DTO.*;
import com.example.register.register.model.*;
import com.example.register.register.repository.ProcessRepository;
import com.example.register.register.repository.SavedDataRepository;
import com.example.register.register.repository.OvertimeBalanceRepository;
import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.EfficiencyService;
import com.example.register.register.service.OvertimeBalanceService;
import com.example.register.register.service.SavedDataService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/saved-data")
public class SavedDataController {

    private final SavedDataService savedDataService;
    private final ProcessRepository processRepository;
    private final UserRepository userRepository;
    private final SavedDataRepository savedDataRepository;
    private final EfficiencyService efficiencyService;
    private final OvertimeBalanceService overtimeBalanceService;
    private final OvertimeBalanceRepository overtimeBalanceRepository;

    @PostMapping("/save")
    @Transactional
    public String saveData(@RequestBody List<SavedData> dataList) {
        LocalDate today = LocalDate.now();

        boolean deductPartialHandled = false;
        boolean overtimeOffHandled = false;
        boolean overtimePaidHandled = false;

        for (SavedData data : dataList) {
            // —————————————— wstawianie Usera ——————————————
            if (data.getUser() == null) {
                if (data.getUserId() == null) {
                    throw new RuntimeException("user_id is missing in request payload!");
                }
                User user = userRepository.findById(data.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found with ID: " + data.getUserId()));
                data.setUser(user);
            }
            // —————————————— wstawianie Processu ——————————————
            if (data.getProcess() == null && data.getProcessId() != null) {
                BusinessProcess proc = processRepository.findById(data.getProcessId())
                        .orElseThrow(() -> new RuntimeException("Process not found with ID: " + data.getProcessId()));
                data.setProcess(proc);
            }
            LocalDate effectiveDate = (data.getTodaysDate() != null) ? data.getTodaysDate() : today;
            data.setTodaysDate(effectiveDate);

            if (data.getVolumeType() == null) {
                data.setVolumeType(VolumeType.BASIC);
            }
            if (data.getOvertimeMinutes() == null) {
                data.setOvertimeMinutes(0);
            }
            if (data.getVolumeType() == VolumeType.DEDUCT_PARTIAL) {
                if (!deductPartialHandled) {
                    deductPartialHandled = true;
                } else {
                    data.setOvertimeMinutes(0);
                }
            }
            if (data.getVolumeType() == VolumeType.OVERTIME_OFF) {
                if (!overtimeOffHandled) {
                    overtimeOffHandled = true;
                } else {
                    data.setOvertimeMinutes(0);
                }
            }
            if (data.getVolumeType() == VolumeType.OVERTIME_PAID) {
                if(!overtimePaidHandled) {
                    overtimePaidHandled = true;
                } else {
                    data.setOvertimeMinutes(0);
                }
            }
            if (
                    data.getVolumeType() == VolumeType.OVERTIME_PAID ||
                            data.getVolumeType() == VolumeType.OVERTIME_OFF ||
                            data.getVolumeType() == VolumeType.DEDUCT_PARTIAL ||
                            data.getVolumeType() == VolumeType.DEDUCT_FULL_DAY
            ) {
                if(data.getOvertimeMinutes() != null && data.getOvertimeMinutes() > 0) {
                    overtimeBalanceService.addOvertimeEvent(
                            data.getUser(),
                            data.getVolumeType(),
                            data.getOvertimeMinutes(),
                            data.getTodaysDate()
                    );
                }
            }
        }
        // zapisujemy wszystkie rekordy
        savedDataService.saveData(dataList);
        return "Dane zostały zapisane!";
    }


    //Zapis dla pojedynczego procesu (kliknięcie `+`)
    @PostMapping("/save-single")
    public ResponseEntity<String> saveSingleData(@RequestBody SingleSaveDTO req) {
        // 1) Tworzenie encji SavedData
        SavedData data = toEntity(req);

        // 2) zapis nadgodzin do tabeli overtimeBalance
        if (
                data.getVolumeType() == VolumeType.OVERTIME_PAID ||
                        data.getVolumeType() == VolumeType.OVERTIME_OFF ||
                        data.getVolumeType() == VolumeType.DEDUCT_PARTIAL ||
                        data.getVolumeType() == VolumeType.DEDUCT_FULL_DAY
        ) {
            if (data.getOvertimeMinutes() != null && data.getOvertimeMinutes() > 0) {
                overtimeBalanceService.addOvertimeEvent(
                        data.getUser(),
                        data.getVolumeType(),
                        data.getOvertimeMinutes(),
                        data.getTodaysDate()
                );
            }
        }
        // 3) Zapis wolumenu (zawsze new)
        savedDataService.saveData(List.of(data));

        // 4) Automatyczne przeliczenie efektywności (upsert w tabeli Efficiency)
        efficiencyService.calculateAndSaveEfficiency(req.getUserId(), req.getTodaysDate());

        return ResponseEntity.ok("Wolumen zapisany i efektywność zaktualizowana!");
    }

    private SavedData toEntity(SingleSaveDTO req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + req.getUserId()));
        BusinessProcess proc = processRepository.findById(req.getProcessId())
                .orElseThrow(() -> new RuntimeException("Process not found: " + req.getProcessId()));

        SavedData sd = new SavedData();
        sd.setUser(user);
        sd.setProcess(proc);
        sd.setQuantity(req.getQuantity());
        sd.setTodaysDate(req.getTodaysDate() != null ? req.getTodaysDate() : LocalDate.now());
        sd.setVolumeType(req.getVolumeType());
        sd.setOvertimeMinutes(req.getOvertimeMinutes() == null ? 0 : req.getOvertimeMinutes());
        return sd;
    }

    @GetMapping("/get-report")
    public List<SavedDataDto> getSummaryData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Principal principal
    ) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("U?ytkownik nie znaleziony"));
        Long teamId = user.getTeam().getId();

        List<SavedData> teamData = savedDataRepository.findByUser_Team_Id(teamId);
        return buildSummaryData(teamData, startDate, endDate);
    }

    @GetMapping("/get-report/my")
    public List<SavedDataDto> getMySummaryData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Principal principal
    ) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Użytkownik nie znaleziony"));

        List<SavedData> userData = savedDataRepository.findByUser_Id(user.getId());
        return buildSummaryData(userData, startDate, endDate);
    }

    private List<SavedDataDto> buildSummaryData(
            List<SavedData> sourceData,
            LocalDate startDate,
            LocalDate endDate
    ) {
        LocalDate today             = LocalDate.now();
        LocalDate effectiveEndDate  = (endDate != null) ? endDate : today;
        LocalDate effectiveStartDate= (startDate != null) ? startDate : effectiveEndDate.minusDays(6);

        Map<SummaryKey, List<SavedData>> grouped = sourceData.stream()
                .filter(d -> d.getProcess() != null)
                .filter(d -> {
                    LocalDate dt = d.getTodaysDate();
                    return !dt.isBefore(effectiveStartDate) && !dt.isAfter(effectiveEndDate);
                })
                .collect(Collectors.groupingBy(d ->
                        new SummaryKey(
                                d.getProcess().getProcessName(),
                                d.getTodaysDate(),
                                d.getUser().getUsername(),
                                d.getVolumeType().getLabel()
                        )
                ));

        return grouped.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().todaysDate()))
                .map(e -> {
                    SummaryKey key = e.getKey();
                    List<SavedData> list = e.getValue();

                    long totalQuantity = list.stream()
                            .mapToLong(SavedData::getQuantity)
                            .sum();

                    int totalOvertime = list.stream()
                            .mapToInt(SavedData::getOvertimeMinutes)
                            .sum();

                    SavedData any = list.getFirst();
                    String fullName = any.getUser().getFirstName() + " " + any.getUser().getLastName();

                    return new SavedDataDto(
                            key.processName(),
                            totalQuantity,
                            key.todaysDate(),
                            fullName,
                            key.volumeType(),
                            totalOvertime
                    );
                })
                .toList();
    }

    @GetMapping("/get-report/export")
    public void exportToXlsx(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) List<String> processes,
            @RequestParam(required = false) List<String> users,
            Principal principal,
            HttpServletResponse response
    ) throws IOException {
        List<SavedDataDto> filteredData = getSummaryData(startDate, endDate, principal);

        // 🔽 Filtrowanie po procesach i pracownikach (jeśli przesłano)
        if (processes != null && !processes.isEmpty()) {
            filteredData = filteredData.stream()
                    .filter(dto -> processes.contains(dto.todaysDate().toString()))
                    .toList();
        }

        if (users != null && !users.isEmpty()) {
            filteredData = filteredData.stream()
                    .filter(dto -> users.contains(dto.username()))
                    .toList();
        }

        // 🔽 Konfiguracja odpowiedzi
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

    @GetMapping("/get-report/my/export")
    public void exportMyToXlsx(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) List<String> processes,
            @RequestParam(required = false) List<String> users,
            Principal principal,
            HttpServletResponse response
    ) throws IOException {
        List<SavedDataDto> filteredData = getMySummaryData(startDate, endDate, principal);

        if (processes != null && !processes.isEmpty()) {
            filteredData = filteredData.stream()
                    .filter(dto -> processes.contains(dto.todaysDate().toString()))
                    .toList();
        }

        if (users != null && !users.isEmpty()) {
            filteredData = filteredData.stream()
                    .filter(dto -> users.contains(dto.username()))
                    .toList();
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=raport.xlsx");

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Raport");

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

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/summary/{userId}")
    public List<MixedChartDTO> getMixedChartData(@PathVariable Long userId) {
        return savedDataService.getMixedChartForUser(userId);
    }

    @GetMapping("/overtime/{userId}")
    public OvertimeSummaryDTO getOvertimeSummary(@PathVariable Long userId) {
        int paid = Optional.ofNullable(overtimeBalanceRepository
                .sumOvertimeByUserAndType(userId, VolumeType.OVERTIME_PAID)).orElse(0);

        int offRaw = Optional.ofNullable(overtimeBalanceRepository
                .sumOvertimeByUserAndType(userId, VolumeType.OVERTIME_OFF)).orElse(0);

        int deductedPartial = Optional.ofNullable(overtimeBalanceRepository
                .sumOvertimeByUserAndType(userId, VolumeType.DEDUCT_PARTIAL)).orElse(0);

        int deductedFullDay = Optional.ofNullable(overtimeBalanceRepository
                .sumOvertimeByUserAndType(userId, VolumeType.DEDUCT_FULL_DAY)).orElse(0);

        int off = offRaw - deductedPartial - deductedFullDay;

        return new OvertimeSummaryDTO(paid, off);
    }




    @PostMapping("/deduct-full-day")
    public ResponseEntity<Void> deductFullDay(@RequestBody DeductFullDayDTO dto) {
        savedDataService.deductFullDay(dto.userId(), dto.date());
        return ResponseEntity.ok().build();
    }

}






