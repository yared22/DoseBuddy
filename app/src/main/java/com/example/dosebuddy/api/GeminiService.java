package com.example.dosebuddy.api;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Service for interacting with Google Gemini API
 */
public class GeminiService {
    private static final String API_KEY = "YOUR_GEMINI_API_KEY"; // Replace with actual key
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;
    
    private final OkHttpClient client;
    private final Gson gson;
    private final Handler mainHandler;

    public interface GeminiCallback {
        void onResponse(String description);
        void onError(String message);
    }

    public GeminiService() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void generateDescription(String drugName, GeminiCallback callback) {
        if (API_KEY.equals("YOUR_GEMINI_API_KEY")) {
            mainHandler.post(() -> callback.onResponse("Description for " + drugName + " (Mock: API key not set)."));
            return;
        }

        String promptText = "Provide a short, simple explanation for the medication '" + drugName + "'. Include what it is used for and how it helps. Keep it under 2 sentences.";

        JsonObject jsonBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", promptText);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        jsonBody.add("contents", contents);

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    mainHandler.post(() -> callback.onError("Gemini API error: " + response.code()));
                    return;
                }

                try {
                    String responseData = response.body().string();
                    JsonObject root = gson.fromJson(responseData, JsonObject.class);
                    String text = root.getAsJsonArray("candidates")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("content")
                            .getAsJsonArray("parts")
                            .get(0).getAsJsonObject()
                            .get("text").getAsString();
                    
                    mainHandler.post(() -> callback.onResponse(text.trim()));
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError("Parsing error: " + e.getMessage()));
                }
            }
        });
    }
}
