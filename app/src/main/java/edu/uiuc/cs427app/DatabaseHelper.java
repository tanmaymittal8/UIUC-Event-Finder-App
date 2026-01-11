package edu.uiuc.cs427app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DatabaseHelper manages SQLite database operations for user data.
 * This is an internal helper class used by AuthenticationManager.
 */
class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CS427AppDB";
    private static final int DATABASE_VERSION = 8;

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD_HASH = "password_hash";
    private static final String COLUMN_THEME = "theme";
    private static final String COLUMN_CITIES = "cities";

    // Cities table
    private static final String TABLE_CITIES = "cities";
    private static final String COLUMN_CITY_ID = "city_id";
    private static final String COLUMN_CITY_NAME = "name";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_COUNTRY = "country";
    private static final String COLUMN_STATE = "state";

    private static final String COLUMN_THEME_JSON = "theme_json";


    /**
     * Constructor for DatabaseHelper.
     *
     * @param context Application context
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time.
     * Creates the users table.
     *
     * @param db The database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
                COLUMN_PASSWORD_HASH + " TEXT NOT NULL, " +
                COLUMN_THEME + " TEXT NOT NULL DEFAULT 'LIGHT', " +
                COLUMN_THEME_JSON + " TEXT NOT NULL DEFAULT '', " +
                COLUMN_CITIES + " TEXT" +
                ");";
        db.execSQL(createUsersTable);

        String createCitiesTable = "CREATE TABLE " + TABLE_CITIES + " (" +
                COLUMN_CITY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CITY_NAME + " TEXT NOT NULL, " +
                COLUMN_LATITUDE + " REAL NOT NULL, " +
                COLUMN_LONGITUDE + " REAL NOT NULL, " +
                COLUMN_COUNTRY + " TEXT NOT NULL, " +
                COLUMN_STATE + " TEXT, " +
                "UNIQUE(" + COLUMN_CITY_NAME + ", " + COLUMN_COUNTRY + ", " + COLUMN_STATE + ")" +
                ");";
        db.execSQL(createCitiesTable);
    }

    /**
     * Called when the database needs to be upgraded.
     *
     * @param db         The database
     * @param oldVersion The old database version
     * @param newVersion The new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CITIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    /**
     * Inserts a new user into the database.
     *
     * @param username     The username
     * @param passwordHash The hashed password
     * @return The user ID of the newly inserted user, or -1 if insertion failed
     */
    public long insertUser(String username, String passwordHash, Theme theme) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD_HASH, passwordHash);
        values.put(COLUMN_THEME, theme.name());
        long userId = db.insert(TABLE_USERS, null, values);
        return userId;
    }

    /**
     * Retrieves a user by username.
     *
     * @param username The username to search for
     * @return User object if found, null otherwise
     */
    public User getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        String query = "SELECT * FROM " + TABLE_USERS +
                " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        if (cursor.moveToFirst()) {
            int userIdIndex = cursor.getColumnIndex(COLUMN_USER_ID);
            int usernameIndex = cursor.getColumnIndex(COLUMN_USERNAME);
            int passwordHashIndex = cursor.getColumnIndex(COLUMN_PASSWORD_HASH);
            int themeIdx = cursor.getColumnIndex(COLUMN_THEME);
            if (userIdIndex != -1 && usernameIndex != -1 && passwordHashIndex != -1) {
                String userId = String.valueOf(cursor.getInt(userIdIndex));
                String retrievedUsername = cursor.getString(usernameIndex);
                String passwordHash = cursor.getString(passwordHashIndex);

                // read theme (default LIGHT if column missing or null)
                String themeStr = (themeIdx != -1) ? cursor.getString(themeIdx) : "LIGHT";
                if (themeStr == null || themeStr.isEmpty()) themeStr = "LIGHT";
                Theme theme = Theme.valueOf(themeStr);
                user = new User(userId, retrievedUsername, passwordHash, theme);
            }
        }

        cursor.close();
//        db.close();
        return user;
    }

    /**
     * Checks if a username already exists in the database.
     *
     * @param username The username to check
     * @return true if username exists, false otherwise
     */
    public boolean userExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT 1 FROM " + TABLE_USERS +
                " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
//        db.close();
        return exists;
    }

    /**
     * Updates a user's password hash.
     *
     * @param username        The username
     * @param newPasswordHash The new password hash
     * @return true if update successful, false otherwise
     */
    public boolean updateUserPassword(String username, String newPasswordHash) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD_HASH, newPasswordHash);

        int rowsAffected = db.update(TABLE_USERS, values,
                COLUMN_USERNAME + " = ?",
                new String[]{username});
//        db.close();
        return rowsAffected > 0;
    }

    /**
     * Deletes a user from the database.
     *
     * @param username The username to delete
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_USERS,
                COLUMN_USERNAME + " = ?",
                new String[]{username});
//        db.close();
        return rowsDeleted > 0;
    }

    /**
     * Gets the total number of registered users.
     *
     * @return The count of users in the database
     */
    public int getUserCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_USERS;
        Cursor cursor = db.rawQuery(query, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
//        db.close();
        return count;
    }

    /**
     * Updates the theme JSON for a user.
     *
     * @param username  The username
     * @param themeJson The theme JSON string to save
     * @return true if update successful, false otherwise
     */
    public boolean updateUserThemeJson(String username, String themeJson) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_THEME_JSON, themeJson);
        int rows = db.update(TABLE_USERS, values, COLUMN_USERNAME + " = ?", new String[]{username});
        db.close();
        return rows > 0;
    }

    /**
     * Retrieves the theme JSON for a user.
     *
     * @param username The username
     * @return The theme JSON string or null if not found
     */
    public String getUserThemeJson(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_USERS,
                new String[]{COLUMN_THEME_JSON},
                COLUMN_USERNAME + " = ?",
                new String[]{username},
                null, null, null);
        String json = null;
        if (c.moveToFirst()) {
            json = c.getString(0);
        }
        c.close();
        db.close();
        return json;
    }

    // ==================== City Operations ====================

    /**
     * Inserts a new city into the database.
     * If a city with the same name, country, and state already exists, returns existing city ID.
     *
     * @param city The City object to insert
     * @return The city ID of the inserted or existing city, or -1 if insertion failed
     */
    public long insertCity(City city) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if city already exists
        City existingCity = getCityByLocation(city.getName(), city.getCountry(), city.getState());
        if (existingCity != null) {

            if (existingCity.getLatitude() != city.getLatitude() || existingCity.getLongitude() != city.getLongitude()) {

                updateCityCoordinates(existingCity.getCityId(), city.getLatitude(), city.getLongitude());
            }
            return existingCity.getCityId();
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_CITY_NAME, city.getName());
        values.put(COLUMN_LATITUDE, city.getLatitude());
        values.put(COLUMN_LONGITUDE, city.getLongitude());
        values.put(COLUMN_COUNTRY, city.getCountry());
        values.put(COLUMN_STATE, city.getState());

        long cityId = db.insert(TABLE_CITIES, null, values);
//        db.close();
        return cityId;
    }

    /**
     * Updates the coordinates (latitude and longitude) for an existing city.
     *
     * @param cityId    The city ID to update
     * @param latitude  The new latitude value
     * @param longitude The new longitude value
     * @return true if update successful, false otherwise
     */
    public boolean updateCityCoordinates(int cityId, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        int rowsAffected = db.update(TABLE_CITIES, values, COLUMN_CITY_ID + " = ?", new String[]{String.valueOf(cityId)});

        return rowsAffected > 0;

    }

    /**
     * Retrieves a city by its ID.
     *
     * @param cityId The city ID to search for
     * @return City object if found, null otherwise
     */
    public City getCityById(int cityId) {
        SQLiteDatabase db = this.getReadableDatabase();
        City city = null;

        String query = "SELECT * FROM " + TABLE_CITIES +
                " WHERE " + COLUMN_CITY_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(cityId)});

        if (cursor.moveToFirst()) {
            city = extractCityFromCursor(cursor);
        }

        cursor.close();
//        db.close();
        return city;
    }

    /**
     * Retrieves a city by its name, country, and state.
     *
     * @param name    The city name
     * @param country The country
     * @param state   The state (can be null)
     * @return City object if found, null otherwise
     */
    public City getCityByLocation(String name, String country, String state) {
        SQLiteDatabase db = this.getReadableDatabase();
        City city = null;
        String query;
        String[] args;

        if (state == null || state.isEmpty()) {
            query = "SELECT * FROM " + TABLE_CITIES +
                    " WHERE " + COLUMN_CITY_NAME + " = ? AND " +
                    COLUMN_COUNTRY + " = ? AND " +
                    COLUMN_STATE + " IS NULL";
            args = new String[]{name, country};
        } else {
            query = "SELECT * FROM " + TABLE_CITIES +
                    " WHERE " + COLUMN_CITY_NAME + " = ? AND " +
                    COLUMN_COUNTRY + " = ? AND " +
                    COLUMN_STATE + " = ? ";
            args = new String[]{name, country, state};
        }

        Cursor cursor = db.rawQuery(query, args);

        if (cursor.moveToFirst()) {
            city = extractCityFromCursor(cursor);
        }

        cursor.close();
        return city;
    }


    /**
     * Retrieves all cities from the database.
     *
     * @return List of all City objects
     */
    public java.util.List<City> getAllCities() {
        SQLiteDatabase db = this.getReadableDatabase();
        java.util.List<City> cities = new java.util.ArrayList<>();

        String query = "SELECT * FROM " + TABLE_CITIES;
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            City city = extractCityFromCursor(cursor);
            if (city != null) {
                cities.add(city);
            }
        }

        cursor.close();
//        db.close();
        return cities;
    }

    /**
     * Deletes a city from the database.
     *
     * @param cityId The city ID to delete
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteCity(int cityId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_CITIES,
                COLUMN_CITY_ID + " = ?",
                new String[]{String.valueOf(cityId)});
//        db.close();
        return rowsDeleted > 0;
    }

    /**
     * Helper method to extract City object from cursor.
     *
     * @param cursor Database cursor
     * @return City object or null
     */
    private City extractCityFromCursor(Cursor cursor) {
        int cityIdIndex = cursor.getColumnIndex(COLUMN_CITY_ID);
        int nameIndex = cursor.getColumnIndex(COLUMN_CITY_NAME);
        int latIndex = cursor.getColumnIndex(COLUMN_LATITUDE);
        int lonIndex = cursor.getColumnIndex(COLUMN_LONGITUDE);
        int countryIndex = cursor.getColumnIndex(COLUMN_COUNTRY);
        int stateIndex = cursor.getColumnIndex(COLUMN_STATE);

        if (cityIdIndex != -1 && nameIndex != -1 && latIndex != -1 &&
                lonIndex != -1 && countryIndex != -1) {
            int cityId = cursor.getInt(cityIdIndex);
            String name = cursor.getString(nameIndex);
            double latitude = cursor.getDouble(latIndex);
            double longitude = cursor.getDouble(lonIndex);
            String country = cursor.getString(countryIndex);
            String state = stateIndex != -1 ? cursor.getString(stateIndex) : null;

            return new City(cityId, name, latitude, longitude, country, state);
        }
        return null;
    }

    // ==================== User-Cities Association Operations ====================

    /**
     * Associates a city with a user by adding the city ID to the user's cities column.
     *
     * @param username The username
     * @param cityId   The city ID to associate
     * @return true if association successful, false otherwise
     */
    public boolean addCityToUser(String username, int cityId) {
        User user = getUserByUsername(username);
        if (user == null) {
            return false;
        }

        java.util.List<Integer> cityIds = getUserCityIds(username);

        // Check if city is already associated
        if (cityIds.contains(cityId)) {
            return true; // Already associated
        }

        cityIds.add(cityId);
        return updateUserCities(username, cityIds);
    }

    /**
     * Removes a city association from a user.
     *
     * @param username The username
     * @param cityId   The city ID to remove
     * @return true if removal successful, false otherwise
     */
    public boolean removeCityFromUser(String username, int cityId) {
        java.util.List<Integer> cityIds = getUserCityIds(username);

        if (!cityIds.contains(cityId)) {
            return true; // Already not associated
        }

        cityIds.remove(Integer.valueOf(cityId));
        return updateUserCities(username, cityIds);
    }

    /**
     * Gets all city IDs associated with a user.
     *
     * @param username The username
     * @return List of city IDs (empty list if none)
     */
    public java.util.List<Integer> getUserCityIds(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        java.util.List<Integer> cityIds = new java.util.ArrayList<>();

        String query = "SELECT " + COLUMN_CITIES + " FROM " + TABLE_USERS +
                " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        if (cursor.moveToFirst()) {
            int citiesIndex = cursor.getColumnIndex(COLUMN_CITIES);
            if (citiesIndex != -1) {
                String citiesStr = cursor.getString(citiesIndex);
                cityIds = parseCityIds(citiesStr);
            }
        }

        cursor.close();
//        db.close();
        return cityIds;
    }

    /**
     * Gets all City objects associated with a user.
     *
     * @param username The username
     * @return List of City objects (empty list if none)
     */
    public java.util.List<City> getUserCities(String username) {
        java.util.List<Integer> cityIds = getUserCityIds(username);
        java.util.List<City> cities = new java.util.ArrayList<>();

        for (Integer cityId : cityIds) {
            City city = getCityById(cityId);
            if (city != null) {
                cities.add(city);
            }
        }

        return cities;
    }

    /**
     * Updates the cities column for a user.
     *
     * @param username The username
     * @param cityIds  List of city IDs
     * @return true if update successful, false otherwise
     */
    private boolean updateUserCities(String username, java.util.List<Integer> cityIds) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CITIES, serializeCityIds(cityIds));

        int rowsAffected = db.update(TABLE_USERS, values,
                COLUMN_USERNAME + " = ?",
                new String[]{username});
//        db.close();
        return rowsAffected > 0;
    }

    /**
     * Parses comma-separated city IDs string to List of Integers.
     *
     * @param citiesStr Comma-separated city IDs (e.g., "1,3,5")
     * @return List of city IDs
     */
    private java.util.List<Integer> parseCityIds(String citiesStr) {
        java.util.List<Integer> cityIds = new java.util.ArrayList<>();

        if (citiesStr == null || citiesStr.trim().isEmpty()) {
            return cityIds;
        }

        String[] ids = citiesStr.split(",");
        for (String id : ids) {
            try {
                cityIds.add(Integer.parseInt(id.trim()));
            } catch (NumberFormatException e) {
                // Skip invalid IDs
            }
        }

        return cityIds;
    }

    /**
     * Serializes List of city IDs to comma-separated string.
     *
     * @param cityIds List of city IDs
     * @return Comma-separated string (e.g., "1,3,5")
     */
    private String serializeCityIds(java.util.List<Integer> cityIds) {
        if (cityIds == null || cityIds.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cityIds.size(); i++) {
            sb.append(cityIds.get(i));
            if (i < cityIds.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
}
