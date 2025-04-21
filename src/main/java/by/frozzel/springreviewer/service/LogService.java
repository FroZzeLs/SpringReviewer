package by.frozzel.springreviewer.service;

import by.frozzel.springreviewer.exception.ResourceNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class LogService {

    @Getter
    private final String logFilePathString;
    private final String logFilePattern;
    private static final DateTimeFormatter LOG_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd");
    private static final String LOG_RESOURCE = "Log file";

    public LogService(@Value("${logging.file.name}") String logFileName) {
        Path path = Paths.get(logFileName);
        this.logFilePathString = path.toAbsolutePath().toString();
        Path parentDir = path.getParent();
        Path archiveDir = parentDir != null ? parentDir.resolve("archived") : Paths.get("archived");
        String baseName = path.getFileName().toString();
        String archivedNamePattern = baseName.endsWith(".log")
                ? baseName.substring(0, baseName.length() - 4) + "-%s.log"
                : baseName + "-%s.log";
        this.logFilePattern = archiveDir.resolve(archivedNamePattern).toString();

        log.info("Log service initialized.");
        log.info("Active log file path: {}", this.logFilePathString);
        log.info("Archived log file pattern: {}", this.logFilePattern);
    }


    public Path getLogFilePathForDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        Path logPathToRead;
        String formattedDate = date.format(LOG_DATE_FORMATTER);

        if (date.isAfter(today)) {
            log.warn("Cannot request logs for a future date: {}", formattedDate);
            throw new ResourceNotFoundException(LOG_RESOURCE, "date", "Future date " + formattedDate + " requested");
        } else if (date.isEqual(today)) {
            logPathToRead = Paths.get(logFilePathString);
            log.info("Requested logs for today ({}), using active file: {}", formattedDate, logPathToRead);
        } else {
            String specificLogFilePath = String.format(logFilePattern, formattedDate);
            logPathToRead = Paths.get(specificLogFilePath);
            log.info("Requested logs for past date ({}), using archived file: {}", formattedDate, logPathToRead);
        }

        try {
            if (!Files.exists(logPathToRead)) {
                log.warn("Log file not found at path: {}", logPathToRead);
                throw new ResourceNotFoundException(LOG_RESOURCE, "path", logPathToRead.toString());
            }
        } catch (SecurityException e) {
            log.error("Permission denied while trying to access log file path: {}", logPathToRead, e);
            throw new ResourceNotFoundException(LOG_RESOURCE, "path", logPathToRead + " (permission denied)");
        }

        return logPathToRead;
    }
}