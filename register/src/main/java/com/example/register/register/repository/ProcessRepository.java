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

    List<BusinessProcess> findByTeamIdAndActiveTrueOrderByProcessNameAsc(Long teamId);
    List<BusinessProcess> findByTeamIdAndActive(Long teamId, boolean active);

    @Query("""
    SELECT uf.process FROM UserFavorites uf
    WHERE uf.user.id = :userId AND uf.process.active = true
    """)
    List<BusinessProcess> findFavoritesByUserId(@Param("userId") Long userId);



    List<BusinessProcess> findByTeamId(Long teamId);


}