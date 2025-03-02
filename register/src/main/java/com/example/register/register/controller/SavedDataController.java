package com.example.register.register.controller;

import com.example.register.register.model.SavedData;
import com.example.register.register.service.SavedDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/saved-data")
public class SavedDataController {

    @Autowired
    private SavedDataService savedDataService;

    @PostMapping("/save")
    public String saveData(@RequestBody List<SavedData> dataList) {
        for (SavedData data : dataList) {
            data.setTodaysDate(new Date()); // Ręczne ustawienie bieżącej daty
        }
        savedDataService.saveData(dataList);
        return "Dane zostały zapisane!";
    }





}
