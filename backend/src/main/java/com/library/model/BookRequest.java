package com.library.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @CreationTimestamp
    @Column(name = "request_date", updatable = false)
    private LocalDateTime requestDate;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
}

