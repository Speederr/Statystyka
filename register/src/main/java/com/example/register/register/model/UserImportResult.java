package com.example.register.register.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserImportResult {

    private int successCount;
    private int errorCount;

    private List<String> errors = new ArrayList<>();

    public void addSuccess() {
        successCount++;
    }

    public void addError(String error) {
        errorCount++;
        errors.add(error);
    }

}
