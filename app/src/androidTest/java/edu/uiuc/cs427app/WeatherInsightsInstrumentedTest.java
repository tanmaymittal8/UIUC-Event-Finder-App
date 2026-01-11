package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingPolicies;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * Instrumented Espresso test for the Weather Insights feature.
 * Tests verify that clicking on LLM-generated question buttons triggers
 * the behavior of showing corresponding responses in a dialog.
 * <p>
 * Note: Per project requirements, we test the app's behavior (clicking questions
 * and showing answers) without making assertions on the dynamically generated
 * LLM content itself.
 */
@RunWith(AndroidJUnit4.class)
public class WeatherInsightsInstrumentedTest {

    // Sample weather data for testing
    private static final String TEST_WEATHER_DATA = "Temperature: 72°F, Condition: Partly Cloudy, Humidity: 65%, Wind: 10 mph";
    private static final String TEST_CITY_NAME = "Chicago";

    @Before
    public void setUp() {
        // Increase Espresso timeout for slower LLM operations
        IdlingPolicies.setMasterPolicyTimeout(120, TimeUnit.SECONDS);
        IdlingPolicies.setIdlingResourceTimeout(120, TimeUnit.SECONDS);
    }

    /**
     * Test that verifies the Weather Insights activity displays correctly
     * and questions can be clicked to show answers.
     * Steps:
     * 1. Launch WeatherInsightsActivity with test weather data
     * 2. Wait for questions to be generated
     * 3. Verify the title and questions layout are displayed
     * 4. Click on a question button
     * 5. Verify that a dialog appears showing the answer
     */
    @Test
    public void testWeatherInsightsDisplaysAndAnswersQuestions() throws InterruptedException {
        // Launch WeatherInsightsActivity with test data
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), WeatherInsightsActivity.class);
        intent.putExtra("weatherData", TEST_WEATHER_DATA);
        intent.putExtra("cityName", TEST_CITY_NAME);

        ActivityScenario<WeatherInsightsActivity> scenario = ActivityScenario.launch(intent);

        // Allow time for activity to initialize
        Thread.sleep(2000);

        // Verify title is displayed
        onView(withId(R.id.titleTextView))
                .check(matches(isDisplayed()));
        Thread.sleep(500);
        onView(withId(R.id.titleTextView))
                .check(matches(withText(containsString("Weather Insights"))));
        Thread.sleep(500);

        // Wait for LLM to generate questions
        Thread.sleep(15000);

        // Verify questions layout is visible
        onView(withId(R.id.questionsLayout))
                .check(matches(isDisplayed()));

        Thread.sleep(1500);

        // Click on the first question button
        scenario.onActivity(activity -> {
            LinearLayout questionsLayout = activity.findViewById(R.id.questionsLayout);
            for (int i = 0; i < questionsLayout.getChildCount(); i++) {
                View child = questionsLayout.getChildAt(i);
                if (child instanceof Button) {
                    child.performClick();
                    break;
                }
            }
        });

        Thread.sleep(2000);

        // Wait for answer dialog to appear
        Thread.sleep(15000);

        // Verify dialog with OK button is displayed
        onView(withText("OK"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        Thread.sleep(1000);

        // Dismiss the dialog
        onView(withText("OK"))
                .inRoot(isDialog())
                .perform(click());

        Thread.sleep(1000);

        // Cleanup
        scenario.close();
    }


    /**
     * Test that verifies the back button closes the activity.
     * Steps:
     * 1. Launch WeatherInsightsActivity with test weather data
     * 2. Wait for questions to be generated
     * 3. Click the back button
     * 4. Verify the activity is finished by checking the scenario state
     */
    @Test
    public void testBackButtonClosesActivity() throws InterruptedException {
        // Launch WeatherInsightsActivity with test data
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), WeatherInsightsActivity.class);
        intent.putExtra("weatherData", TEST_WEATHER_DATA);
        intent.putExtra("cityName", TEST_CITY_NAME);

        ActivityScenario<WeatherInsightsActivity> scenario = ActivityScenario.launch(intent);

        // Allow time for activity to initialize
        Thread.sleep(2000);

        // Wait for LLM to generate questions
        Thread.sleep(15000);

        // Verify back button is displayed
        onView(withId(R.id.backButton))
                .check(matches(isDisplayed()));

        Thread.sleep(1500);

        // Click the back button
        onView(withId(R.id.backButton))
                .perform(click());

        Thread.sleep(1500);

        // The activity should be finished after clicking back
        // We verify by checking that the scenario state is DESTROYED
        // This is the proper way to assert activity destruction
        assert scenario.getState() == androidx.lifecycle.Lifecycle.State.DESTROYED :
                "Activity should be destroyed after clicking back button";

        // No need to close scenario as activity is already destroyed
    }

}
