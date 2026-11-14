package com.library.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "borrow_id", nullable = false)
    private BorrowRecord borrowRecord;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "days_overdue", nullable = false)
    private Integer daysOverdue;

    @Column(name = "paid_status", nullable = false, length = 20)
    @Builder.Default
    private String paidStatus = "UNPAID"; // UNPAID, PAID

    @Column(name = "paid_date")
    private LocalDateTime paidDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

