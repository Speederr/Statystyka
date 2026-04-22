package com.example.register.register.repository;

import com.example.register.register.model.UserProcessLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProcessLevelRepository extends JpaRepository<UserProcessLevel, Long> {

    List<UserProcessLevel> findByUserId(Long userId);
    void deleteByUserIdAndProcessId(Long userId, Long processId);

}
