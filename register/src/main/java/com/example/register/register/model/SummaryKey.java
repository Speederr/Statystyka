package com.example.register.register.model;

import java.time.LocalDate;
import java.util.Date;

public record SummaryKey(String processName, LocalDate todaysDate, String username) {
}
