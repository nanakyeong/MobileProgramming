package com.example.mobileprogramming;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BookDao {

    @Query("SELECT * FROM Book")
    List<Book> getAllBooks();

    @Insert
    void insertBook(Book book);

    @Query("SELECT * FROM Book WHERE id = :id LIMIT 1")
    Book getBookById(int id);

    @Query("SELECT * FROM Book ORDER BY id DESC LIMIT 1")
    Book getLatestBook();
    @Update
    void updateBook(Book book);

    @Delete
    void deleteBook(Book book);
}