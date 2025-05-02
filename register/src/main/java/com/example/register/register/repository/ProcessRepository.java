package com.example.register.register.repository;

import com.example.register.register.model.BusinessProcess;
import com.example.register.register.model.Team;
import com.example.register.register.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Arrays;
import java.util.List;

public interface ProcessRepository extends JpaRepository<BusinessProcess, Long> {

    List<BusinessProcess> findAll();

    @Query("SELECT f.process FROM UserFavorites f WHERE f.user.id = :userId")
    List<BusinessProcess> findFavoritesByUserId(@Param("userId") Long userId);

    List<BusinessProcess> findByTeamId(Long teamId);


}