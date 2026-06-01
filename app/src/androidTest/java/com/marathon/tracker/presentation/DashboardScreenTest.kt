package com.marathon.tracker.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.marathon.tracker.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
        // Navigate to the Dashboard tab
        composeRule.onNodeWithText("Dashboard").performClick()
    }

    @Test
    fun dashboardScreen_showsWeekLabel() {
        composeRule.onNodeWithText("Week 1", substring = true).assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_showsTargetKm() {
        // Fake repo returns 42.0 target km
        composeRule.onNodeWithText("42", substring = true).assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_showsProgressBar() {
        // Weekly progress section label
        composeRule.onNodeWithText("of weekly target", substring = true).assertIsDisplayed()
    }
}
