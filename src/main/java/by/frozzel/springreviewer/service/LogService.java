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
        Path archiveDir = path.getParent() != null ? path.getParent().resolve("archived") : Paths.get("archived");
        String baseName = path.getFileName().toString();
        String archivedNamePattern = baseName.endsWith(".log")
                ? baseName.substring(0, baseName.length() - 4) + "-%s.log"
                : baseName + "-%s.log";
        this.logFilePattern = archiveDir.resolve(archivedNamePattern).toString();

        log.info("Log service initialized.");
        log.info("Active log file path: {}", this.logFilePath);
        log.info("Archived log file pattern: {}", this.logFilePattern);
    }

    public List<String> getLogsForDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        Path logPathToRead;
        String datePrefix = date.format(LOG_DATE_FORMATTER);

        if (date.isAfter(today)) {
            log.warn("Cannot request logs for a future date: {}", datePrefix);
            return Collections.emptyList();
        } else if (date.isEqual(today)) {
            logPathToRead = Paths.get(logFilePath);
            log.info("Reading logs for today ({}) from active file: {}", datePrefix, logPathToRead);
        } else {
            String formattedDate = date.format(LOG_DATE_FORMATTER);
            String specificLogFilePath = String.format(logFilePattern, formattedDate);
            logPathToRead = Paths.get(specificLogFilePath);
            log.info("Reading logs for past date ({}) from archived file: {}", datePrefix, logPathToRead);
        }

        return readLogsFromFile(logPathToRead, datePrefix);
    }

    private List<String> readLogsFromFile(Path filePath, String datePrefix) {
        if (!Files.exists(filePath)) {
            log.warn("Log file not found at path: {}", filePath);
            return Collections.emptyList();
        }

        try (Stream<String> lines = Files.lines(filePath, StandardCharsets.UTF_8)) {
            List<String> filteredLogs = lines
                    .filter(line -> line.startsWith(datePrefix))
                    .toList();
            log.info("Found {} log entries for date {} in file {}", filteredLogs.size(), datePrefix, filePath);
            return filteredLogs;
        } catch (NoSuchFileException e) {
            log.warn("Log file disappeared while attempting to read: {}", filePath);
            return Collections.emptyList();
        } catch (IOException e) {
            log.error("Error reading log file: {}", filePath, e);
            throw new LogAccessException("Failed to read log file: " + filePath, e);
        } catch (SecurityException e) {
            log.error("Permission denied while trying to read log file: {}", filePath, e);
            throw new LogAccessException("Permission denied for log file: " + filePath, e);
        }
    }
}