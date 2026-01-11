package edu.uiuc.cs427app;

// referred to https://www.digitalocean.com/community/tutorials/retrofit-android-example-tutorial
// referred to https://medium.com/@saharbat00l/how-to-build-a-simple-android-weather-app-using-kotlin-and-openweathermap-api-2e046d1a8514
// referred to https://square.github.io/retrofit/

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit interface for OpenWeather Geocoding API.
 * Provides methods to fetch location data by city name.
 */
public interface LocationDB {
    /**
     * Fetches location options (coordinates) for a given city name.
     *
     * @param cityName Name of the city to search for
     * @param limit    Maximum number of results to return
     * @param apiKey   OpenWeather API key for authentication
     * @return Call object containing list of LocationInfo results
     */
    @GET("geo/1.0/direct")
    Call<ArrayList<LocationInfo>> getLocationOptions(@Query("q") String cityName, @Query("limit") int limit, @Query("appid") String apiKey);
}
