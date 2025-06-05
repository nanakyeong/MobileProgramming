package com.example.mobileprogramming.Api;

import com.example.mobileprogramming.GoogleBooksResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleBooksApi {
    @GET("volumes")
    Call<GoogleBooksResponse> searchBooks(
            @Query("q") String query,
            @Query("key") String apiKey
    );


}