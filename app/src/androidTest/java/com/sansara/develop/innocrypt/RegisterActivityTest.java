package com.sansara.develop.innocrypt;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.sansara.develop.innocrypt.ui.LoginActivity;
import com.sansara.develop.innocrypt.ui.RegisterActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class RegisterActivityTest {

    @Rule
    public ActivityTestRule<RegisterActivity> activityRegisterRule = new ActivityTestRule<>(RegisterActivity.class);


    @Before
    public void setUp() {
        //activityRegisterRule.getActivity();     //TODO: try to avoid
    }

    @Test
    public void testValidEmailAndPasswordsRegisterNewUser() {
        onView(withId(R.id.edit_username)).perform(typeText("test2@gmail.com"));
        onView(withId(R.id.edit_password)).perform(typeText("123qweQWE"));
        onView(withId(R.id.edit_repeat_password)).perform(typeText("123qweQWE"), closeSoftKeyboard());

        onView(withId(R.id.button_go)).perform(click());
        assertTrue(activityRegisterRule.getActivity().isDestroyed());
    }
}
