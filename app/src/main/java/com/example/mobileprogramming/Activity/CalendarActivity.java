package com.example.mobileprogramming.Activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobileprogramming.AppDatabase;
import com.example.mobileprogramming.Book;
import com.example.mobileprogramming.R;
import com.example.mobileprogramming.ReadDayDecorator;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private TextView textSelectedDate;
    private TextView textBooksOnDate;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);

        MaterialCalendarView calendarView = findViewById(R.id.calendarView);

        new Thread(() -> {
            db = AppDatabase.getInstance(getApplicationContext());
            List<Book> readBooks = db.bookDao().getBooksWithReadDate();

            List<Long> readDates = new java.util.ArrayList<>();
            for (Book book : readBooks) {
                if (book.getReadDateMillis() > 0) {
                    readDates.add(book.getReadDateMillis());
                }
            }

            for (Long date : readDates) {
                Log.d("CalendarActivity", "📅 읽은 날짜: " + date);
            }

            runOnUiThread(() -> {
                calendarView.addDecorator(new ReadDayDecorator(readDates));
            });
        }).start();

        this.calendarView = calendarView;
        textSelectedDate = findViewById(R.id.text_selected_date);
        textBooksOnDate = findViewById(R.id.text_books_on_date);
        db = AppDatabase.getInstance(getApplicationContext());

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            long millis = convertToMillis(date);
            textSelectedDate.setText(formatDate(millis));
            loadBooksOnDate(millis);
        });
    }

    private long convertToMillis(CalendarDay day) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(day.getYear(), day.getMonth(), day.getDay());
        return calendar.getTimeInMillis();
    }

    private String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA);
        return sdf.format(new Date(millis));
    }

    private void loadBooksOnDate(long millis) {
        new Thread(() -> {
            long startOfDay = getStartOfDay(millis);
            long endOfDay = getEndOfDay(millis);

            List<Book> allBooks = db.bookDao().getAllBooks();
            for (Book b : allBooks) {
                Log.d("CalendarDebug", "전체 책 readDateMillis: " + b.getReadDateMillis() + " (" + b.getTitle() + ")");
            }
            List<Book> books = db.bookDao().getBooksByDate(startOfDay, endOfDay);

            runOnUiThread(() -> {
                if (books.isEmpty()) {
                    textBooksOnDate.setText("이 날에는 독서 기록이 없습니다.");
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (Book book : books) {
                        sb.append("📚 ").append(book.getTitle()).append("\n");
                    }
                    textBooksOnDate.setText(sb.toString().trim());
                }
            });
        }).start();
    }

    private long getStartOfDay(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getEndOfDay(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
}
