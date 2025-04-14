package by.frozzel.springreviewer.controller;

import by.frozzel.springreviewer.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Logs",
        description = "API для работы с логами приложения")
public class LogController {

    private final LogService logService;

    @GetMapping("/by-date")
    @Operation(
            summary = "Получить логи за определенную дату",
            description = "Возвращает список строковых представлений логов за указанную дату."
    )
    @ApiResponses(value = {
        @ApiResponse(
                    responseCode = "200",
                    description = "Логи успешно получены",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "array",
                                    implementation = String.class))),
        @ApiResponse(
                    responseCode = "400",
                    description = "Неверный параметр запроса (дата не указана,"
                            + " некорректный формат или дата в будущем)",
                    content = @Content)
    })
    public ResponseEntity<List<String>> getLogsByDate(
            @Parameter(
                    description = "Дата для фильтрации логов (в формате YYYY-MM-DD)",
                    required = true,
                    example = "2024-03-15")
            @RequestParam
            @NotNull(message = "Date parameter is required")
            @PastOrPresent(message = "Date must be in the past or present")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        List<String> logs = logService.getLogsForDate(date);
        return ResponseEntity.ok(logs);
    }
}