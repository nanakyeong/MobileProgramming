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
}