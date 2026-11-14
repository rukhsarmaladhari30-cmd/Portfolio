package com.library.controller;

import com.library.dto.LibraryDtos.BookRequestPayload;
import com.library.model.Book;
import com.library.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(bookService.searchBooks(q));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Book>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(bookService.getBooksByCategory(categoryId));
    }

    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody BookRequestPayload payload) {
        return ResponseEntity.ok(bookService.addBook(payload));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody BookRequestPayload payload) {
        return ResponseEntity.ok(bookService.updateBook(id, payload));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}

