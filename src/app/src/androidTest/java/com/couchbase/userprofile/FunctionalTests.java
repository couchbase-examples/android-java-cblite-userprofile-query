package com.couchbase.userprofile;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.couchbase.userprofile.login.LoginActivity;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

//used for App Center UI Tests
import com.microsoft.appcenter.espresso.Factory;
import com.microsoft.appcenter.espresso.ReportHelper;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;

import android.view.View;
import android.widget.SearchView;

@RunWith(AndroidJUnit4.class)
public class FunctionalTests {
    private static final String testUsername = "demo@example.com";
    private static final String testUsername1 = "demo1@example.com";
    private static final String testPassword = "password";
    private static final String testFullName = "Bob Smith";
    private static final String testAddress = "123 nowhere land";
    private static final String UNIVERSITY_COUNTRY = "india";
    private static final String UNIVERSITY_NAME = "dev";

    @Rule
    public ActivityScenarioRule<LoginActivity> activityScenarioRule = new ActivityScenarioRule<>(LoginActivity.class);
    //public ActivityScenarioRule<UserProfileActivity> userProfileActivityActivityScenarioRule = new ActivityScenarioRule<>(UserProfileActivity.class);

    @Rule
    public ReportHelper reportHelper = Factory.getReportHelper();

    @Test
    public void loginTest() throws InterruptedException {

        //do the login
        reportHelper.label("Logging into the App");
        login();

        //validate screen we are on
        reportHelper.label("Check email is correct from login");
        onView(withId(R.id.emailInput))
                .check(matches(withText(testUsername)));

        //select the proper university
        onView(withId(R.id.universityText)).perform(click());
        Thread.sleep(1000);

        reportHelper.label("selecting university");
        onView(withId(R.id.nameSearchView))
                .perform(click());
        onView(withId(R.id.nameSearchView))
                .perform(typeSearchViewText(UNIVERSITY_NAME));

        onView(withId(R.id.countrySearchView))
                .perform(click());
        onView(withId(R.id.countrySearchView))
                .perform(typeSearchViewText(UNIVERSITY_COUNTRY));

        //search for university
        reportHelper.label("search for university");
        onView(withId(R.id.lookupButton))
                .perform(click());
        Thread.sleep(2000);

        //select university
        onView(withId(R.id.universityList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(2000);

        //set the user profile and save it
        reportHelper.label("Start updating user profile");
        onView(withId(R.id.nameInput))
                .perform(replaceText(testFullName));
        onView(withId(R.id.addressInput))
                .perform(typeText(""));
        onView(withId(R.id.addressInput))
                .perform(replaceText(testAddress), closeSoftKeyboard());

        //click the save button
        reportHelper.label("Save User Profile information");
        onView(withId(R.id.saveButton)).perform(click());

        //logout
        reportHelper.label("Logout");
        Thread.sleep(5000);
        onView(withId(R.id.logoutButton)).perform(click());
        Thread.sleep(5000);

        //log back in
        reportHelper.label("Log back into the App");
        login();

        //test to make sure form was saved into the database and read back
        reportHelper.label("Start data validation");
        onView(withId(R.id.nameInput))
                .check(matches(withText(testFullName)));

        onView(withId(R.id.emailInput))
                .check(matches(withText(testUsername)));

        onView(withId(R.id.addressInput))
                .check(matches(withText(testAddress)));

        reportHelper.label("data validation completed");
    }

    private void login() {
        //type in text and then press the login button
        onView(withId(R.id.usernameInput))
                .perform(typeText(testUsername));
        onView(withId(R.id.passwordInput))
                .perform(typeText(testPassword), closeSoftKeyboard());
        //click the login button
        onView(withId(R.id.loginButton)).perform(click());
    }

    //used to automate the SearchView widget because standard typeText can't
    //be used to access the internal view of SearchView
    //https://developer.android.com/reference/androidx/test/espresso/ViewAction
    public static ViewAction typeSearchViewText(final String text) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                //Ensure that only apply if it is a SearchView and if it is visible.
                return allOf(isDisplayed(), isAssignableFrom(SearchView.class));
            }

            @Override
            public String getDescription() {
                return "Change view text";
            }

            @Override
            public void perform(UiController uiController, View view) {
                ((SearchView) view).setQuery(text, false);
            }
        };
    }
}