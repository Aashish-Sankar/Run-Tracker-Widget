package com.marathon.tracker.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
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
class TodayScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun todayScreen_showsWorkoutCard() {
        // The fake repo provides an Easy 10km plan; verify it surfaces in the UI
        composeRule.onNodeWithText("Easy Run 10.0km", substring = true).assertIsDisplayed()
    }

    @Test
    fun todayScreen_showsCoachNote() {
        composeRule.onNodeWithText("Keep it easy today.", substring = true).assertIsDisplayed()
    }

    @Test
    fun todayScreen_showsMarkAsCompletedButton() {
        composeRule.onNodeWithText("Mark as Completed").assertIsDisplayed()
    }

    @Test
    fun todayScreen_showsPhaseChip() {
        composeRule.onNodeWithText("Base Building", substring = true).assertIsDisplayed()
    }
}
