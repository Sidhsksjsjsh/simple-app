package com.nexus.ai.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

class MockLocationService : Service() {

    companion object {
        const val ACTION_START = "START_MOCK"
        const val ACTION_STOP = "STOP_MOCK"
        const val ACTION_UPDATE = "UPDATE_MOCK"
        const val EXTRA_LAT = "lat"
        const val EXTRA_LNG = "lng"

        private const val CHANNEL_ID = "mock_location_channel"
        private const val NOTIF_ID = 1001

        // Flag status, bisa diakses dari luar
        @Volatile var isRunning = false
            private set

        // ====== API publik untuk trigger dari luar ======
        fun start(context: Context, lat: Double, lng: Double) {
            val intent = Intent(context, MockLocationService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_LAT, lat)
                putExtra(EXTRA_LNG, lng)
            }
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }

        fun update(context: Context, lat: Double, lng: Double) {
            if (!isRunning) return
            val intent = Intent(context, MockLocationService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_LAT, lat)
                putExtra(EXTRA_LNG, lng)
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, MockLocationService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var locationManager: LocationManager
    private val providerName = LocationManager.GPS_PROVIDER
    private val reusableLocation by lazy { Location(providerName) }
    private val isMocking = AtomicBoolean(false)

    @Volatile private var currentLat = -6.200000
    @Volatile private var currentLng = 106.816666

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> { stopMocking(); return START_NOT_STICKY }
            ACTION_UPDATE -> {
                currentLat = intent.getDoubleExtra(EXTRA_LAT, currentLat)
                currentLng = intent.getDoubleExtra(EXTRA_LNG, currentLng)
                return START_STICKY
            }
            else -> {
                currentLat = intent?.getDoubleExtra(EXTRA_LAT, currentLat) ?: currentLat
                currentLng = intent?.getDoubleExtra(EXTRA_LNG, currentLng) ?: currentLng
                startAsForeground()
                startLoop()
            }
        }
        return START_STICKY
    }

    private fun startAsForeground() {
        isRunning = true
        val notif = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, notif,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIF_ID, notif)
        }
    }

    private fun startLoop() {
        if (isMocking.getAndSet(true)) return

        try {
            runCatching { locationManager.removeTestProvider(providerName) }
            locationManager.addTestProvider(
                providerName, false, false, false, false,
                true, true, true,
                android.location.Criteria.POWER_LOW,
                android.location.Criteria.ACCURACY_FINE
            )
            locationManager.setTestProviderEnabled(providerName, true)
        } catch (e: SecurityException) {
            isMocking.set(false)
            isRunning = false
            Toast.makeText(this,
                "Pilih app ini di Developer Options dulu!",
                Toast.LENGTH_LONG).show()
            stopSelf()
            return
        }

        serviceScope.launch {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            while (isActive && isMocking.get()) {
                pushLocation(currentLat, currentLng)
                delay(1000L)
            }
        }
    }

    private fun pushLocation(lat: Double, lng: Double) {
        try {
            with(reusableLocation) {
                latitude = lat
                longitude = lng
                accuracy = 1f
                time = System.currentTimeMillis()
                elapsedRealtimeNanos = System.nanoTime()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    verticalAccuracyMeters = 1f
                    speedAccuracyMetersPerSecond = 0.1f
                    bearingAccuracyDegrees = 0.1f
                }
            }
            locationManager.setTestProviderLocation(providerName, reusableLocation)
        } catch (_: Exception) { }
    }

    private fun stopMocking() {
        isRunning = false
        if (isMocking.getAndSet(false)) {
            runCatching { locationManager.removeTestProvider(providerName) }
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(): Notification {
        val stopIntent = PendingIntent.getService(
            this, 0,
            Intent(this, MockLocationService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Mock Location Aktif")
            .setContentText("Lokasi sedang dimanipulasi")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, "Stop", stopIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID, "Mock Location", NotificationManager.IMPORTANCE_LOW
            ).apply { setShowBadge(false) }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(ch)
        }
    }

    override fun onDestroy() {
        isRunning = false
        if (isMocking.getAndSet(false)) {
            runCatching { locationManager.removeTestProvider(providerName) }
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}