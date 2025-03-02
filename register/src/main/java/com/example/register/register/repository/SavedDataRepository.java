package com.example.register.register.repository;

import com.example.register.register.model.SavedData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedDataRepository  extends JpaRepository<SavedData, Long> {
}
