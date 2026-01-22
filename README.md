# QuadCompass: Geo-Social Campus Event Platform

**QuadCompass** is a native Android application designed to foster campus community engagement by enabling users to discover, create, and track local events on an interactive map. It features a custom-built mock backend for multi-user simulation, robust authentication, and real-time geospatial visualization.

## Features

### Interactive Event Mapping
* **Global Event Visualization:** Aggregates events from all users onto a shared Google Map.
* **Smart Marker Management:** Implements a custom **spatial jittering algorithm** to resolve coordinate collisions, ensuring overlapping events at the same location (e.g., same building) remain individually clickable.
* **Seamless Navigation:** Integrated marker tagging system to route users from map pins directly to detailed event description fragments.
* **Custom UI:** Utilizes a custom `InfoWindowAdapter` to render specialized event summaries on map clicks.

### User Management & Security
* **Secure Authentication:** Full Login/Registration system using **SQLite** with **SHA-256 password hashing**.
* **Profile Customization:** Users can manage their personal profiles (Name/Bio) which are persisted locally.
* **Session Management:** Persistent login state handling using `SharedPreferences`.

### Event & Data Persistence
* **Mock Backend Architecture:** Engineered a hybrid persistence layer using **Gson (JSON serialization)** to simulate a cloud environment.
  * **Global State:** Events are stored in a shared internal file, allowing independent user accounts to interact within a unified ecosystem on a single device.
  * **User Isolation:** "My Events" tab filters the global dataset to show only the currently logged-in user's contributions.

---

## Tech Stack

* **Language:** Java (Native Android)
* **Architecture:** MVVM-adjacent (Repository Pattern), Single-Activity Architecture (Fragments)
* **APIs & SDKs:** * Google Maps SDK (Maps & Markers)
  * Android Geocoding API (Address to Coordinate conversion)
  * OpenWeatherMap API (RESTful Data Fetching)
* **Libraries:**
  * **Gson:** JSON Parsing & Serialization
  * **Retrofit:** Type-safe HTTP client
  * **AndroidX:** Jetpack components (ConstraintLayout, RecyclerView, CardView)

---

## 🚀 Installation & Setup

To run this project locally, you will need **Android Studio**.

1. **Clone the repository**
   ```bash
   git clone [https://github.com/yourusername/QuadCompass.git](https://github.com/yourusername/QuadCompass.git)
