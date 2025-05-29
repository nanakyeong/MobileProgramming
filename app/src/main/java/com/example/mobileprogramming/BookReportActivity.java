package com.example.mobileprogramming;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.drawable.Drawable;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookReportActivity extends AppCompatActivity {

    private ImageView imageBookCover;
    private TextView textTitle, textAuthor;
    private EditText edit_favorite_quote, edit_thought;
    private Button buttonSearch;

    private BookDao bookDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookreport);

        imageBookCover = findViewById(R.id.image_book_cover);
        textTitle = findViewById(R.id.text_book_title);
        textAuthor = findViewById(R.id.text_book_author);
        edit_favorite_quote = findViewById(R.id.edit_favorite_quote);
        edit_thought = findViewById(R.id.edit_thoughts);
        buttonSearch = findViewById(R.id.button_search);

        AppDatabase db = AppDatabase.getInstance(this);
        bookDao = db.bookDao();

        buttonSearch.setOnClickListener(v -> {
            String query = textTitle.getText().toString().trim();
            if (query.isEmpty()) {
                Toast.makeText(this, "책 제목을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            GoogleBooksApi api = ApiClient.getClient().create(GoogleBooksApi.class);
            String apiKey = BuildConfig.GOOGLE_API_KEY;

            Call<GoogleBooksResponse> call = api.searchBooks(query, apiKey);
            call.enqueue(new Callback<GoogleBooksResponse>() {
                @Override
                public void onResponse(Call<GoogleBooksResponse> call, Response<GoogleBooksResponse> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().items.isEmpty()) {
                        GoogleBooksResponse.Item book = response.body().items.get(0);

                        textTitle.setText(book.volumeInfo.title);
                        textAuthor.setText(book.volumeInfo.authors.get(0));
                        Glide.with(BookReportActivity.this)
                                .load(book.volumeInfo.imageLinks.thumbnail)
                                .into(imageBookCover);
                    } else {
                        Toast.makeText(BookReportActivity.this, "책 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<GoogleBooksResponse> call, Throwable t) {
                    Toast.makeText(BookReportActivity.this, "API 호출 실패", Toast.LENGTH_SHORT).show();
                }
            });
        });

        Button buttonRegister = findViewById(R.id.button);
        buttonRegister.setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_book, null);
            EditText editTitle = dialogView.findViewById(R.id.editTitle);
            EditText editAuthor = dialogView.findViewById(R.id.editAuthor);
            ImageView imageCover = dialogView.findViewById(R.id.imageCover);
            Button buttonSearchInsideDialog = dialogView.findViewById(R.id.buttonSearchInsideDialog);

            buttonSearchInsideDialog.setOnClickListener(v1 -> {
                String query = editTitle.getText().toString().trim();
                if (query.isEmpty()) {
                    Toast.makeText(BookReportActivity.this, "책 제목을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                GoogleBooksApi api = ApiClient.getClient().create(GoogleBooksApi.class);
                String apiKey = BuildConfig.GOOGLE_API_KEY;

                Call<GoogleBooksResponse> call = api.searchBooks(query, apiKey);
                call.enqueue(new Callback<GoogleBooksResponse>() {
                    @Override
                    public void onResponse(Call<GoogleBooksResponse> call, Response<GoogleBooksResponse> response) {

                        if (!response.isSuccessful() && response.errorBody() != null) {
                            try {
                                Log.e("BOOK_API", "에러 메시지: " + response.errorBody().string());
                            } catch (java.io.IOException e) {
                                Log.e("BOOK_API", "에러 메시지 읽기 실패", e);
                            }
                        }

                        if (response.isSuccessful() && response.body() != null && !response.body().items.isEmpty()) {
                            GoogleBooksResponse.Item book = response.body().items.get(0);
                            editAuthor.setText(book.volumeInfo.authors.get(0));

                            if (book.volumeInfo.imageLinks != null && book.volumeInfo.imageLinks.thumbnail != null) {
                                String thumbnailUrl = book.volumeInfo.imageLinks.thumbnail.replace("http://", "https://");

                                Glide.with(BookReportActivity.this)
                                        .load(thumbnailUrl)
                                        .into(imageCover);

                                imageCover.setVisibility(View.VISIBLE);
                            } else {
                                imageCover.setVisibility(View.GONE);
                            }
                        } else {
                            Toast.makeText(BookReportActivity.this, "책 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<GoogleBooksResponse> call, Throwable t) {
                        Toast.makeText(BookReportActivity.this, "API 호출 실패", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            new androidx.appcompat.app.AlertDialog.Builder(BookReportActivity.this)
                .setTitle("책 등록하기")
                .setView(dialogView)
                .setPositiveButton("등록", (dialog, which) -> {
                    String title = editTitle.getText().toString().trim();
                    String author = editAuthor.getText().toString().trim();

                    if (title.isEmpty() || author.isEmpty()) {
                        Toast.makeText(BookReportActivity.this, "책 제목과 저자를 입력해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Drawable drawable = imageCover.getDrawable();
                    if (drawable != null) {
                        imageBookCover.setImageDrawable(drawable);
                        imageBookCover.setVisibility(View.VISIBLE);
                    }

                    textTitle.setText(title);
                    textAuthor.setText(author);
                    // Only set title, author, and image here.
                })
                .setNegativeButton("취소", null)
                .show();
            // Registration logic for saving the Book (with quote and thoughts) after dialog
        });

        // --- Add registration logic for main register button ---
        EditText editQuote = findViewById(R.id.edit_favorite_quote);
        EditText editThoughts = findViewById(R.id.edit_thoughts);
        Button buttonRegisterMain = findViewById(R.id.button_search); // replace with your actual button ID

        buttonRegisterMain.setOnClickListener(view -> {
            Log.d("BOOK_LOG", "등록 버튼 클릭됨");
            String title = textTitle.getText().toString().trim();
            String author = textAuthor.getText().toString().trim();
            String quote = editQuote.getText().toString().trim();
            String thoughts = editThoughts.getText().toString().trim();

            Log.d("BOOK_LOG", "입력된 제목: " + title);
            Log.d("BOOK_LOG", "입력된 저자: " + author);
            Log.d("BOOK_LOG", "입력된 구절: " + quote);
            Log.d("BOOK_LOG", "입력된 느낀 점: " + thoughts);

            if (title.isEmpty() || author.isEmpty()) {
                Toast.makeText(BookReportActivity.this, "책 제목과 저자를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("BOOK_LOG", "DB에 저장할 Book 객체 생성됨");
            Book newBook = new Book(title, author, "book_cover", quote, thoughts);

            new Thread(() -> {
                try {
                    Log.d("BOOK_LOG", "bookDao.insertBook 호출");
                    bookDao.insertBook(newBook);
                    // After insertion, fetch updated books and update RecyclerView
                    java.util.List<Book> updatedBooks = bookDao.getAllBooks();
                    runOnUiThread(() -> {
                        Toast.makeText(BookReportActivity.this, "책이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(BookReportActivity.this, MainActivity.class);
                        intent.putExtra("bookTitle", newBook.getTitle());
                        intent.putExtra("bookAuthor", newBook.getAuthor());
                        // Instead of passing image path, pass the image as byte array from imageBookCover
                        Drawable drawable = imageBookCover.getDrawable();
                        if (drawable != null && drawable instanceof android.graphics.drawable.BitmapDrawable) {
                            android.graphics.Bitmap bitmap = ((android.graphics.drawable.BitmapDrawable) drawable).getBitmap();
                            java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] byteArray = stream.toByteArray();
                            intent.putExtra("bookImage", byteArray);
                        }
                        startActivity(intent);
                        finish(); // optional: close current activity
                    });
                } catch (Exception e) {
                    Log.e("BOOK_DB", "DB 저장 실패: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
        });
        // Handle intent extras to pre-fill fields if provided
        Intent intent = getIntent();
        if (intent != null) {
            String passedTitle = intent.getStringExtra("bookTitle");
            String passedAuthor = intent.getStringExtra("bookAuthor");
            String passedQuote = intent.getStringExtra("bookQuote");
            String passedThoughts = intent.getStringExtra("bookThoughts");
            byte[] imageBytes = intent.getByteArrayExtra("bookImage");

            if (passedTitle != null) {
                Log.d("BOOK_INTENT", "받은 제목: " + passedTitle);
            }
            if (passedAuthor != null) {
                Log.d("BOOK_INTENT", "받은 저자: " + passedAuthor);
            }
            Log.d("BOOK_INTENT", "받은 이미지 바이트: " + (imageBytes != null ? "존재함" : "없음"));

            if (passedTitle != null) {
                textTitle.setText(passedTitle);
                Log.d("BOOK_INTENT", "제목 설정됨: " + passedTitle);
            }
            if (passedAuthor != null) {
                textAuthor.setText(passedAuthor);
                Log.d("BOOK_INTENT", "저자 설정됨: " + passedAuthor);
            }
            if (passedQuote != null) edit_favorite_quote.setText(passedQuote);
            if (passedThoughts != null) edit_thought.setText(passedThoughts);
            if (imageBytes != null) {
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageBookCover.setImageBitmap(bitmap);
                Log.d("BOOK_INTENT", "이미지 설정됨");
            }
        }

        ImageView homeButton = findViewById(R.id.ic_home);
        if (homeButton != null) {
            homeButton.setOnClickListener(v -> {
                Intent intentHome = new Intent(BookReportActivity.this, MainActivity.class);

                String title = textTitle.getText().toString().trim();
                String author = textAuthor.getText().toString().trim();
                String quote = edit_favorite_quote.getText().toString().trim();
                String thoughts = edit_thought.getText().toString().trim();

                intentHome.putExtra("bookTitle", title);
                intentHome.putExtra("bookAuthor", author);
                intentHome.putExtra("bookQuote", quote);
                intentHome.putExtra("bookThoughts", thoughts);

                Drawable drawable = imageBookCover.getDrawable();
                if (drawable instanceof android.graphics.drawable.BitmapDrawable) {
                    android.graphics.Bitmap bitmap = ((android.graphics.drawable.BitmapDrawable) drawable).getBitmap();
                    java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    intentHome.putExtra("bookImage", byteArray);
                }

                startActivity(intentHome);
            });
        }
    }
}
