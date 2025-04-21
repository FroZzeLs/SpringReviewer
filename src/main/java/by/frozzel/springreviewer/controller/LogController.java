package by.frozzel.springreviewer.controller;

import by.frozzel.springreviewer.exception.ResourceNotFoundException;
import by.frozzel.springreviewer.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Logs",
        description = "API для работы с логами приложения")
public class LogController {

    private final LogService logService;

    @GetMapping("/download")
    @Operation(
            summary = "Скачать файл логов за определенную дату",
            description = "Возвращает файл логов (текущий или архивный) для указанной даты."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Файл логов успешно найден и отправлен",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный параметр запроса (дата не указана, некорректный формат или дата в будущем)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Файл логов на указанную дату не найден",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера при чтении файла",
                    content = @Content
            )
    })
    public ResponseEntity<Resource> downloadLogFile(
            @Parameter(
                    description = "Дата для скачивания логов (в формате YYYY-MM-DD)",
                    required = true,
                    example = "2025-04-15")
            @RequestParam("date")
            @NotNull(message = "Date parameter is required")
            @PastOrPresent(message = "Date must be in the past or present")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        try {
            Path logFilePath = logService.getLogFilePathForDate(date);
            Resource resource = new FileSystemResource(logFilePath);

            if (!resource.exists() || !resource.isReadable()) {
                log.error("Log resource not found or not readable after service check: {}", logFilePath);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Log file became unreadable.");
            }

            String filename = logFilePath.getFileName().toString();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");

            long contentLength = Files.size(logFilePath);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(contentLength)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (ResourceNotFoundException e) {
            log.warn("Log file not found request for date {}: {}", date, e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IOException e) {
            log.error("IO error preparing log file for download for date {}: {}", date, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading log file.", e);
        } catch (Exception e) {
            log.error("Unexpected error getting log file for date {}: {}", date, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", e);
        }
    }
}