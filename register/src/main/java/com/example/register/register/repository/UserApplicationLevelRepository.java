package com.example.register.register.repository;

import com.example.register.register.model.Application;
import com.example.register.register.model.User;
import com.example.register.register.model.UserApplicationLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserApplicationLevelRepository extends JpaRepository<UserApplicationLevel, Long> {

    List<UserApplicationLevel> findByUser(User user);
    Optional<UserApplicationLevel> findByUserAndApplication(User user, Application application);
}
