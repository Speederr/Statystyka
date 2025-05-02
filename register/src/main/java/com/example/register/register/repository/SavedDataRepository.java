package com.example.register.register.repository;

import com.example.register.register.model.SavedData;
import com.example.register.register.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface SavedDataRepository  extends JpaRepository<SavedData, Long> {

    @Query("SELECT sd FROM SavedData sd WHERE sd.user.id = :userId AND sd.process.nonOperational = true AND sd.todaysDate = :date")
    List<SavedData> findNonOperationalSavedDataByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate todaysDate);

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM SavedData s WHERE s.user = :user AND s.todaysDate = :date")
    Long sumQuantityByUserAndDate(@Param("user") User user, @Param("date") LocalDate todaysDate);

    List<SavedData> findByUserAndTodaysDate(User user, LocalDate todaysDate);

    @Query("SELECT sd.todaysDate, p.processName, SUM(sd.quantity) " +
            "FROM SavedData sd " +
            "JOIN sd.process p " +
            "WHERE sd.user.id = :userId " +
            "GROUP BY sd.todaysDate, p.processName " +
            "ORDER BY sd.todaysDate")
    List<Object[]> getStackedChartData(@Param("userId") Long userId);

    @Query("""
    SELECT SUM(s.quantity)
    FROM SavedData s
    WHERE s.user.id = :userId
    AND s.process.nonOperational = true
    """)
    Double sumNonOperationalHoursByUserId(@Param("userId") Long userId);

}
