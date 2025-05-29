package com.example.mobileprogramming;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;

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

        AppDatabase db = AppDatabase.getInstance(this);
        BookDao bookDao = db.bookDao();

        ImageView imageNote = findViewById(R.id.ic_note);
        imageNote.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BookReportActivity.class);
            startActivity(intent);
        });

        ImageView imageCover = findViewById(R.id.bookCover);

        new Thread(() -> {
            Book latestBook = bookDao.getLatestBook();
            if (latestBook != null && latestBook.getImagePath() != null) {
                runOnUiThread(() -> {
                    byte[] byteArray = getIntent().getByteArrayExtra("bookImage");
                    if (byteArray != null && byteArray.length > 0) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                        imageCover.setImageBitmap(bitmap);

                        imageCover.setOnClickListener(v -> {
                            Intent intent = new Intent(MainActivity.this, BookReportActivity.class);
                            intent.putExtra("bookId", latestBook.getId());
                            intent.putExtra("bookTitle", latestBook.getTitle());
                            intent.putExtra("bookAuthor", latestBook.getAuthor());
                            intent.putExtra("bookQuote", latestBook.getQuote());
                            intent.putExtra("bookThoughts", latestBook.getThoughts());
                            intent.putExtra("bookImage", byteArray);
                            startActivity(intent);
                        });
                    }
                });
                    TextView textTitle = findViewById(R.id.bookTitle);
                    textTitle.setText(latestBook.getTitle());

                    byte[] byteArray = getIntent().getByteArrayExtra("bookImage");
                    if (byteArray != null && byteArray.length > 0) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                        imageCover.setImageBitmap(bitmap);
                    }

            }
        }).start();

    }
}