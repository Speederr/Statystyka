package com.example.register.register.model;

import java.time.LocalDate;

public record SummaryKey(String processName, LocalDate todaysDate, String username, String volumeType) {
}
