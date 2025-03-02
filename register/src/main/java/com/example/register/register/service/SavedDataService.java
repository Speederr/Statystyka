package com.example.register.register.service;

import com.example.register.register.model.SavedData;
import com.example.register.register.repository.SavedDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SavedDataService {

    @Autowired
    private SavedDataRepository savedDataRepository;

    public void saveData(List<SavedData> dataList) {
        savedDataRepository.saveAll(dataList);
    }
}