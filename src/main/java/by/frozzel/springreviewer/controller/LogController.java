package by.frozzel.springreviewer.controller;

import by.frozzel.springreviewer.service.LogService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
@Validated
public class LogController {

    private final LogService logService;

    @GetMapping("/by-date")
    public ResponseEntity<List<String>> getLogsByDate(
            @RequestParam @NotNull(message = "Date parameter is required")
            @PastOrPresent(message = "Date must be in the past or present")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<String> logs = logService.getLogsForDate(date);
        return ResponseEntity.ok(logs);
    }
}