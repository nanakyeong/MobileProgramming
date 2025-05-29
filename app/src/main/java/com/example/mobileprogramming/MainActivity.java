package com.example.mobileprogramming;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 바이트 배열로 전달된 이미지가 있으면 바로 썸네일로 적용
        Intent intent = getIntent();
        byte[] imageBytes = intent.getByteArrayExtra("bookImage");
        if (imageBytes != null && imageBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            if (bitmap != null) {
                ImageView imageCover = findViewById(R.id.bookCover);
                imageCover.setImageBitmap(bitmap);
                imageCover.setVisibility(View.VISIBLE);
            }
        }

        TextView textTitle = findViewById(R.id.bookTitle);
        textTitle.setVisibility(View.GONE);

        AppDatabase db = AppDatabase.getInstance(this);
        BookDao bookDao = db.bookDao();

        ImageView imageNote = findViewById(R.id.ic_note);
        imageNote.setOnClickListener(v -> {
            Intent reportIntent = new Intent(MainActivity.this, BookReportActivity.class);
            startActivity(reportIntent);
        });

        ImageView imageCover = findViewById(R.id.bookCover);

        new Thread(() -> {
            Book latestBook = bookDao.getLatestBook();
            runOnUiThread(() -> {
                textTitle.setVisibility(View.VISIBLE);
                if (latestBook != null) {
                    textTitle.setText(latestBook.getTitle());

                    byte[] imageBytesFromIntent = getIntent().getByteArrayExtra("bookImage");
                    if (imageBytesFromIntent != null && imageBytesFromIntent.length > 0) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytesFromIntent, 0, imageBytesFromIntent.length);
                        imageCover.setImageBitmap(bitmap);
                        imageCover.setVisibility(View.VISIBLE);
                    } else if (latestBook.getImagePath() != null) {
                        Bitmap bitmap = BitmapFactory.decodeFile(latestBook.getImagePath());
                        imageCover.setImageBitmap(bitmap);
                        Log.d("BOOK_LOG", "📸 이미지 파일 로드됨: " + latestBook.getImagePath());
                    }

                    imageCover.setOnClickListener(v -> {
                        Intent reportIntent = new Intent(MainActivity.this, BookReportActivity.class);
                        Log.d("IMAGE_PATH", "Image path: " + latestBook.getImagePath());
                        reportIntent.putExtra("bookId", latestBook.getId());
                        reportIntent.putExtra("bookTitle", latestBook.getTitle());
                        reportIntent.putExtra("bookAuthor", latestBook.getAuthor());
                        reportIntent.putExtra("bookQuote", latestBook.getQuote());
                        reportIntent.putExtra("bookThoughts", latestBook.getThoughts());
                        reportIntent.putExtra("bookImagePath", latestBook.getImagePath());
                        Log.d(latestBook.getImagePath(), "onCreate: 뜬다");
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

        TextView textTitle = findViewById(R.id.bookTitle);
        ImageView imageCover = findViewById(R.id.bookCover);
        AppDatabase db = AppDatabase.getInstance(this);
        BookDao bookDao = db.bookDao();

        new Thread(() -> {
            Book latestBook = bookDao.getLatestBook();
            runOnUiThread(() -> {
                if (latestBook != null && latestBook.getTitle() != null && !latestBook.getTitle().isEmpty()) {
                    textTitle.setText(latestBook.getTitle());
                    textTitle.setVisibility(View.VISIBLE);

                    if (latestBook.getImagePath() != null) {
                        Bitmap bitmap = BitmapFactory.decodeFile(latestBook.getImagePath());
                        if (bitmap != null) {
                            imageCover.setImageBitmap(bitmap);
                            imageCover.setVisibility(View.VISIBLE);
                        } else {
                            imageCover.setVisibility(View.GONE);
                        }
                    } else {
                        imageCover.setVisibility(View.GONE);
                    }

                    imageCover.setOnClickListener(v -> {
                        Intent reportIntent = new Intent(MainActivity.this, BookReportActivity.class);
                        reportIntent.putExtra("bookId", latestBook.getId());
                        reportIntent.putExtra("bookTitle", latestBook.getTitle());
                        reportIntent.putExtra("bookAuthor", latestBook.getAuthor());
                        reportIntent.putExtra("bookQuote", latestBook.getQuote());
                        reportIntent.putExtra("bookThoughts", latestBook.getThoughts());
                        reportIntent.putExtra("bookImagePath", latestBook.getImagePath());
                        reportIntent.putExtra("fromHome", true);
                        startActivity(reportIntent);
                    });
                } else {
                    textTitle.setText("");
                    textTitle.setVisibility(View.GONE);
                    imageCover.setImageDrawable(null);
                    imageCover.setVisibility(View.GONE);
                }
            });
        }).start();
    }
}