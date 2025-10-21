package com.example.booklibrary.service;

import com.example.booklibrary.exception.BookNotFoundException;
import com.example.booklibrary.model.Book;
import com.example.booklibrary.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
    private final BookRepository repo;

    public BookService(BookRepository repo) {
        this.repo = repo;
    }

    public Book addBook(Book book) {
        return repo.save(book);
    }

    public List<Book> getAllBooks() {
        return repo.findAll();
    }

    public Book getBookById(Long id) {
        return repo.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    public Book updateBook(Long id, Book newBook) {
        Book existing = getBookById(id);
        existing.setTitle(newBook.getTitle());
        existing.setAuthor(newBook.getAuthor());
        existing.setGenre(newBook.getGenre());
        existing.setPublishedDate(newBook.getPublishedDate());
        existing.setAvailabilityStatus(newBook.getAvailabilityStatus());
        return repo.save(existing);
    }

    public void deleteBook(Long id) {
        // Throw if not exists
        if (!repo.existsById(id)) throw new BookNotFoundException(id);
        repo.deleteById(id);
    }
}
