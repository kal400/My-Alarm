package com.example.ZenWake.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import androidx.annotation.NonNull;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class WeatherManager {

    private static final String TAG = "WeatherManager";
    private static final String API_URL = "https://api.open-meteo.com/v1/forecast";

    private Context context;
    private RequestQueue requestQueue;
    private WeatherCallback callback;

    public interface WeatherCallback {
        void onSuccess(String temperature, String condition);
        void onError(String error);
    }

    public WeatherManager(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public void getCurrentWeather(WeatherCallback callback) {
        this.callback = callback;

        // Get last known location
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                fetchWeather(location.getLatitude(), location.getLongitude());
            } else {
                // Default to NYC coordinates if no location
                fetchWeather(40.7128, -74.0060);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission not granted");
            fetchWeather(40.7128, -74.0060);
        }
    }

    private void fetchWeather(double lat, double lon) {
        String url = API_URL + "?latitude=" + lat + "&longitude=" + lon +
                "&current_weather=true&temperature_unit=fahrenheit";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject currentWeather = response.getJSONObject("current_weather");
                        double temperature = currentWeather.getDouble("temperature");
                        int weatherCode = currentWeather.getInt("weathercode");

                        String condition = getWeatherCondition(weatherCode);

                        if (callback != null) {
                            callback.onSuccess(String.valueOf((int) temperature), condition);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing weather: " + e.getMessage());
                        if (callback != null) {
                            callback.onError("Failed to parse weather data");
                        }
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching weather: " + error.getMessage());
                    if (callback != null) {
                        callback.onError("Failed to fetch weather");
                    }
                });

        requestQueue.add(request);
    }

    private String getWeatherCondition(int code) {
        // WMO Weather interpretation codes
        switch (code) {
            case 0:
                return "Clear Sky";
            case 1:
            case 2:
            case 3:
                return "Partly Cloudy";
            case 45:
            case 48:
                return "Foggy";
            case 51:
            case 53:
            case 55:
                return "Drizzle";
            case 61:
            case 63:
            case 65:
                return "Rainy";
            case 71:
            case 73:
            case 75:
                return "Snowy";
            case 80:
            case 81:
            case 82:
                return "Rain Showers";
            case 95:
            case 96:
            case 99:
                return "Thunderstorm";
            default:
                return "Cloudy";
        }
    }
}