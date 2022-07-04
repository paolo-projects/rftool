package com.tools.rftool

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.util.TypedValue
import com.tools.rftool.repository.AppConfigurationRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.kotlin.*
import java.math.BigInteger

/**
 * Test for FFT N values, they should be restricted
 * to a given set of valid values
 */
class AppConfigValidFftTest {
    @OptIn(DelicateCoroutinesApi::class)
    private val mainThreadSurrogate = newSingleThreadContext("MAIN")

    private lateinit var mockContext: Context
    private lateinit var mockPreferences: SharedPreferences
    private lateinit var mockPrefEditor: SharedPreferences.Editor
    private lateinit var mockResources: Resources

    private fun createSharedPrefsMock() {
        mockPrefEditor = mock {
            on { putInt(eq("rf_fft_n"), any()) } doAnswer Mockito.RETURNS_SELF
            on { commit() } doReturn true
        }
        mockPreferences = mock {
            on { edit() } doReturn mockPrefEditor
        }
    }

    private fun createResourcesMock() {
        mockResources = mock {
            on { getInteger(any()) } doReturn 0
            on { getString(any()) } doReturn ""
            on { getBoolean(any()) } doReturn false
            on { getDimension(any()) } doReturn 0f
        }
    }

    private fun createContextMock() {
        mockContext = mock {
            on {
                getSharedPreferences(
                    "rftool_rf_preferences",
                    Context.MODE_PRIVATE
                )
            } doReturn mockPreferences
            on {
                resources
            } doReturn mockResources
        }
    }

    @Before
    fun initMockContext() {
        createSharedPrefsMock()
        createResourcesMock()
        createContextMock()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun savesValidFftN() = runTest {
        Dispatchers.setMain(mainThreadSurrogate)
        val repo = AppConfigurationRepository(mockContext)
        repo.dispatcher = Dispatchers.Main
        val fftNCaptor = argumentCaptor<Int>()

        val values = mapOf(
            500 to 512,
            640 to 512,
            980 to 1024,
            1500 to 1024,
            1700 to 2048,
            512 to 512,
            1024 to 1024,
            2048 to 2048,
            3500 to 3072,
            100000000 to 8192,
            0 to 512,
            -50000000 to 512
        )

        var i = 0
        for (value in values.entries) {
            repo.setFftN(value.key)
            i++
            verify(mockPrefEditor, times(i)).putInt(eq("rf_fft_n"), fftNCaptor.capture())
            assertEquals(value.value, fftNCaptor.lastValue)
        }
    }
}