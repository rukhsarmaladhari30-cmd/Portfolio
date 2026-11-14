package com.library.controller;

import com.library.dto.LibraryDtos.IssueBookPayload;
import com.library.dto.LibraryDtos.ReturnResponse;
import com.library.model.BorrowRecord;
import com.library.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/borrow")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BorrowController {

    private final BorrowService borrowService;

    @PostMapping("/issue")
    public ResponseEntity<BorrowRecord> issueBook(@RequestBody IssueBookPayload payload) {
        return ResponseEntity.ok(borrowService.issueBook(payload.getUserId(), payload.getBookId()));
    }

    @PostMapping("/return/{borrowId}")
    public ResponseEntity<ReturnResponse> returnBook(@PathVariable Long borrowId) {
        return ResponseEntity.ok(borrowService.returnBook(borrowId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BorrowRecord>> getUserHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(borrowService.getStudentBorrowHistory(userId));
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<BorrowRecord>> getUserActive(@PathVariable Long userId) {
        return ResponseEntity.ok(borrowService.getStudentActiveBorrows(userId));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowRecord>> getOverdue() {
        return ResponseEntity.ok(borrowService.getOverdueRecords());
    }

    @GetMapping("/all")
    public ResponseEntity<List<BorrowRecord>> getAll() {
        return ResponseEntity.ok(borrowService.getAllBorrowRecords());
    }
}

