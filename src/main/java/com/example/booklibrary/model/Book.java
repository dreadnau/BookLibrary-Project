package com.example.booklibrary.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@Entity
@Table(name = "books")
public class Book {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title cannot be empty")
    private String title;

    @NotBlank(message = "Author cannot be empty")
    private String author;

    // restrict to allowed genres
    @Pattern(regexp = "Fiction|Non-Fiction|Sci-Fi|History", message = "Genre must be one of: Fiction, Non-Fiction, Sci-Fi, History")
    private String genre;

    @PastOrPresent(message = "Published date cannot be in the future")
    private LocalDate publishedDate;

    private Boolean availabilityStatus = true;

    // Constructors
    public Book() {}

    public Book(String title, String author, String genre, LocalDate publishedDate, Boolean availabilityStatus) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.publishedDate = publishedDate;
        this.availabilityStatus = availabilityStatus;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public LocalDate getPublishedDate() { return publishedDate; }
    public void setPublishedDate(LocalDate publishedDate) { this.publishedDate = publishedDate; }

    public Boolean getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(Boolean availabilityStatus) { this.availabilityStatus = availabilityStatus; }
}
