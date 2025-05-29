package com.example.mobileprogramming;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class QuizActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz);

        GridLayout quizGrid = findViewById(R.id.quiz_bookGrid);
        if (quizGrid != null) {
            quizGrid.removeAllViews();
            quizGrid.setColumnCount(3); // 3 columns from left to right

            for (int i = 0; i < QuizDataHolder.bookTitle.size(); i++) {
                String title = QuizDataHolder.bookTitle.get(i);
                byte[] imageBytes = QuizDataHolder.bookImageBytes.get(i);

                Log.d("QUIZ_INTENT", "받은 제목: " + title);
                Log.d("QUIZ_INTENT", "받은 이미지 바이트: " + (imageBytes != null ? "존재함" : "없음"));

                // Create vertical LinearLayout for each item
                LinearLayout itemLayout = new LinearLayout(this);
                itemLayout.setOrientation(LinearLayout.VERTICAL);
                itemLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams();
                gridParams.width = 0;
                gridParams.height = LayoutParams.WRAP_CONTENT;
                gridParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                itemLayout.setLayoutParams(gridParams);
                int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
                itemLayout.setPadding(padding, padding, padding, padding);

                // ImageView
                ImageView imageView = new ImageView(this);
                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics())
                );
                imageParams.gravity = Gravity.CENTER_HORIZONTAL;
                imageView.setLayoutParams(imageParams);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                if (imageBytes != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(android.R.drawable.ic_menu_report_image);
                }
                itemLayout.addView(imageView);

                // Title
                TextView titleView = new TextView(this);
                titleView.setText(title);
                titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                titleView.setGravity(Gravity.CENTER_HORIZONTAL);
                itemLayout.addView(titleView);

                quizGrid.addView(itemLayout);
            }
        }
        populateBookGrid();
    }

    private void populateBookGrid() {
        GridLayout bookGrid = findViewById(R.id.bookGrid);
        if (bookGrid == null) return;

        // Open or create the database
        SQLiteDatabase db = openOrCreateDatabase("books.db", MODE_PRIVATE, null);
        // Query: select id, title, thumbnail from books
        Cursor cursor = db.rawQuery("SELECT id, title, thumbnail FROM books", null);
        if (cursor != null) {
            int titleIdx = cursor.getColumnIndex("title");
            int thumbIdx = cursor.getColumnIndex("thumbnail");
            while (cursor.moveToNext()) {
                String title = cursor.getString(titleIdx);
                String thumbnailPath = cursor.getString(thumbIdx);

                // Create a vertical LinearLayout for the book item
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

                // ImageView for thumbnail
                ImageView imageView = new ImageView(this);
                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics()),
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics())
                );
                imageParams.gravity = Gravity.CENTER_HORIZONTAL;
                imageView.setLayoutParams(imageParams);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                // Load image from file
                Bitmap bitmap = BitmapFactory.decodeFile(thumbnailPath);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    // Set placeholder if needed
                    imageView.setImageResource(android.R.drawable.ic_menu_report_image);
                }
                itemLayout.addView(imageView);

                // TextView for title
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

                // Add to grid
                bookGrid.addView(itemLayout);
            }
            cursor.close();
        }
        db.close();
    }
}