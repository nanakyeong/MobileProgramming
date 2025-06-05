package com.example.mobileprogramming.Activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;

import com.example.mobileprogramming.AppDatabase;
import com.example.mobileprogramming.Book;
import com.example.mobileprogramming.R;

import java.util.List;

public class QuizActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz);

        ImageView homeButton = findViewById(R.id.ic_home);
        if (homeButton != null) {
            homeButton.setOnClickListener(v -> {
                Intent intentHome = new Intent(QuizActivity.this, MainActivity.class);
                startActivity(intentHome);
            });
        }

        ImageView noteButton = findViewById(R.id.ic_note);
        if (noteButton != null) {
            noteButton.setOnClickListener(v -> {
                Intent intentNote = new Intent(QuizActivity.this, BookReportActivity.class);
                startActivity(intentNote);
            });
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                List<Book> bookList = db.bookDao().getAllBooks();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateQuizUI(bookList);
                    }
                });
            }
        }).start();
    }

    // Updated to match main screen: three-column horizontal layout, image and title centered
    private void updateQuizUI(List<Book> bookList) {
        GridLayout quizGrid = findViewById(R.id.quiz_bookGrid);
        if (quizGrid != null) {
            quizGrid.removeAllViews();
            quizGrid.setColumnCount(3); // Ensure horizontal row-wise arrangement

            for (Book book : bookList) {
                String title = book.getTitle();
                String author = book.getAuthor();
                String imagePath = book.getImagePath();

                // Create a vertical LinearLayout for each book item
                LinearLayout itemLayout = new LinearLayout(this);
                itemLayout.setOrientation(LinearLayout.VERTICAL);
                itemLayout.setGravity(Gravity.CENTER_HORIZONTAL);

                GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams();
                gridParams.width = GridLayout.LayoutParams.WRAP_CONTENT;
                gridParams.height = LayoutParams.WRAP_CONTENT;
                gridParams.setMargins(0, 5, 0, 5); // Optional: Add spacing between items
                itemLayout.setLayoutParams(gridParams);
                int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
                itemLayout.setPadding(padding, padding, padding, padding);

                // ImageView setup
                ImageView imageView = new ImageView(this);
                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()),
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics())
                );
                imageParams.gravity = Gravity.CENTER_HORIZONTAL;
                imageView.setLayoutParams(imageParams);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(android.R.drawable.ic_menu_report_image);
                }
                imageView.setOnClickListener(v -> {
                    Intent intent = new Intent(QuizActivity.this, QuizDetailActivity.class);
                    intent.putExtra("bookTitle", title);
                    intent.putExtra("bookAuthor", author);
                    intent.putExtra("bookImagePath", imagePath);
                    startActivity(intent);
                });
                itemLayout.addView(imageView);

                // Title TextView setup
                TextView titleView = new TextView(this);
                titleView.setText(title);
                titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                titleView.setGravity(Gravity.CENTER_HORIZONTAL);
                titleView.setMaxLines(2);
                titleView.setLayoutParams(new LinearLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
                ));
                itemLayout.addView(titleView);

                // Add click listener to open QuizDetailActivity with book details
                itemLayout.setOnClickListener(v -> {
                    Intent intent = new Intent(QuizActivity.this, QuizDetailActivity.class);
                    intent.putExtra("bookTitle", title);
                    intent.putExtra("bookAuthor", author);
                    intent.putExtra("bookImagePath", imagePath);
                    startActivity(intent);
                });

                // Add item layout to GridLayout
                quizGrid.addView(itemLayout);
            }
        }
    }

    private void populateBookGrid() {
        GridLayout bookGrid = findViewById(R.id.quiz_bookGrid);
        if (bookGrid == null) return;

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        List<Book> books = db.bookDao().getAllBooks();

        for (Book book : books) {
            String title = book.getTitle();
            String thumbnailPath = book.getImagePath();

            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams();
            gridParams.width = 0;
            gridParams.height = LayoutParams.WRAP_CONTENT;
            gridParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            itemLayout.setLayoutParams(gridParams);
            itemLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
            itemLayout.setPadding(padding, padding, padding, padding);

            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 130, getResources().getDisplayMetrics())
            );
            imageParams.gravity = Gravity.CENTER_HORIZONTAL;
            imageView.setLayoutParams(imageParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            Bitmap bitmap = BitmapFactory.decodeFile(thumbnailPath);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(android.R.drawable.ic_menu_report_image);
            }
            itemLayout.addView(imageView);

            TextView titleView = new TextView(this);
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
            );
            titleParams.gravity = Gravity.CENTER_HORIZONTAL;
            titleView.setLayoutParams(titleParams);
            titleView.setText(title);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            titleView.setMaxLines(2);
            titleView.setGravity(Gravity.CENTER_HORIZONTAL);
            itemLayout.addView(titleView);

            bookGrid.addView(itemLayout);
        }
    }
}