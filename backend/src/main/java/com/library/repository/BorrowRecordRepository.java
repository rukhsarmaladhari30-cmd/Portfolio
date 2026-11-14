package com.library.repository;

import com.library.model.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    List<BorrowRecord> findByUserId(Long userId);
    List<BorrowRecord> findByUserIdAndStatus(Long userId, String status);
    List<BorrowRecord> findByStatus(String status);
    long countByStatus(String status);

    @Query("SELECT r FROM BorrowRecord r WHERE r.status = 'ISSUED' AND r.dueDate < :currentDate")
    List<BorrowRecord> findOverdueRecords(LocalDate currentDate);

    @Query("SELECT r.book.id, r.book.title, COUNT(r) as borrowCount FROM BorrowRecord r GROUP BY r.book.id, r.book.title ORDER BY borrowCount DESC")
    List<Object[]> findMostBorrowedBooks();
}

