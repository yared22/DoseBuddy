package com.example.dosebuddy.api;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Service class for making API calls to OpenFDA Drug API
 */
public class DrugInfoService {
    
    private static final String TAG = "DrugInfoService";
    private static final String BASE_URL = "https://api.fda.gov/drug/label.json";
    private static final int TIMEOUT_SECONDS = 30;
    
    private final OkHttpClient httpClient;
    private final Gson gson;
    
    public DrugInfoService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }
    
    /**
     * Interface for API response callbacks
     */
    public interface DrugInfoCallback {
        void onSuccess(List<DrugInfo> drugInfoList);
        void onError(String errorMessage);
    }
    
    /**
     * Search for drug information by name
     * @param drugName Name of the drug to search for
     * @param callback Callback to handle the response
     */
    public void searchDrugInfo(String drugName, DrugInfoCallback callback) {
        if (drugName == null || drugName.trim().isEmpty()) {
            callback.onError("Drug name cannot be empty");
            return;
        }

        // Try primary search first
        searchDrugInfoInternal(drugName, callback, true);
    }

    /**
     * Internal method to search for drug information with fallback options
     */
    private void searchDrugInfoInternal(String drugName, DrugInfoCallback callback, boolean tryFallback) {
        try {
            // Clean and encode the drug name for URL
            String cleanDrugName = drugName.trim().toLowerCase();
            String encodedDrugName = URLEncoder.encode(cleanDrugName, "UTF-8");

            // Build the API URL with more flexible search
            // Search in brand names, generic names, and active ingredients
            String url = BASE_URL + "?search=(openfda.brand_name:" + encodedDrugName + "+OR+openfda.generic_name:" + encodedDrugName + "+OR+active_ingredient:" + encodedDrugName + ")&limit=5";

            Log.d(TAG, "Making API request to: " + url);

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "DoseBuddy-Android-App")
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "API request failed", e);

                    if (tryFallback) {
                        // Try fallback search with common drug name variations
                        tryFallbackSearch(drugName, callback);
                    } else {
                        callback.onError("Network error: Unable to connect to drug database. Please check your internet connection.");
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (!response.isSuccessful()) {
                            Log.e(TAG, "API request unsuccessful: " + response.code());

                            if (tryFallback && response.code() == 404) {
                                // Try fallback search
                                tryFallbackSearch(drugName, callback);
                                return;
                            }

                            if (response.code() == 404) {
                                callback.onError("No information found for '" + drugName + "' in the FDA database.");
                            } else {
                                callback.onError("Server error: " + response.code());
                            }
                            return;
                        }

                        String responseBody = response.body().string();
                        Log.d(TAG, "API response received, length: " + responseBody.length());

                        // Parse the JSON response
                        DrugSearchResponse searchResponse = gson.fromJson(responseBody, DrugSearchResponse.class);

                        if (searchResponse == null || searchResponse.getResults() == null || searchResponse.getResults().isEmpty()) {
                            if (tryFallback) {
                                // Try fallback search
                                tryFallbackSearch(drugName, callback);
                            } else {
                                // Return empty list to trigger sample data display
                                callback.onSuccess(new ArrayList<>());
                            }
                            return;
                        }

                        // Convert API results to DrugInfo objects
                        List<DrugInfo> drugInfoList = new ArrayList<>();
                        for (DrugSearchResponse.DrugResult result : searchResponse.getResults()) {
                            DrugInfo drugInfo = result.toDrugInfo();
                            if (drugInfo.hasEssentialInfo()) {
                                drugInfoList.add(drugInfo);
                            }
                        }

                        if (drugInfoList.isEmpty()) {
                            if (tryFallback) {
                                // Try fallback search
                                tryFallbackSearch(drugName, callback);
                            } else {
                                // Return empty list to trigger sample data display
                                callback.onSuccess(new ArrayList<>());
                            }
                        } else {
                            Log.d(TAG, "Successfully parsed " + drugInfoList.size() + " drug info results");
                            callback.onSuccess(drugInfoList);
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing API response", e);
                        callback.onError("Error processing drug information: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error building API request", e);
            callback.onError("Error preparing request: " + e.getMessage());
        }
    }

    /**
     * Try fallback search with common drug name variations
     */
    private void tryFallbackSearch(String originalDrugName, DrugInfoCallback callback) {
        String fallbackName = getFallbackDrugName(originalDrugName);

        if (fallbackName != null && !fallbackName.equals(originalDrugName.toLowerCase().trim())) {
            Log.d(TAG, "Trying fallback search with: " + fallbackName);
            searchDrugInfoInternal(fallbackName, callback, false);
        } else {
            // Return empty list to trigger sample data display
            callback.onSuccess(new ArrayList<>());
        }
    }

    /**
     * Get fallback drug name for common variations
     */
    private String getFallbackDrugName(String drugName) {
        String lowerName = drugName.toLowerCase().trim();

        // Common drug name mappings
        switch (lowerName) {
            case "paracetamol":
                return "acetaminophen";
            case "acetaminophen":
                return "paracetamol";
            case "ibuprofen":
                return "advil";
            case "advil":
                return "ibuprofen";
            case "aspirin":
                return "acetylsalicylic acid";
            case "acetylsalicylic acid":
                return "aspirin";
            case "tylenol":
                return "acetaminophen";
            default:
                return null;
        }
    }
    
    /**
     * Search for drug suggestions (for autocomplete)
     * @param query Partial drug name
     * @param callback Callback to handle the response
     */
    public void searchDrugSuggestions(String query, DrugInfoCallback callback) {
        if (query == null || query.trim().length() < 2) {
            callback.onSuccess(new ArrayList<>()); // Return empty list for short queries
            return;
        }
        
        try {
            String cleanQuery = query.trim().toLowerCase();
            String encodedQuery = URLEncoder.encode(cleanQuery, "UTF-8");
            
            // Search for drugs that start with the query
            String url = BASE_URL + "?search=openfda.brand_name:\"" + encodedQuery + "*\"+openfda.generic_name:\"" + encodedQuery + "*\"&limit=10";
            
            Log.d(TAG, "Making suggestions API request to: " + url);
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "DoseBuddy-Android-App")
                    .build();
            
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Suggestions API request failed", e);
                    callback.onSuccess(new ArrayList<>()); // Return empty list on error
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (!response.isSuccessful()) {
                            callback.onSuccess(new ArrayList<>()); // Return empty list on error
                            return;
                        }
                        
                        String responseBody = response.body().string();
                        DrugSearchResponse searchResponse = gson.fromJson(responseBody, DrugSearchResponse.class);
                        
                        List<DrugInfo> suggestions = new ArrayList<>();
                        if (searchResponse != null && searchResponse.getResults() != null) {
                            for (DrugSearchResponse.DrugResult result : searchResponse.getResults()) {
                                DrugInfo drugInfo = result.toDrugInfo();
                                if (drugInfo.hasEssentialInfo()) {
                                    suggestions.add(drugInfo);
                                }
                            }
                        }
                        
                        callback.onSuccess(suggestions);
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing suggestions response", e);
                        callback.onSuccess(new ArrayList<>()); // Return empty list on error
                    }
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error building suggestions request", e);
            callback.onSuccess(new ArrayList<>()); // Return empty list on error
        }
    }
    
    /**
     * Clean up resources
     */
    public void shutdown() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
        }
    }
}
