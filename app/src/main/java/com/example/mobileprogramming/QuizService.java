package com.example.mobileprogramming;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QuizService {
    private static final String API_KEY = BuildConfig.OPENAI_API_KEY;

    public void sendQuestion(String question, Callback callback) {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.get("application/json");
        JSONObject body = new JSONObject();
        try {
            body.put("model", "gpt-4o-mini");
            JSONArray messages = new JSONArray();
            JSONObject msg = new JSONObject();
            msg.put("role", "user");
            msg.put("content", question);
            messages.put(msg);
            body.put("messages", messages);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        Request request = new Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .post(RequestBody.create(mediaType, body.toString()))
            .addHeader("Authorization", "Bearer " + API_KEY)
            .addHeader("Content-Type", "application/json")
            .build();

        client.newCall(request).enqueue(callback);
    }
}