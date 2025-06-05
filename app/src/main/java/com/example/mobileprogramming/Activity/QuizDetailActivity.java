package com.example.mobileprogramming.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobileprogramming.Api.QuizApi;
import com.example.mobileprogramming.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

// Removed incorrect Callback import

import okhttp3.Response;

public class QuizDetailActivity extends AppCompatActivity {

    private TextView quizQuestion;
    private Button buttonO;
    private Button buttonX;

    private TextView mcqQuestion;
    private Button mcqButton1;
    private Button mcqButton2;
    private Button mcqButton3;
    private Button mcqButton4;

    private String prompt; // Make prompt a class field
    private boolean isOXQuiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String title = getIntent().getStringExtra("bookTitle");
        String author = getIntent().getStringExtra("bookAuthor");

        Log.d("QUIZ_DETAIL_INTENT", "제목: " + title);
        Log.d("QUIZ_DETAIL_INTENT", "저자: " + author);

        loadNextQuiz();
    }

    private void loadNextQuiz() {
        isOXQuiz = new Random().nextBoolean(); // moved here
        String title = getIntent().getStringExtra("bookTitle");
        String author = getIntent().getStringExtra("bookAuthor");

        if (isOXQuiz) {
            prompt = "책의 내용을 바탕으로 창의적이고 다양한 OX 퀴즈를 만들어줘. 단순한 저자나 제목 정보가 아니라, 책의 줄거리나 주요 장면, 인물, 주제 등에 기반한 퀴즈를 생성하고, 매번 새로운 문제를 제시해줘. 이전과 중복되지 않도록 해줘.\n" +
                    "- 책 제목: " + title + "\n" +
                    "- 저자: " + author + "\n\n" +
                    "OX 퀴즈 형식으로 문장을 제시하고 정답은 true 또는 false로 알려줘.\n" +
                    "형식: {\"type\": \"ox\", \"question\": \"...\", \"answer\": true}\n" +
                    "고유값: " + java.util.UUID.randomUUID();
        } else {
            prompt = "책의 내용을 바탕으로 창의적이고 다양한 객관식(4지선다) 퀴즈를 만들어줘. 단순한 저자나 제목 정보가 아니라, 책의 줄거리나 주요 장면, 인물, 주제 등에 기반한 퀴즈를 생성하고, 매번 새로운 문제를 제시해줘. 이전과 중복되지 않도록 해줘.\n" +
                    "- 책 제목: " + title + "\n" +
                    "- 저자: " + author + "\n\n" +
                    "객관식 퀴즈 형식으로 문제와 4개의 선택지를 제시하고 정답은 0부터 시작하는 정답 인덱스로 알려줘.\n" +
                    "형식: {\"type\": \"mcq\", \"question\": \"...\", \"choices\": [\"...\", \"...\", \"...\", \"...\"], \"answer\": 1}\n" +
                    "고유값: " + java.util.UUID.randomUUID();
        }

        new QuizApi().sendQuestion(prompt, new okhttp3.Callback() {
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
                        String type = quizData.getString("type");
                        if ("ox".equals(type)) {
                            String question = quizData.getString("question");
                            boolean answer = quizData.getBoolean("answer");

                            runOnUiThread(() -> {
                                // Always ensure correct layout before accessing views
                                setContentView(R.layout.quiz_1);
                                TextView questionView = findViewById(R.id.quiz_question);
                                Button btnO = findViewById(R.id.quiz_o);
                                Button btnX = findViewById(R.id.quiz_x);
                                // Only access views if present
                                if (questionView != null) {
                                    questionView.setText(question);
                                }
                                if (btnO != null && btnX != null) {
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
                                    btnO.setOnClickListener(listener);
                                    btnX.setOnClickListener(listener);
                                }
                            });
                        } else if ("mcq".equals(type)) {
                            String question = quizData.getString("question");
                            int answer = quizData.getInt("answer");
                            String[] choices = new String[4];
                            for (int i = 0; i < 4; i++) {
                                choices[i] = quizData.getJSONArray("choices").getString(i);
                            }

                            runOnUiThread(() -> {
                                // Always ensure correct layout before accessing views
                                setContentView(R.layout.quiz_2);
                                TextView questionView = findViewById(R.id.quiz_box2);
                                Button btn1 = findViewById(R.id.quiz1);
                                Button btn2 = findViewById(R.id.quiz2);
                                Button btn3 = findViewById(R.id.quiz3);
                                Button btn4 = findViewById(R.id.quiz4);
                                // Only access views if present
                                if (questionView != null && btn1 != null && btn2 != null && btn3 != null && btn4 != null) {
                                    questionView.setText(question);
                                    btn1.setText(choices[0]);
                                    btn2.setText(choices[1]);
                                    btn3.setText(choices[2]);
                                    btn4.setText(choices[3]);
                                    View.OnClickListener listener = new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            int userAnswer = -1;
                                            if (v.getId() == R.id.quiz1) userAnswer = 0;
                                            else if (v.getId() == R.id.quiz2) userAnswer = 1;
                                            else if (v.getId() == R.id.quiz3) userAnswer = 2;
                                            else if (v.getId() == R.id.quiz4) userAnswer = 3;
                                            if (userAnswer == answer) {
                                                showToast("정답입니다!");
                                                loadNextQuiz();
                                            } else {
                                                showToast("오답입니다!");
                                            }
                                        }
                                    };
                                    btn1.setOnClickListener(listener);
                                    btn2.setOnClickListener(listener);
                                    btn3.setOnClickListener(listener);
                                    btn4.setOnClickListener(listener);
                                }
                            });
                        }
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
