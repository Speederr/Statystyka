package com.example.register.register.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcessService {

    private final JdbcTemplate jdbcTemplate;

    public ProcessService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> getAllProcesses() {
        String sql = "SELECT process_name from processes";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("process_name"));
    }
}
