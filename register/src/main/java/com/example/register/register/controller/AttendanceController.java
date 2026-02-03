package com.example.register.register.controller;

import com.example.register.register.DTO.LeaveDeleteDTO;
import com.example.register.register.DTO.LeaveEventDTO;
import com.example.register.register.DTO.LeaveRangeDTO;
import com.example.register.register.DTO.LeaveUpdateDTO;
import com.example.register.register.model.Attendance;
import com.example.register.register.model.User;
import com.example.register.register.repository.AttendanceRepository;
import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.security.core.Authentication;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attendance/")
public class AttendanceController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/update")
    public ResponseEntity<String> updateAttendance(@RequestParam Long userId, @RequestParam String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Attendance attendance = attendanceRepository.findByUserAndAttendanceDate(user, LocalDate.now())
                .orElseGet(() -> {
                    Attendance newAttendance = new Attendance();
                    newAttendance.setUser(user);
                    newAttendance.setAttendanceDate(LocalDate.now());
                    newAttendance.setWorkMode(null);
                    newAttendance.setStatus(status);
                    return newAttendance;
                });

        // Jeśli rekord już istniał – zaktualizuj tylko status
        if (attendance.getId() != null) {
            attendance.setStatus(status); // 🔹 Zmieniamy tylko status
        }

        attendanceRepository.save(attendance);

        return ResponseEntity.ok("Status zaktualizowany!");
    }

@GetMapping("/workmode/summary")
public ResponseEntity<Map<String, Object>> getWorkModeSummary(
        @RequestParam(required = false) String sectionId,
        Principal principal
) {
    List<User> users;

    if (sectionId != null && !"all".equalsIgnoreCase(sectionId)) {
        long id;
        try {
            id = Long.parseLong(sectionId);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
        users = userRepository.findBySection_Id(id);
    } else {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        users = userRepository.findByTeam_Id(currentUser.getTeam().getId());
    }

    int total = users.size();
    int office = 0;
    int homeoffice = 0;
    List<String> officeNames = new ArrayList<>();
    List<String> homeofficeNames = new ArrayList<>();

    LocalDate today = LocalDate.now();

    for (User user : users) {
        Optional<Attendance> attendanceOpt = attendanceRepository.findByUserAndAttendanceDate(user, today);
        if (attendanceOpt.isPresent()) {
            String mode = attendanceOpt.get().getWorkMode();
            String fullName = user.getFirstName() + " " + user.getLastName();

            if ("office".equalsIgnoreCase(mode)) {
                office++;
                officeNames.add(fullName);
            } else if ("homeoffice".equalsIgnoreCase(mode)) {
                homeoffice++;
                homeofficeNames.add(fullName);
            }
        }
    }

    Map<String, Object> result = new HashMap<>();
    result.put("total", total);
    result.put("office", office);
    result.put("homeoffice", homeoffice);
    result.put("officeNames", officeNames);
    result.put("homeofficeNames", homeofficeNames);

    return ResponseEntity.ok(result);
}

    @GetMapping("/leaves")
    public List<LeaveEventDTO> getLeaves(Principal principal) {
        return attendanceService.getAllLeaves(principal);
    }

    @PostMapping("/addLeave")
    public ResponseEntity<?> addLeave(@RequestBody LeaveRangeDTO dto, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika"));
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            return ResponseEntity.badRequest().body("Brak daty");
        }
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            return ResponseEntity.badRequest().body("Zły zakres dat");
        }

        // ⛔ Blokada: nie można dodawać urlopu w przeszłości lub na dziś
        LocalDate today = LocalDate.now();
        if (!dto.getStartDate().isAfter(today) || !dto.getEndDate().isAfter(today)) {
            return ResponseEntity.badRequest().body("Urlop można dodać tylko na przyszłe dni!");
        }

        // Sprawdź, czy JAKIKOLWIEK dzień z zakresu jest już oznaczony jako urlop
        List<Attendance> existingLeaves = attendanceRepository.findLeavesInRange(user, dto.getStartDate(), dto.getEndDate());
        if (!existingLeaves.isEmpty()) {
            return ResponseEntity.badRequest().body("Niektóre dni już są oznaczone jako urlop.");
        }

        // Dodaj wszystkie dni z zakresu
        LocalDate d = dto.getStartDate();
        while (!d.isAfter(dto.getEndDate())) {
            Attendance att = new Attendance();
            att.setUser(user);
            att.setAttendanceDate(d);
            att.setStatus("leave");
            att.setWorkMode(null);
            attendanceRepository.save(att);
            d = d.plusDays(1);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/updateLeave")
    public ResponseEntity<?> updateLeave(@RequestBody LeaveUpdateDTO dto, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika"));

        LocalDate today = LocalDate.now();
        if (!dto.getNewStartDate().isAfter(today) || !dto.getNewEndDate().isAfter(today)) {
            return ResponseEntity.badRequest().body("Urlop można aktualizować tylko na przyszłe dni!");
        }

        // 1. Usuń stare wpisy z oryginalnego zakresu (dla usera)
        attendanceRepository.deleteAllByUserAndAttendanceDateBetweenAndStatus(
                user, dto.getOldStartDate(), dto.getOldEndDate(), "leave"
        );

        // 2. Dodaj nowe wpisy na podstawie wybranego zakresu (analogicznie jak przy dodawaniu)
        LocalDate d = dto.getNewStartDate();
        while (!d.isAfter(dto.getNewEndDate())) {
            Attendance att = new Attendance();
            att.setUser(user);
            att.setAttendanceDate(d);
            att.setStatus("leave");
            att.setWorkMode(null);
            attendanceRepository.save(att);
            d = d.plusDays(1);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deleteLeave")
    public ResponseEntity<?> deleteLeave(@RequestBody LeaveDeleteDTO dto, Authentication authentication) throws AccessDeniedException {

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika"));

        String currentUsername = authentication.getName();

        // Pozwól jeśli: a) usuwasz swój urlop, b) masz uprawnioną rolę
        if (
                !currentUsername.equals(user.getUsername()) &&
                        !hasAnyRole(authentication, "ROLE_ADMIN", "ROLE_MANAGER", "ROLE_COORDINATOR")
        ) {
            throw new AccessDeniedException("Brak uprawnień do usuwania cudzych urlopów!");
        }

        attendanceRepository.deleteAllByUserAndAttendanceDateBetweenAndStatus(
                user, dto.getStartDate(), dto.getEndDate(), "leave"
        );
        return ResponseEntity.ok().build();
    }

    private boolean hasAnyRole(Authentication auth, String... roles) {
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .anyMatch(authority -> Arrays.asList(roles).contains(authority));
    }


    @GetMapping("/present/count")
    public ResponseEntity<Map<String, Integer>> getPresentEmployeesCount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Principal principal
    ) {
        int presentCount = attendanceService.getPresentEmployees(date, principal).size();

        Map<String, Integer> result = new HashMap<>();
        result.put("presentCount", presentCount);

        return ResponseEntity.ok(result);
    }



}
