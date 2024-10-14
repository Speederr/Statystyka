package com.example.register.register.controller;

import com.example.register.register.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @PostMapping("/save")
    public ResponseEntity<Void> saveItems(@RequestBody List<String> items) {
        itemService.saveItems(items);
        return ResponseEntity.ok().build();
    }
}
