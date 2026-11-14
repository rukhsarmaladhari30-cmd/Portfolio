package com.library.service;

import com.library.dto.LibraryDtos.BookRequestPayload;
import com.library.model.Book;
import com.library.model.Category;
import com.library.repository.BookRepository;
import com.library.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
    }

    public List<Book> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return bookRepository.findAll();
        }
        return bookRepository.searchBooks(query.trim());
    }

    public List<Book> getBooksByCategory(Long categoryId) {
        return bookRepository.findByCategoryId(categoryId);
    }

    @Transactional
    public Book addBook(BookRequestPayload payload) {
        if (bookRepository.existsByIsbn(payload.getIsbn())) {
            throw new RuntimeException("Book with ISBN " + payload.getIsbn() + " already exists.");
        }

        Category category = categoryRepository.findById(payload.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + payload.getCategoryId()));

        Book book = Book.builder()
                .title(payload.getTitle())
                .author(payload.getAuthor())
                .isbn(payload.getIsbn())
                .category(category)
                .totalCopies(payload.getTotalCopies() != null ? payload.getTotalCopies() : 1)
                .availableCopies(payload.getTotalCopies() != null ? payload.getTotalCopies() : 1)
                .coverImage(payload.getCoverImage())
                .publishedYear(payload.getPublishedYear())
                .publisher(payload.getPublisher())
                .description(payload.getDescription())
                .build();

        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(Long id, BookRequestPayload payload) {
        Book book = getBookById(id);

        if (payload.getCategoryId() != null) {
            Category category = categoryRepository.findById(payload.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            book.setCategory(category);
        }

        book.setTitle(payload.getTitle());
        book.setAuthor(payload.getAuthor());
        book.setCoverImage(payload.getCoverImage());
        book.setPublisher(payload.getPublisher());
        book.setPublishedYear(payload.getPublishedYear());
        book.setDescription(payload.getDescription());

        // Adjust copies if total copies was modified
        if (payload.getTotalCopies() != null) {
            int difference = payload.getTotalCopies() - book.getTotalCopies();
            book.setTotalCopies(payload.getTotalCopies());
            book.setAvailableCopies(Math.max(0, book.getAvailableCopies() + difference));
        }

        return bookRepository.save(book);
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = getBookById(id);
        if (book.getAvailableCopies() < book.getTotalCopies()) {
            throw new RuntimeException("Cannot delete book while copies are currently issued to students.");
        }
        bookRepository.delete(book);
    }
}

