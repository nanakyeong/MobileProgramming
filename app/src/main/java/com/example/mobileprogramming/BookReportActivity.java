package com.example.mobileprogramming;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookReportActivity extends AppCompatActivity {

    private ImageView imageBookCover;
    private TextView textTitle, textAuthor;
    private EditText editQuote, editThoughts;
    private Button buttonSearch;

    private BookDao bookDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookreport);

        // 1. View 연결
        imageBookCover = findViewById(R.id.image_book_cover);
        textTitle = findViewById(R.id.text_book_title);
        textAuthor = findViewById(R.id.text_book_author);
        editQuote = findViewById(R.id.edit_favorite_quote);
        editThoughts = findViewById(R.id.edit_thoughts);
        buttonSearch = findViewById(R.id.button_search);

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
            // 팝업에 들어갈 View를 동적으로 생성
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_book, null);
            EditText editTitle = dialogView.findViewById(R.id.editTitle);
            EditText editAuthor = dialogView.findViewById(R.id.editAuthor);

            new androidx.appcompat.app.AlertDialog.Builder(BookReportActivity.this)
                .setTitle("책 등록하기")
                .setView(dialogView)
                .setPositiveButton("등록", (dialog, which) -> {
                    String title = editTitle.getText().toString().trim();
                    String author = editAuthor.getText().toString().trim();
                    String quote = editQuote.getText().toString().trim();
                    String thoughts = editThoughts.getText().toString().trim();

                    if (title.isEmpty() || author.isEmpty()) {
                        Toast.makeText(BookReportActivity.this, "책 제목과 저자를 입력해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Book newBook = new Book(title, author, "book_cover", quote, thoughts);

                    new Thread(() -> {
                        bookDao.insertBook(newBook);
                        runOnUiThread(() -> Toast.makeText(BookReportActivity.this, "책이 등록되었습니다.", Toast.LENGTH_SHORT).show());
                    }).start();
                })
                .setNegativeButton("취소", null)
                .show();
        });

        // 2. DB 인스턴스 가져오기
        AppDatabase db = AppDatabase.getInstance(this);
        BookDao bookDao = db.bookDao();

        // 3. 예시로 id=1인 책 가져오기 (MainActivity에서 intent로 id 넘겨줄 수도 있음)
        new Thread(() -> {
            Book book = bookDao.getBookById(1);

            runOnUiThread(() -> {
                if (book != null) {
                    textTitle.setText(book.title);
                    textAuthor.setText(book.author);
                    editQuote.setText(book.quote);
                    editThoughts.setText(book.thoughts);

                    int resId = getResources().getIdentifier(book.imagePath, "drawable", getPackageName());
                    imageBookCover.setImageResource(resId);
                }
            });
        }).start();
    }
}
