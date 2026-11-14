package com.library.repository;

import com.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByIsbn(String isbn);
    boolean existsByIsbn(String isbn);
    List<Book> findByCategoryId(Long categoryId);

    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.category.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Book> searchBooks(@Param("query") String query);

    @Query("SELECT SUM(b.totalCopies) FROM Book b")
    Long sumTotalCopies();

    @Query("SELECT SUM(b.availableCopies) FROM Book b")
    Long sumAvailableCopies();
}

