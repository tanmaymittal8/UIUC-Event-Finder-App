package edu.uiuc.cs427app;

/**
 * City model class to represent a city in the application.
 * Stores city ID, name, coordinates (latitude/longitude), country, and state.
 */
public class City {
    private int cityId;
    private String name;
    private double latitude;
    private double longitude;
    private String country;
    private String state;

    /**
     * Constructor to create a new City object with city ID.
     *
     * @param cityId    The unique city ID from database
     * @param name      The name of the city
     * @param latitude  The latitude coordinate
     * @param longitude The longitude coordinate
     * @param country   The country where the city is located
     * @param state     The state/province where the city is located (optional)
     */
    public City(int cityId, String name, double latitude, double longitude, String country, String state) {
        this.cityId = cityId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
        this.state = state;
    }

    /**
     * Constructor to create a new City object without city ID.
     * Used for city creation before database insertion.
     *
     * @param name      The name of the city
     * @param latitude  The latitude coordinate
     * @param longitude The longitude coordinate
     * @param country   The country where the city is located
     * @param state     The state/province where the city is located (optional)
     */
    public City(String name, double latitude, double longitude, String country, String state) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
        this.state = state;
    }

    // Getters and Setters

    /**
     * Gets the city ID.
     *
     * @return The unique city identifier
     */
    public int getCityId() {
        return cityId;
    }

    /**
     * Sets the city ID.
     *
     * @param cityId The unique city identifier to set
     */
    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    /**
     * Gets the city name.
     *
     * @return The name of the city
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the city name.
     *
     * @param name The name of the city to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the latitude coordinate.
     *
     * @return The latitude value
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude coordinate.
     *
     * @param latitude The latitude value to set
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Gets the longitude coordinate.
     *
     * @return The longitude value
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude coordinate.
     *
     * @param longitude The longitude value to set
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Gets the country name.
     *
     * @return The country where the city is located
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the country name.
     *
     * @param country The country name to set
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Gets the state/province name.
     *
     * @return The state or province where the city is located
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the state/province name.
     *
     * @param state The state or province name to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Returns a string representation of the City object.
     *
     * @return String containing all city attributes
     */
    @Override
    public String toString() {
        return "City{" +
                "cityId=" + cityId +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", country='" + country + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
