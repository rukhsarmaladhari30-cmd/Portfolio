package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class LibraryDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookRequestPayload {
        private String title;
        private String author;
        private String isbn;
        private Long categoryId;
        private Integer totalCopies;
        private String coverImage;
        private Integer publishedYear;
        private String publisher;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IssueBookPayload {
        private Long userId;
        private Long bookId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReturnResponse {
        private Long borrowId;
        private String bookTitle;
        private LocalDate issueDate;
        private LocalDate dueDate;
        private LocalDate returnDate;
        private int daysOverdue;
        private BigDecimal fineAmount;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DashboardStats {
        private long totalBooks;
        private long totalBookCopies;
        private long availableCopies;
        private long registeredStudents;
        private long currentlyIssuedBooks;
        private long overdueBooks;
        private long pendingRequests;
        private BigDecimal totalFinesCollected;
        private Map<String, Long> categoryDistribution;
        private List<Map<String, Object>> monthlyBorrowStats;
    }
}

