package com.nexus.ai.utils

import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.Collections
import java.util.Timer
import java.util.TimerTask

class MockedLocationService : Service() {

    // ─────────────────────────────────────────────
    // TAG (dulu static final String)
    // ─────────────────────────────────────────────
    companion object {
        private val TAG = MockedLocationService::class.java.simpleName
    }

    // ─────────────────────────────────────────────
    // LiveData — dulu: protected final MutableLiveData<...> x = new ...
    // Kotlin: cukup val, karena MutableLiveData tidak perlu null-check
    // ─────────────────────────────────────────────
    protected val mockState = MutableLiveData<MockState>()
    protected val mockedLocation = MutableLiveData<Location>()

    // Daftar provider — dulu: List<MockedLocationProvider> providers
    private val providers = mutableListOf<MockedLocationProvider>()

    // Timer & Set task — dulu: Timer + synchronizedSet(HashSet<>())
    private val timer = Timer()
    private val tasks: MutableSet<TimerTask> =
        Collections.synchronizedSet(HashSet<TimerTask>())

    // ─────────────────────────────────────────────
    // BINDING
    // ─────────────────────────────────────────────
    override fun onBind(intent: Intent): IBinder {
        indicateBinding()
        return MockedBinder(this)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "Mock is finished")
        synchronized(tasks) {
            tasks.forEach { it.cancel() }
            tasks.clear()
        }
        providers.forEach { it.shutdown() }
        providers.clear()
        mockState.postValue(MockState.NOT_MOCKED)
        return super.onUnbind(intent)
    }

    private fun indicateBinding() {
        mockState.postValue(MockState.SERVICE_BOUND)
    }

    // ─────────────────────────────────────────────
    // START MOCK
    // ─────────────────────────────────────────────
    protected fun startMockedService(
        longitude: Double,
        latitude: Double,
        longitudeDistance: Double,
        latitudeDistance: Double,
        mockMilli: Long,
        maxTime: Int,
        mockSpeed: Float
    ) {
        try {
            providers.clear()
            providers.add(MockedLocationProvider(LocationManager.GPS_PROVIDER, this))
            providers.add(MockedLocationProvider(LocationManager.NETWORK_PROVIDER, this))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                providers.add(MockedLocationProvider(LocationManager.FUSED_PROVIDER, this))
            }

            val mockedTask = MockedTask(
                longitude, latitude,
                longitudeDistance, latitudeDistance,
                maxTime, mockSpeed
            )
            timer.schedule(mockedTask, 0L, mockMilli)
            tasks.add(mockedTask)
            mockState.postValue(MockState.MOCKED)
        } catch (e: SecurityException) {
            Log.e(TAG, "Could not construct mock location providers!", e)
            mockState.postValue(MockState.MOCK_ERROR)
        }
    }

    // ─────────────────────────────────────────────
    // INNER CLASS: MockedTask
    // Java: class (non-static inner) → Kotlin: inner class
    // ─────────────────────────────────────────────
    inner class MockedTask(
        private var longitude: Double,
        private var latitude: Double,
        private val longitudeMockedDistance: Double,
        private val latitudeMockedDistance: Double,
        private val maxLocationTimes: Int,
        private val speed: Float
    ) : TimerTask() {

        private var currentTimes = 0

        override fun run() {
            val value = Location(LocationManager.GPS_PROVIDER).apply {
                this.longitude = this@MockedTask.longitude
                this.latitude = this@MockedTask.latitude
            }

            if (speed > 0f) {
                with(value) {
                    this.speed = this@MockedTask.speed
                    accuracy = 0.1f
                    time = System.currentTimeMillis()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        speedAccuracyMetersPerSecond = 0.01f
                    }
                }
            }

            mockedLocation.postValue(value)

            // Akses `providers` dari outer class — ini bisa karena `inner class`
            providers.forEach { prov ->
                prov.pushLocation(latitude, longitude)
            }

            ++currentTimes
            if (maxLocationTimes != 0 && maxLocationTimes == currentTimes) {
                cancel()
                stopSelf()
                mockState.postValue(MockState.NOT_MOCKED)
                return
            }
            latitude += latitudeMockedDistance
            longitude += longitudeMockedDistance
        }
    }

    // ─────────────────────────────────────────────
    // NESTED CLASS: MockedBinder
    // Java: static inner class → Kotlin: nested class (default, bukan inner)
    // ─────────────────────────────────────────────
    class MockedBinder(private val service: MockedLocationService) : Binder() {

        val mockState: LiveData<MockState> = service.mockState
        val mockedLocation: LiveData<Location> = service.mockedLocation

        fun continueMock() {
            service.indicateBinding()
        }

        fun startMock(
            longitude: Double,
            latitude: Double,
            longitudeDistance: Double,
            latitudeDistance: Double,
            mockMilli: Long,
            maxTimes: Int,
            mockSpeed: Float
        ) {
            service.startMockedService(
                longitude, latitude,
                longitudeDistance, latitudeDistance,
                mockMilli, maxTimes, mockSpeed
            )
        }
    }
}