package com.library.controller;

import com.library.dto.LibraryDtos.DashboardStats;
import com.library.model.BookRequest;
import com.library.model.User;
import com.library.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getStats() {
        return ResponseEntity.ok(adminService.getDashboardStatistics());
    }

    @GetMapping("/requests")
    public ResponseEntity<List<BookRequest>> getPendingRequests() {
        return ResponseEntity.ok(adminService.getPendingRequests());
    }

    @PostMapping("/requests/{requestId}/action")
    public ResponseEntity<BookRequest> actOnRequest(
            @PathVariable Long requestId,
            @RequestParam Long adminId,
            @RequestParam boolean approve) {
        return ResponseEntity.ok(adminService.approveOrRejectRequest(requestId, adminId, approve));
    }

    @GetMapping("/students")
    public ResponseEntity<List<User>> getAllStudents() {
        return ResponseEntity.ok(adminService.getAllStudents());
    }
}

