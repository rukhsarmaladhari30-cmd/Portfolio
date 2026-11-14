package com.library.service;

import com.library.dto.LibraryDtos.ReturnResponse;
import com.library.model.*;
import com.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final FineRepository fineRepository;

    @Value("${app.library.loan-duration-days:14}")
    private int loanDurationDays;

    @Value("${app.library.fine-rate-per-day:5.00}")
    private BigDecimal fineRatePerDay;

    @Transactional
    public BorrowRecord issueBook(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Student user not found with id: " + userId));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));

        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("No copies available for issue. All " + book.getTotalCopies() + " copies are currently checked out.");
        }

        // Check active borrowings of student
        List<BorrowRecord> activeBorrows = borrowRecordRepository.findByUserIdAndStatus(userId, "ISSUED");
        if (activeBorrows.size() >= 5) {
            throw new RuntimeException("Student has reached the maximum borrowing limit of 5 books.");
        }

        // Decrement available copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(loanDurationDays);

        BorrowRecord record = BorrowRecord.builder()
                .user(user)
                .book(book)
                .issueDate(issueDate)
                .dueDate(dueDate)
                .status("ISSUED")
                .fineAmount(BigDecimal.ZERO)
                .build();

        return borrowRecordRepository.save(record);
    }

    @Transactional
    public ReturnResponse returnBook(Long borrowRecordId) {
        BorrowRecord record = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> new RuntimeException("Borrow record not found with id: " + borrowRecordId));

        if ("RETURNED".equalsIgnoreCase(record.getStatus())) {
            throw new RuntimeException("This book has already been marked as returned.");
        }

        LocalDate returnDate = LocalDate.now();
        record.setReturnDate(returnDate);
        record.setStatus("RETURNED");

        // Calculate overdue days and fine
        int daysOverdue = 0;
        BigDecimal fine = BigDecimal.ZERO;

        if (returnDate.isAfter(record.getDueDate())) {
            daysOverdue = (int) ChronoUnit.DAYS.between(record.getDueDate(), returnDate);
            fine = fineRatePerDay.multiply(BigDecimal.valueOf(daysOverdue));
            record.setFineAmount(fine);

            // Record fine in fines table
            Fine fineRecord = Fine.builder()
                    .borrowRecord(record)
                    .user(record.getUser())
                    .amount(fine)
                    .daysOverdue(daysOverdue)
                    .paidStatus("UNPAID")
                    .build();
            fineRepository.save(fineRecord);
        }

        // Increment book available copies
        Book book = record.getBook();
        book.setAvailableCopies(Math.min(book.getTotalCopies(), book.getAvailableCopies() + 1));
        bookRepository.save(book);

        borrowRecordRepository.save(record);

        return ReturnResponse.builder()
                .borrowId(record.getId())
                .bookTitle(book.getTitle())
                .issueDate(record.getIssueDate())
                .dueDate(record.getDueDate())
                .returnDate(returnDate)
                .daysOverdue(daysOverdue)
                .fineAmount(fine)
                .message(daysOverdue > 0 ? "Book returned late (" + daysOverdue + " days overdue). Fine: ₹" + fine : "Book returned successfully on time.")
                .build();
    }

    public List<BorrowRecord> getStudentBorrowHistory(Long userId) {
        return borrowRecordRepository.findByUserId(userId);
    }

    public List<BorrowRecord> getStudentActiveBorrows(Long userId) {
        return borrowRecordRepository.findByUserIdAndStatus(userId, "ISSUED");
    }

    public List<BorrowRecord> getAllBorrowRecords() {
        return borrowRecordRepository.findAll();
    }

    public List<BorrowRecord> getOverdueRecords() {
        return borrowRecordRepository.findOverdueRecords(LocalDate.now());
    }
}

