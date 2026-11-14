package com.library.service;

import com.library.dto.LibraryDtos.DashboardStats;
import com.library.model.Book;
import com.library.model.BookRequest;
import com.library.model.User;
import com.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRequestRepository bookRequestRepository;
    private final FineRepository fineRepository;
    private final CategoryRepository categoryRepository;

    public DashboardStats getDashboardStatistics() {
        long totalBooks = bookRepository.count();
        Long totalCopies = bookRepository.sumTotalCopies();
        Long availableCopies = bookRepository.sumAvailableCopies();
        long registeredStudents = userRepository.countByRole("ROLE_STUDENT");
        long currentlyIssued = borrowRecordRepository.countByStatus("ISSUED");
        long overdueCount = borrowRecordRepository.findOverdueRecords(LocalDate.now()).size();
        long pendingRequests = bookRequestRepository.countByStatus("PENDING");
        BigDecimal unpaidFines = fineRepository.sumTotalUnpaidFines();

        // Category distribution
        Map<String, Long> categoryMap = new HashMap<>();
        categoryRepository.findAll().forEach(cat -> {
            long count = bookRepository.findByCategoryId(cat.getId()).size();
            categoryMap.put(cat.getName(), count);
        });

        // Monthly borrow stats (mock data for visualization or aggregate)
        List<Map<String, Object>> monthlyStats = new ArrayList<>();
        String[] months = {"Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        int[] circulations = {42, 68, 95, 110, 145, 160};
        for (int i = 0; i < months.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("month", months[i]);
            map.put("borrows", circulations[i]);
            monthlyStats.add(map);
        }

        return DashboardStats.builder()
                .totalBooks(totalBooks)
                .totalBookCopies(totalCopies != null ? totalCopies : 0)
                .availableCopies(availableCopies != null ? availableCopies : 0)
                .registeredStudents(registeredStudents)
                .currentlyIssuedBooks(currentlyIssued)
                .overdueBooks(overdueCount)
                .pendingRequests(pendingRequests)
                .totalFinesCollected(unpaidFines != null ? unpaidFines : BigDecimal.ZERO)
                .categoryDistribution(categoryMap)
                .monthlyBorrowStats(monthlyStats)
                .build();
    }

    public List<BookRequest> getPendingRequests() {
        return bookRequestRepository.findByStatus("PENDING");
    }

    @Transactional
    public BookRequest approveOrRejectRequest(Long requestId, Long adminUserId, boolean approve) {
        BookRequest request = bookRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Book request not found: " + requestId));

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("Admin user not found: " + adminUserId));

        request.setStatus(approve ? "APPROVED" : "REJECTED");
        request.setApprovedBy(admin);
        request.setApprovalDate(LocalDateTime.now());

        return bookRequestRepository.save(request);
    }

    public List<User> getAllStudents() {
        return userRepository.findByRole("ROLE_STUDENT");
    }
}

