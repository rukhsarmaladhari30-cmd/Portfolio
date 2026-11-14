package com.library.repository;

import com.library.model.Fine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {
    List<Fine> findByUserId(Long userId);
    List<Fine> findByPaidStatus(String paidStatus);

    @Query("SELECT SUM(f.amount) FROM Fine f WHERE f.paidStatus = 'UNPAID'")
    BigDecimal sumTotalUnpaidFines();
}

