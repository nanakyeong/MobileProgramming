package com.example.mobileprogramming;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.util.List;

import android.view.View;
import android.view.Gravity;

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

        Intent intent = getIntent();
        TextView textTitle = findViewById(R.id.bookTitle);
        byte[] imageBytes = intent.getByteArrayExtra("bookImage");
        String titleFromIntent = intent.getStringExtra("bookTitle");

        if (imageBytes != null && imageBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
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

        GridLayout bookGrid = findViewById(R.id.bookGrid);
        bookGrid.removeAllViews();

        AppDatabase db = AppDatabase.getInstance(this);
        BookDao bookDao = db.bookDao();

        new Thread(() -> {
            List<Book> allBooks = bookDao.getAllBooks();
            runOnUiThread(() -> {
                TextView totalCountText = findViewById(R.id.bookCountText);
                totalCountText.setText("전체보기(" + allBooks.size() + ")");
                for (Book book : allBooks) {
                    // Center-align both image and title, set spacing between items
                    LinearLayout itemLayout = new LinearLayout(this);
                    itemLayout.setOrientation(LinearLayout.VERTICAL);
                    itemLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                    itemLayout.setPadding(16, 16, 16, 16);
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = GridLayout.LayoutParams.WRAP_CONTENT;
                    params.setMargins(16, 16, 16, 16);
                    itemLayout.setLayoutParams(params);

                    // Set fixed image size and center
                    ImageView imageView = new ImageView(this);
                    LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(200, 280); // adjust size as needed
                    imageParams.gravity = Gravity.CENTER_HORIZONTAL;
                    imageView.setLayoutParams(imageParams);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    if (book.getImagePath() != null) {
                        Bitmap bitmap = BitmapFactory.decodeFile(book.getImagePath());
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
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
            });
        }).start();
    }
}