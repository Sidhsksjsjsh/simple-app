package com.nexus.ai.utils

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.SystemClock
import android.util.Log

class MockedLocationProvider(
    private val providerName: String,
    private val ctx: Context
) {

    init {
        var powerUsage = 0
        var accuracy = 5

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            powerUsage = 1
            accuracy = 2
        }

        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        startup(lm, powerUsage, accuracy)
    }

    private fun startup(lm: LocationManager, powerUsage: Int, accuracy: Int) {
        startup(lm, powerUsage, accuracy, 0)
    }

    private fun startup(
        lm: LocationManager,
        powerUsage: Int,
        accuracy: Int,
        currentRetryCount: Int
    ) {
        if (currentRetryCount < MAX_RETRY_COUNT) {
            try {
                shutdown()
                lm.addTestProvider(
                    providerName,
                    false, false, false, false, false,
                    true, true,
                    powerUsage, accuracy
                )
                lm.setTestProviderEnabled(providerName, true)
            } catch (t: Throwable) {
                Log.e(TAG, "startup: ", t)
                startup(lm, powerUsage, accuracy, currentRetryCount + 1)
            }
        } else {
            throw SecurityException("Not allowed to perform MOCK_LOCATION")
        }
    }

    fun pushLocation(lat: Double, lon: Double) {
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val mockLocation = Location(providerName).apply {
            latitude = lat
            longitude = lon
            altitude = 3.0
            time = System.currentTimeMillis()
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            speed = 0.01F
            bearing = 1F
            accuracy = 3F
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                bearingAccuracyDegrees = 0.1F
                verticalAccuracyMeters = 0.1F
                speedAccuracyMetersPerSecond = 0.01F
            }
        }
        Log.d(TAG, "pushLocation: $lat, $lon")
        lm.setTestProviderLocation(providerName, mockLocation)
    }

    fun shutdown() {
        try {
            val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.removeTestProvider(providerName)
        } catch (_: Throwable) {
        }
    }

    companion object {
        private val TAG = MockedLocationProvider::class.java.simpleName
        private const val MAX_RETRY_COUNT = 3
    }
}