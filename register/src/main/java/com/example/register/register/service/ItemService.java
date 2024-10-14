package com.example.register.register.service;

import com.example.register.register.model.SavedItem;
import com.example.register.register.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    public void saveItems(List<String> items) {
        for(String item : items) {
            SavedItem savedItem = new SavedItem();
            itemRepository.save(savedItem);
        }
    }
}
