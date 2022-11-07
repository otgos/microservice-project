package com.example.service;

import com.example.domain.AppLog;
import com.example.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogService {
    @Autowired
    private LogRepository logRepository;

    public void saveLog(AppLog appLog) {
        logRepository.save(appLog);
    }

    public List<AppLog> getAll(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return logRepository.findByTimeBetweenOrderByTimeDesc(startDateTime,endDateTime);
    }
}
