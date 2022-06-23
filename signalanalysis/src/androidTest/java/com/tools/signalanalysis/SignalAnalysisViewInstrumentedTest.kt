package com.tools.signalanalysis

import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.tools.rftool.test.TestActivity
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class SignalAnalysisViewInstrumentedTest {
    companion object {
        private const val TEST_SAMPLE_RATE = 2400000
    }

    @get:Rule
    val rule = activityScenarioRule<TestActivity>()

    fun setupData(activity: TestActivity) {
        activity.setData(SampleData.generate(TEST_SAMPLE_RATE * 1))
    }

    @After
    fun terminate() {
        rule.scenario.close()
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = getInstrumentation().targetContext
        assertEquals("com.tools.rftool.test", appContext.packageName)
    }

    @Test
    fun testViewInstantiation() {
        launchActivity<TestActivity>().use {
            it.onActivity { activity ->
                setupData(activity)
                onView(withId(R.id.ch_signal)).check { view, noViewFoundException ->
                    assertNotNull(view)
                }
            }
        }
    }

    @Test
    fun testPinchZoom() {
        val device = UiDevice.getInstance(getInstrumentation())

        launchActivity<TestActivity>().use {
            it.onActivity { activity ->
                setupData(activity)
                onView(withId(com.tools.signalanalysis.R.id.ch_signal)).let { viewInteraction ->
                    viewInteraction.check { view, noViewFoundException ->
                        matches(isDisplayed())
                        assertNull(noViewFoundException)
                        assertNotNull(view)

                        device.findObject(UiSelector().resourceId("com.tools.signalanalysis:id/ch_signal"))
                            .pinchOut(25, 10)
                    }
                }
            }
        }
    }
}