package com.example.mobileprogramming.Activity;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.List;

import android.view.View;
import android.view.Gravity;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.example.mobileprogramming.AppDatabase;
import com.example.mobileprogramming.Book;
import com.example.mobileprogramming.BookDao;
import com.example.mobileprogramming.R;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        findViewById(R.id.buttonDateFilter).setOnClickListener(v -> {
            Intent calendarIntent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(calendarIntent);
        });

        Intent intent = getIntent();
        TextView textTitle = findViewById(R.id.bookTitle);
        byte[] imageBytes = intent.getByteArrayExtra("bookImage");
        String titleFromIntent = intent.getStringExtra("bookTitle");

        if (imageBytes != null && imageBytes.length > 0) {
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            } catch (Exception e) {
                Log.e("IMAGE_ERROR", "이미지 디코딩 중 오류 발생", e);
                runOnUiThread(() -> android.widget.Toast.makeText(this, "책 표지 이미지를 불러오는 중 오류가 발생했어요.", android.widget.Toast.LENGTH_SHORT).show());
            }
            if (bitmap != null) {
                ImageView imageCover = findViewById(R.id.bookCover);
                imageCover.setImageBitmap(bitmap);
                imageCover.setVisibility(View.VISIBLE);
                textTitle.setText(titleFromIntent != null ? titleFromIntent : ""); // set title if available
                textTitle.setVisibility(View.VISIBLE);
            }
        } else {
            textTitle.setVisibility(View.GONE);
        }

        AppDatabase db = AppDatabase.getInstance(this);
        BookDao bookDao = db.bookDao();

        ImageView imageNote = findViewById(R.id.ic_note);
        imageNote.setOnClickListener(v -> {
            Intent reportIntent = new Intent(MainActivity.this, BookReportActivity.class);
            startActivity(reportIntent);
        });

        ImageView imageQuiz = findViewById(R.id.ic_quiz);
        imageQuiz.setOnClickListener(v -> {
            Intent quizIntent = new Intent(MainActivity.this, QuizActivity.class);
            startActivity(quizIntent);
        });

        ImageView imageCover = findViewById(R.id.bookCover);

        new Thread(() -> {
            Book latestBook = null;
            try {
                latestBook = bookDao.getLatestBook();
            } catch (Exception e) {
                Log.e("DB_ERROR", "최신 책 조회 중 오류 발생", e);
                runOnUiThread(() -> android.widget.Toast.makeText(this, "최근 등록된 책 정보를 가져오는 데 실패했어요.", android.widget.Toast.LENGTH_SHORT).show());
            }
            final Book finalLatestBook = latestBook;
            runOnUiThread(() -> {
                textTitle.setVisibility(View.VISIBLE);
                if (finalLatestBook != null) {
                    textTitle.setText(finalLatestBook.getTitle());

                    byte[] imageBytesFromIntent = getIntent().getByteArrayExtra("bookImage");
                    if (imageBytesFromIntent != null && imageBytesFromIntent.length > 0) {
                        Bitmap bitmap = null;
                        try {
                            bitmap = BitmapFactory.decodeByteArray(imageBytesFromIntent, 0, imageBytesFromIntent.length);
                        } catch (Exception e) {
                            Log.e("IMAGE_ERROR", "이미지 디코딩 중 오류 발생", e);
                            runOnUiThread(() -> android.widget.Toast.makeText(this, "책 표지 이미지를 불러오는 중 오류가 발생했어요.", android.widget.Toast.LENGTH_SHORT).show());
                        }
                        if (bitmap != null) {
                            imageCover.setImageBitmap(bitmap);
                            imageCover.setVisibility(View.VISIBLE);
                        }
                    } else if (finalLatestBook.getImagePath() != null) {
                        try {
                            Bitmap bitmap = BitmapFactory.decodeFile(finalLatestBook.getImagePath());
                            if (bitmap != null) {
                                imageCover.setImageBitmap(bitmap);
                            } else {
                                Log.w("IMAGE_WARNING", "비트맵이 null입니다: " + finalLatestBook.getImagePath());
                                runOnUiThread(() -> android.widget.Toast.makeText(this, "책 이미지가 존재하지 않거나 손상되었어요.", android.widget.Toast.LENGTH_SHORT).show());
                            }
                        } catch (Exception e) {
                            Log.e("IMAGE_ERROR", "이미지 파일 로드 중 오류 발생", e);
                        }
                    }

                    imageCover.setOnClickListener(v -> {
                        Intent reportIntent = new Intent(MainActivity.this, BookReportActivity.class);
                        Log.d("IMAGE_PATH", "Image path: " + finalLatestBook.getImagePath());
                        reportIntent.putExtra("bookId", finalLatestBook.getId());
                        reportIntent.putExtra("bookTitle", finalLatestBook.getTitle());
                        reportIntent.putExtra("bookAuthor", finalLatestBook.getAuthor());
                        reportIntent.putExtra("bookQuote", finalLatestBook.getQuote());
                        reportIntent.putExtra("bookThoughts", finalLatestBook.getThoughts());
                        reportIntent.putExtra("bookImagePath", finalLatestBook.getImagePath());
                        Log.d(finalLatestBook.getImagePath(), "onCreate: 뜬다");
                        reportIntent.putExtra("fromHome", true);
                        startActivity(reportIntent);
                    });
                }
            });
        }).start();

    }

    @Override
    protected void onResume() {
        super.onResume();

        GridLayout bookGrid = findViewById(R.id.bookGrid);
        bookGrid.removeAllViews();

        AppDatabase db = AppDatabase.getInstance(this);
        BookDao bookDao = db.bookDao();

        new Thread(() -> {
            List<Book> allBooks = new java.util.ArrayList<>();
            try {
                allBooks = bookDao.getAllBooks();
            } catch (Exception e) {
                Log.e("DB_ERROR", "책 목록 조회 중 오류 발생", e);
                runOnUiThread(() -> android.widget.Toast.makeText(this, "책 목록을 불러오는 데 문제가 발생했어요.", android.widget.Toast.LENGTH_SHORT).show());
            }
            List<Book> finalAllBooks = allBooks;
            runOnUiThread(() -> {
                TextView totalCountText = findViewById(R.id.bookCountText);
                totalCountText.setText("전체보기(" + finalAllBooks.size() + ")");
                for (Book book : finalAllBooks) {
                    // Center-align both image and title, set spacing between items
                    LinearLayout itemLayout = new LinearLayout(this);
                    itemLayout.setOrientation(LinearLayout.VERTICAL);
                    itemLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                    itemLayout.setPadding(10, 10, 10, 10);
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = GridLayout.LayoutParams.WRAP_CONTENT;
                    params.setMargins(10, 10, 10, 10);
                    itemLayout.setLayoutParams(params);

                    // Set fixed image size and center
                    ImageView imageView = new ImageView(this);
                    LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(280, 430);
                    imageView.setLayoutParams(imageParams);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    if (book.getImagePath() != null) {
                        try {
                            Bitmap bitmap = BitmapFactory.decodeFile(book.getImagePath());
                            if (bitmap != null) {
                                imageView.setImageBitmap(bitmap);
                            } else {
                                Log.w("IMAGE_WARNING", "비트맵이 null입니다: " + book.getImagePath());
                                runOnUiThread(() -> android.widget.Toast.makeText(this, "책 이미지가 존재하지 않거나 손상되었어요.", android.widget.Toast.LENGTH_SHORT).show());
                            }
                        } catch (Exception e) {
                            Log.e("IMAGE_ERROR", "책 이미지 로드 중 오류 발생", e);
                        }
                    }

                    TextView titleView = new TextView(this);
                    titleView.setText(book.getTitle());
                    titleView.setTextSize(14);
                    titleView.setTypeface(titleView.getTypeface(), android.graphics.Typeface.BOLD);
                    LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    titleParams.gravity = Gravity.CENTER_HORIZONTAL;
                    titleView.setLayoutParams(titleParams);
                    titleView.setPadding(0, 8, 0, 0);
                    titleView.setGravity(Gravity.CENTER);
                    titleView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                    itemLayout.addView(imageView);
                    itemLayout.addView(titleView);

                    itemLayout.setOnClickListener(v -> {
                        Intent reportIntent = new Intent(MainActivity.this, BookReportActivity.class);
                        reportIntent.putExtra("bookId", book.getId());
                        reportIntent.putExtra("bookTitle", book.getTitle());
                        reportIntent.putExtra("bookAuthor", book.getAuthor());
                        reportIntent.putExtra("bookQuote", book.getQuote());
                        reportIntent.putExtra("bookThoughts", book.getThoughts());
                        reportIntent.putExtra("bookImagePath", book.getImagePath());
                        reportIntent.putExtra("fromHome", true);
                        startActivity(reportIntent);
                    });

                    bookGrid.addView(itemLayout);
                }

                // Add search functionality with cosine similarity
                EditText search = findViewById(R.id.search);
                search.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        String query = s.toString();
                        List<Book> filteredBooks = new java.util.ArrayList<>();
                        for (Book book : finalAllBooks) {
                            if (calculateCosineSimilarity(book.getTitle(), query) > 0.3) { // Adjust threshold as needed
                                filteredBooks.add(book);
                            }
                        }

                        bookGrid.removeAllViews();
                        for (Book book : filteredBooks) {
                            // Reuse the book display layout logic from runOnUiThread
                            LinearLayout itemLayout = new LinearLayout(MainActivity.this);
                            itemLayout.setOrientation(LinearLayout.VERTICAL);
                            itemLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                            itemLayout.setPadding(13, 13, 13, 13);
                            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                            params.width = GridLayout.LayoutParams.WRAP_CONTENT;
                            params.setMargins(13, 13, 13, 13);
                            itemLayout.setLayoutParams(params);

                            ImageView imageView = new ImageView(MainActivity.this);
                            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(240, 420);
                            imageParams.gravity = Gravity.CENTER_HORIZONTAL;
                            imageView.setLayoutParams(imageParams);
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                            if (book.getImagePath() != null) {
                                try {
                                    Bitmap bitmap = BitmapFactory.decodeFile(book.getImagePath());
                                    if (bitmap != null) {
                                        imageView.setImageBitmap(bitmap);
                                    } else {
                                        Log.w("IMAGE_WARNING", "비트맵이 null입니다: " + book.getImagePath());
                                        runOnUiThread(() -> android.widget.Toast.makeText(MainActivity.this, "책 이미지가 존재하지 않거나 손상되었어요.", android.widget.Toast.LENGTH_SHORT).show());
                                    }
                                } catch (Exception e) {
                                    Log.e("IMAGE_ERROR", "책 이미지 로드 중 오류 발생", e);
                                }
                            }

                            TextView titleView = new TextView(MainActivity.this);
                            titleView.setText(book.getTitle());
                            titleView.setTextSize(14);
                            titleView.setTypeface(titleView.getTypeface(), android.graphics.Typeface.BOLD);
                            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            titleParams.gravity = Gravity.CENTER_HORIZONTAL;
                            titleView.setLayoutParams(titleParams);
                            titleView.setPadding(0, 5, 0, 0);
                            titleView.setGravity(Gravity.CENTER);
                            titleView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                            itemLayout.addView(imageView);
                            itemLayout.addView(titleView);

                            itemLayout.setOnClickListener(v -> {
                                Intent reportIntent = new Intent(MainActivity.this, BookReportActivity.class);
                                reportIntent.putExtra("bookId", book.getId());
                                reportIntent.putExtra("bookTitle", book.getTitle());
                                reportIntent.putExtra("bookAuthor", book.getAuthor());
                                reportIntent.putExtra("bookQuote", book.getQuote());
                                reportIntent.putExtra("bookThoughts", book.getThoughts());
                                reportIntent.putExtra("bookImagePath", book.getImagePath());
                                reportIntent.putExtra("fromHome", true);
                                startActivity(reportIntent);
                            });

                            bookGrid.addView(itemLayout);
                        }
                    }
                });
            });
        }).start();
    }

    // Cosine similarity method for search
    private double calculateCosineSimilarity(String str1, String str2) {
        str1 = str1.toLowerCase();
        str2 = str2.toLowerCase();
        Map<Character, Integer> freq1 = new HashMap<>();
        Map<Character, Integer> freq2 = new HashMap<>();

        for (char c : str1.toCharArray()) {
            freq1.put(c, freq1.getOrDefault(c, 0) + 1);
        }
        for (char c : str2.toCharArray()) {
            freq2.put(c, freq2.getOrDefault(c, 0) + 1);
        }

        Set<Character> allChars = new HashSet<>();
        allChars.addAll(freq1.keySet());
        allChars.addAll(freq2.keySet());

        double dotProduct = 0;
        double normA = 0;
        double normB = 0;

        for (char c : allChars) {
            int x = freq1.getOrDefault(c, 0);
            int y = freq2.getOrDefault(c, 0);
            dotProduct += x * y;
            normA += x * x;
            normB += y * y;
        }

        if (normA == 0 || normB == 0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}