package by.frozzel.springreviewer.service;

import by.frozzel.springreviewer.exception.LogAccessException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogService {

    private final String logFilePath;
    private final String logFilePattern;
    private static final DateTimeFormatter LOG_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd");

    public LogService(@Value("${logging.file.name}") String logFileName) {
        Path path = Paths.get(logFileName);
        this.logFilePath = path.toAbsolutePath().toString();
        this.logFilePattern = path.getParent().resolve("archived")
                .resolve(path.getFileName().toString().replace(".log", "-%s.log"))
                .toString();
        log.info("Log service initialized. Active log file path: {}", this.logFilePath);
    }

    public List<String> getLogsForDate(LocalDate date) {
        String datePrefix = date.format(LOG_DATE_FORMATTER);
        Path currentLogPath = Paths.get(logFilePath);

        if (!Files.exists(currentLogPath)) {
            log.warn("Active log file not found at path: {}", logFilePath);
            return Collections.emptyList();
        }

        log.info("Attempting to read logs for date {} from file {}", datePrefix, currentLogPath);

        try (Stream<String> lines = Files.lines(currentLogPath, StandardCharsets.UTF_8)) {
            List<String> filteredLogs = lines
                    .filter(line -> line.startsWith(datePrefix))
                    .collect(Collectors.toList());
            log.info("Found {} log entries for date {}", filteredLogs.size(), datePrefix);
            return filteredLogs;
        } catch (NoSuchFileException e) {
            log.warn("Log file not found while reading: {}", currentLogPath);
            return Collections.emptyList();
        } catch (IOException e) {
            log.error("Error reading log file: {}", currentLogPath, e);
            throw new LogAccessException("Failed to read log file: " + currentLogPath, e);
        }
    }
}