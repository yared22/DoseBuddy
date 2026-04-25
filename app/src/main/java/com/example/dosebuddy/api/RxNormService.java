package com.example.dosebuddy.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Service for interacting with the RxNorm API (clinicaltables.nlm.nih.gov)
 */
public class RxNormService {
    private static final String TAG = "RxNormService";
    private static final String BASE_URL = "https://clinicaltables.nlm.nih.gov/api/rxterms/v3/search?terms=";
    
    private final OkHttpClient client;
    private final Gson gson;
    private final Handler mainHandler;

    public interface RxNormCallback {
        void onSuggestionsReceived(List<DrugSuggestion> suggestions);
        void onError(String message);
    }

    public static class DrugSuggestion {
        public String name;
        public List<String> strengths;

        public DrugSuggestion(String name, List<String> strengths) {
            this.name = name;
            this.strengths = strengths;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public RxNormService() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void searchDrugs(String query, RxNormCallback callback) {
        try {
            String url = BASE_URL + URLEncoder.encode(query, "UTF-8");
            Request request = new Request.Builder().url(url).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mainHandler.post(() -> callback.onError(e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        mainHandler.post(() -> callback.onError("Unexpected code " + response));
                        return;
                    }

                    String responseData = response.body().string();
                    try {
                        JsonArray root = gson.fromJson(responseData, JsonArray.class);
                        // RxNorm response format: [count, displayNames, null, strengths]
                        JsonArray namesArray = root.get(1).getAsJsonArray();
                        JsonArray strengthsArray = root.get(3).getAsJsonArray();

                        List<DrugSuggestion> suggestions = new ArrayList<>();
                        for (int i = 0; i < namesArray.size(); i++) {
                            String name = namesArray.get(i).getAsString();
                            List<String> strengths = new ArrayList<>();
                            if (strengthsArray.size() > i) {
                                JsonArray sArray = strengthsArray.get(i).getAsJsonArray();
                                for (JsonElement s : sArray) {
                                    strengths.add(s.getAsString());
                                }
                            }
                            suggestions.add(new DrugSuggestion(name, strengths));
                        }
                        mainHandler.post(() -> callback.onSuggestionsReceived(suggestions));
                    } catch (Exception e) {
                        mainHandler.post(() -> callback.onError("Parsing error: " + e.getMessage()));
                    }
                }
            });
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
}
