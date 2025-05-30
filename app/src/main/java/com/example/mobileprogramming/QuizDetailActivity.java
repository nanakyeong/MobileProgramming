package com.example.mobileprogramming;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

// Removed incorrect Callback import

import okhttp3.Callback;
import okhttp3.Response;

public class QuizDetailActivity extends AppCompatActivity {

    private TextView bookTitle;
    private TextView quizQuestion;
    private Button buttonO;
    private Button buttonX;

    private String prompt; // Make prompt a class field

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_1);
        bookTitle = findViewById(R.id.quiz_question);
        quizQuestion = findViewById(R.id.quiz_question);
        buttonO = findViewById(R.id.quiz_o);
        buttonX = findViewById(R.id.quiz_x);

        String title = getIntent().getStringExtra("bookTitle");
        String author = getIntent().getStringExtra("bookAuthor");

        Log.d("QUIZ_DETAIL_INTENT", "제목: " + title);
        Log.d("QUIZ_DETAIL_INTENT", "저자: " + author);

        loadNextQuiz();
    }

    private void loadNextQuiz() {
        String title = getIntent().getStringExtra("bookTitle");
        String author = getIntent().getStringExtra("bookAuthor");

        prompt = "책의 내용을 바탕으로 창의적이고 다양한 OX 퀴즈를 만들어줘. 단순한 저자나 제목 정보가 아니라, 책의 줄거리나 주요 장면, 인물, 주제 등에 기반한 퀴즈를 생성하고, 매번 새로운 문제를 제시해줘. 이전과 중복되지 않도록 해줘.\n" +
                "- 책 제목: " + title + "\n" +
                "- 저자: " + author + "\n\n" +
                "OX 퀴즈 형식으로 문장을 제시하고 정답은 true 또는 false로 알려줘.\n" +
                "형식: {\"question\": \"...\", \"answer\": true}\n" +
                "고유값: " + java.util.UUID.randomUUID();

        new QuizService().sendQuestion(prompt, new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    try {
                        String content = new JSONObject(json)
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        JSONObject quizData = new JSONObject(content);
                        String question = quizData.getString("question");
                        boolean answer = quizData.getBoolean("answer");

                        runOnUiThread(() -> {
                            quizQuestion.setText(question);

                            View.OnClickListener listener = new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    boolean userAnswer = (v.getId() == R.id.quiz_o);
                                    if (userAnswer == answer) {
                                        showToast("정답입니다!");
                                        loadNextQuiz();
                                    } else {
                                        showToast("오답입니다!");
                                    }
                                }
                            };
                            buttonO.setOnClickListener(listener);
                            buttonX.setOnClickListener(listener);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("QUIZ_API", "응답 실패: " + response.code());
                }
            }

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e("QUIZ_API", "오류 발생", e);
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
