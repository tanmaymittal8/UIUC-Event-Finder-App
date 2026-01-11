package edu.uiuc.cs427app;

/**
 * Model class for weather information from OpenWeather API.
 * Contains timezone data and current weather conditions.
 */
public class WeatherInfo {
    public String timezone;
    public int timezone_offset;

    public WeatherCurrent current;

    /**
     * Inner class representing current weather conditions.
     * Contains temperature, humidity, wind data, and weather description.
     */
    public static class WeatherCurrent {
        public double temp;
        public int humidity;

        public double wind_speed;
        public double wind_deg;
        public double wind_gust;

        public WeatherSpecifics[] weather;
    }
}
