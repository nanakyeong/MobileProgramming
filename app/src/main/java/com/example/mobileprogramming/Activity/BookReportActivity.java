package com.example.mobileprogramming.Activity;

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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mobileprogramming.Api.ApiClient;
import com.example.mobileprogramming.AppDatabase;
import com.example.mobileprogramming.Book;
import com.example.mobileprogramming.BookDao;
import com.example.mobileprogramming.BuildConfig;
import com.example.mobileprogramming.Api.GoogleBooksApi;
import com.example.mobileprogramming.GoogleBooksResponse;
import com.example.mobileprogramming.QuizDataHolder;
import com.example.mobileprogramming.R;

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
        // Hide "책 등록하기" button if book is already passed via intent
        Intent checkIntent = getIntent();
        if (checkIntent != null) {
            String existingTitle = checkIntent.getStringExtra("bookTitle");
            if (existingTitle != null && !existingTitle.isEmpty()) {
                buttonRegister.setVisibility(View.GONE);
            }
        }
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
                            } catch (IOException e) {
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
                .setPositiveButton("확인", (dialog, which) -> {
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

                })
                .setNegativeButton("취소", null)
                .show();
        });

        EditText editQuote = findViewById(R.id.edit_favorite_quote);
        EditText editThoughts = findViewById(R.id.edit_thoughts);
        Button buttonRegisterMain = findViewById(R.id.button_search);


        Button editButton = findViewById(R.id.button_edit);
        Button deleteButton = findViewById(R.id.button_delete);

        Intent incomingIntent = getIntent();
        boolean fromHome = incomingIntent.getBooleanExtra("fromHome", false);

        if (fromHome) {
            editButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
            edit_favorite_quote.setEnabled(false);
            edit_thought.setEnabled(false);
        } else {
            editButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
            edit_favorite_quote.setEnabled(true);
            edit_thought.setEnabled(true);
        }

        // Click listener for edit button to enable editing
        editButton.setOnClickListener(v -> {
            edit_favorite_quote.setEnabled(true);
            edit_thought.setEnabled(true);
            Toast.makeText(BookReportActivity.this, "이제 수정 가능합니다.", Toast.LENGTH_SHORT).show();
        });

        // Click listener for delete button with confirmation dialog and DB deletion
        deleteButton.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(BookReportActivity.this)
                .setTitle("삭제 확인")
                .setMessage("이 독후감을 정말 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> {
                    Intent intent = getIntent();
                    int bookId = intent.getIntExtra("bookId", -1);
                    if (bookId != -1) {
                        new Thread(() -> {
                            BookDao bookDao = AppDatabase.getInstance(this).bookDao();
                            Book bookToDelete = bookDao.getBookById(bookId);
                            if (bookToDelete != null) {
                                bookDao.deleteBook(bookToDelete);
                                Log.d("BOOK_LOG", "✅ 삭제 완료: " + bookToDelete.getTitle());
                            } else {
                                Log.d("BOOK_LOG", "⚠️ 삭제할 Book을 찾을 수 없음");
                            }

                            runOnUiThread(() -> {
                                Toast.makeText(this, "독후감이 삭제되었습니다", Toast.LENGTH_SHORT).show();
                                Intent intentToMain = new Intent(this, MainActivity.class);
                                intentToMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intentToMain);
                                finish();
                            });
                        }).start();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
        });

        buttonRegisterMain.setOnClickListener(view -> {
            Log.d("BOOK_LOG", "등록 버튼 클릭됨");
            String title = textTitle.getText().toString().trim();
            String author = textAuthor.getText().toString().trim();
            String quote = editQuote.getText().toString().trim();
            String thoughts = editThoughts.getText().toString().trim();

            // 개별 입력항목별로 안내 메시지
            if (title.isEmpty()) {
                Toast.makeText(BookReportActivity.this, "책 제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (author.isEmpty()) {
                Toast.makeText(BookReportActivity.this, "저자 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (quote.isEmpty()) {
                Toast.makeText(BookReportActivity.this, "인상 깊은 구절을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (thoughts.isEmpty()) {
                Toast.makeText(BookReportActivity.this, "감상평을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            final int finalBookId = getIntent().getIntExtra("bookId", -1);
            new Thread(() -> {
                // If no duplicate, proceed to insert or update the book
                runOnUiThread(() -> {
                    Drawable drawable = imageBookCover.getDrawable();
                    String imagePath = null;

                    if (drawable != null) {
                        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                        File imageFile = saveBitmapToFile(bitmap);
                        if (imageFile != null) {
                            imagePath = imageFile.getAbsolutePath();
                        }
                    }

                    // Set readDateMillis to midnight today
                    java.util.Calendar calendar = java.util.Calendar.getInstance();
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
                    calendar.set(java.util.Calendar.MINUTE, 0);
                    calendar.set(java.util.Calendar.SECOND, 0);
                    calendar.set(java.util.Calendar.MILLISECOND, 0);
                    long readDateMillis = calendar.getTimeInMillis();
                    Log.d("BookSave", "저장되는 readDateMillis: " + readDateMillis);

                    Book bookToSave;
                    int readPages = 0;
                    if (finalBookId != -1) {
                        bookToSave = new Book(title, author, imagePath, quote, thoughts, readPages, readDateMillis);
                        bookToSave.setId(finalBookId);
                    } else {
                        bookToSave = new Book(title, author, imagePath, quote, thoughts, readPages, readDateMillis);
                    }

                    new Thread(() -> {
                        try {
                            if (finalBookId != -1) {
                                bookDao.updateBook(bookToSave);
                            } else {
                                bookDao.insertBook(bookToSave);
                            }

                            runOnUiThread(() -> {
                                Toast.makeText(BookReportActivity.this, "책이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                                Intent intentToMain = new Intent(BookReportActivity.this, MainActivity.class);
                                intentToMain.putExtra("bookTitle", bookToSave.getTitle());
                                intentToMain.putExtra("bookAuthor", bookToSave.getAuthor());

                                Drawable drawable1 = imageBookCover.getDrawable();
                                byte[] byteArray = null;
                                if (drawable1 instanceof BitmapDrawable) {
                                    Bitmap bitmap = ((BitmapDrawable) drawable1).getBitmap();
                                    java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                    byteArray = stream.toByteArray();
                                    intentToMain.putExtra("bookImage", byteArray);
                                }

                                QuizDataHolder.bookTitle.add(bookToSave.getTitle());
                                QuizDataHolder.bookImageBytes.add(byteArray);
                                startActivity(intentToMain);
                                finish();
                            });
                        } catch (Exception e) {
                            Log.e("BOOK_DB", "DB 저장 실패: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }).start();
                });
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

            // [ADD] Handle imagePath extra
            String imagePath = intent.getStringExtra("bookImagePath");

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
                Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageBookCover.setImageBitmap(bitmap);
                imageBookCover.setVisibility(View.VISIBLE);
                Log.d("BOOK_INTENT", "이미지 설정됨");
            }

            if (imagePath != null) {
                Log.d("BOOK_INTENT", "받은 이미지 경로: " + imagePath);
                Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(imagePath);
                imageBookCover.setImageBitmap(bitmap);
                imageBookCover.setVisibility(View.VISIBLE);
            }
        }

        ImageView homeButton = findViewById(R.id.ic_home);
        if (homeButton != null) {
            homeButton.setOnClickListener(v -> {
                Intent intentHome = new Intent(BookReportActivity.this, MainActivity.class);
                startActivity(intentHome);
            });
        }

        ImageView quizButton = findViewById(R.id.ic_quiz);
        if (quizButton != null) {
            quizButton.setOnClickListener(v -> {
                Intent intentQuiz = new Intent(BookReportActivity.this, QuizActivity.class);
                startActivity(intentQuiz);
            });
        }
    }

    private File saveBitmapToFile(Bitmap bitmap) {
        try {
            File file = new File(getFilesDir(), "book_cover_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            Log.d("BOOK_LOG", "저장된 이미지 절대경로: " + file.getAbsolutePath());
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
