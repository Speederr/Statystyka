package com.example.register.register.repository;

import com.example.register.register.model.BusinessProcess;
import org.springframework.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProcessRepository extends JpaRepository<BusinessProcess, Long> {

    @Override
    @NonNull
    List<BusinessProcess> findAll();

    @Query("SELECT f.process FROM UserFavorites f WHERE f.user.id = :userId")
    List<BusinessProcess> findFavoritesByUserId(@Param("userId") Long userId);

    List<BusinessProcess> findByTeamId(Long teamId);


}