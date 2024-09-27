package com.currency.converter

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.currency.converter.feature_currency_converter.presentation.ui.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testSubmitButtonFunctionality() {
        onView(withId(R.id.etAmount)).perform(typeText("100"))
        onView(withId(R.id.btnSubmit)).perform(click())
    }

    @Test
    fun testLoadingState() {
        onView(withId(R.id.pb)).check(matches(isDisplayed()))
    }
}
