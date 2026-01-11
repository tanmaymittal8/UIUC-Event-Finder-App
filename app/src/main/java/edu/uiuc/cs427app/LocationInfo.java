package edu.uiuc.cs427app;

public class LocationInfo {
    private String name;
    private String state;
    private String country;
    private double lat;
    private double lon;

    /**
     * Gets the city name.
     *
     * @return The name of the city
     */
    public String getCityName() {
        return name;
    }

    /**
     * Gets the state name.
     *
     * @return The state/province name
     */
    public String getStateName() {
        return state;
    }

    /**
     * Gets the country name.
     *
     * @return The country name
     */
    public String getCountryName() {
        return country;
    }

    /**
     * Gets the latitude coordinate.
     *
     * @return The latitude value
     */
    public double getLat() {
        return lat;
    }

    /**
     * Gets the longitude coordinate.
     *
     * @return The longitude value
     */
    public double getLon() {
        return lon;
    }

    /**
     * Formats location as a string with city, state (if available), and country.
     *
     * @return Formatted location string
     */
    public String location() {
        if (state != null && !state.isEmpty()) {
            return name + ", " + state + ", " + country;
        } else {
            return name + ", " + country;
        }
    }
}
