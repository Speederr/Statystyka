package com.example.register.register.repository;

import com.example.register.register.model.OvertimePayoutHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OvertimePayoutHistoryRepository extends JpaRepository<OvertimePayoutHistory, Long> {

}
