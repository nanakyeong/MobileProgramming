package com.example.mobileprogramming;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Book {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String author;
    public String imagePath;
    public String quote;
    public String thoughts;

    public Book(String title, String author, String imagePath, String quote, String thoughts) {
        this.title = title;
        this.author = author;
        this.imagePath = imagePath;
        this.quote = quote;
        this.thoughts = thoughts;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getQuote() {
        return quote;
    }

    public String getThoughts() {
        return thoughts;
    }
}