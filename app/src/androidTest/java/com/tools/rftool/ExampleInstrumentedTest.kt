package com.tools.rftool

import android.os.Environment
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.io.File

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.tools.rftool", appContext.packageName)
    }

    @Test
    fun testExternalStoragePath() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val docs = appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        assertTrue(docs?.exists() == true)
    }
}