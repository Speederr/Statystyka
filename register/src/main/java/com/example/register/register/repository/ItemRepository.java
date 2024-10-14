package com.example.register.register.repository;

import com.example.register.register.model.SavedItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<SavedItem, Long> {
}
