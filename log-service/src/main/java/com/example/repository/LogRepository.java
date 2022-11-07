package com.example.repository;

import com.example.domain.AppLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LogRepository extends MongoRepository<AppLog, String> {
    List<AppLog> findByTimeBetweenOrderByTimeDesc(LocalDateTime start, LocalDateTime end);
}
