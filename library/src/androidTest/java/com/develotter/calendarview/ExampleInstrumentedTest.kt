package com.develotter.calendarview

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.assertEquals

/** Instrumentation smoke test for the library's self-instrumenting test APK. */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // A library has no application APK with its namespace as applicationId.
        // The default test host uses the namespace plus .test and targets itself.
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        assertEquals("com.develotter.calendarview.test", instrumentation.context.packageName)
        assertEquals(instrumentation.context.packageName, instrumentation.targetContext.packageName)
    }
}
