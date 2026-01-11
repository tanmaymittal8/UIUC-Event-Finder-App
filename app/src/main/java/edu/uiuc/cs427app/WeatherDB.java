package edu.uiuc.cs427app;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit interface for OpenWeather One Call API.
 * Provides methods to fetch comprehensive weather data for a location.
 */
public interface WeatherDB {
    /**
     * Fetches weather information for given coordinates.
     *
     * @param lat     Latitude coordinate
     * @param lon     Longitude coordinate
     * @param apiKey  OpenWeather API key for authentication
     * @param exclude Data to exclude from response (e.g., "hourly", "daily")
     * @param units   Unit system for temperature (e.g., "imperial", "metric")
     * @return Call object containing WeatherInfo with current conditions
     */
    @GET("data/3.0/onecall")
    Call<WeatherInfo> getWeather(@Query("lat") double lat, @Query("lon") double lon, @Query("appid") String apiKey, @Query("exclude") String exclude, @Query("units") String units);
}
