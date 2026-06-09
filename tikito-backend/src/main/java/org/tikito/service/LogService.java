package org.tikito.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.dto.LogDto;
import org.tikito.dto.LogMessage;
import org.tikito.entity.Log;
import org.tikito.repository.LogRepository;

import java.util.List;

@Service
public class LogService {
    private final LogRepository logRepository;
    private static LogService instance;

    public LogService(final LogRepository logRepository) {
        this.logRepository = logRepository;
        LogService.instance = this;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public static void log(final LogMessage message, final String objectIdentifier) {
        LogService.instance.logRepository.save(new Log(message, objectIdentifier));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void markAsRead(final long id) {
        logRepository.deleteById(id);
    }

    public List<LogDto> getLogs() {
        return logRepository.findAll()
                .stream()
                .map(Log::toDto)
                .toList();
    }
}
