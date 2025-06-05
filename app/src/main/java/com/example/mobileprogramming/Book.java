package com.example.mobileprogramming;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.Getter;
import lombok.Setter;
import retrofit2.http.GET;

@Entity
@Getter
@Setter
public class Book {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String author;
    public String imagePath;
    public String quote;
    public String thoughts;

    public int readPages;
    public long readDateMillis;

    public Book(String title, String author, String imagePath, String quote, String thoughts, int readPages, long readDateMillis) {
        this.title = title;
        this.author = author;
        this.imagePath = imagePath;
        this.quote = quote;
        this.thoughts = thoughts;
        this.readPages = readPages;
        this.readDateMillis = readDateMillis;
    }
}