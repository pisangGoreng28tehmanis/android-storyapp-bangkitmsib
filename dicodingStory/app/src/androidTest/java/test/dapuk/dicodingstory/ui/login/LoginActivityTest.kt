package test.dapuk.dicodingstory.ui.login

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.junit.runner.RunWith
import test.dapuk.dicodingstory.R
import test.dapuk.dicodingstory.ui.main.MainActivity
import test.dapuk.dicodingstory.utils.EspressoIdlingResource
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginActivityTest{
    @get:Rule
    val activity = ActivityScenarioRule(LoginActivity::class.java)
    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }
    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }
    @Test
    fun testLoginLogout() {
        onView(withId(R.id.ed_login_email))
            .perform(typeText("a128b4ky0966@bangkit.academy"), closeSoftKeyboard())

        onView(withId(R.id.ed_login_password))
            .perform(typeText("12345678"), closeSoftKeyboard())

        Intents.init()
        onView(withId(R.id.btn_login))
            .perform(click())

        intended(hasComponent(MainActivity::class.java.name))
        onView(withId(R.id.tv_story))
            .check(matches(withText("Stories")))

        onView(withId(R.id.btn_logout))
            .perform(click())

        intended(hasComponent(LoginActivity::class.java.name))

        Intents.release()
    }
}
