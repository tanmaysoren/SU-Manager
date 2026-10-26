package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class MockLocationService : Service() {

    private val binder = LocalBinder()
    private var locationManager: LocationManager? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var serviceJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    inner class LocalBinder : Binder() {
        fun getService(): MockLocationService = this@MockLocationService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        } catch (e: Exception) {
            // Fallback if Play Services unavailable
        }
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            stopMocking()
            stopSelf()
            return START_NOT_STICKY
        }

        val lat = intent?.getDoubleExtra(EXTRA_LATITUDE, 35.6595) ?: 35.6595
        val lng = intent?.getDoubleExtra(EXTRA_LONGITUDE, 139.7004) ?: 139.7004
        val altitude = intent?.getDoubleExtra(EXTRA_ALTITUDE, 0.0) ?: 0.0
        val locationName = intent?.getStringExtra(EXTRA_NAME) ?: "Mock Location"
        val mockIp = intent?.getStringExtra(EXTRA_IP) ?: "104.28.19.42"
        val movementMode = intent?.getStringExtra(EXTRA_MOVEMENT_MODE) ?: "STATIC"

        try {
            val notification = createNotification(locationName, mockIp)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                    )
                } catch (e: Throwable) {
                    try {
                        startForeground(NOTIFICATION_ID, notification)
                    } catch (ex: Throwable) {
                        // Ignore notification foreground restriction
                    }
                }
            } else {
                try {
                    startForeground(NOTIFICATION_ID, notification)
                } catch (e: Throwable) {
                    // Ignore notification foreground restriction
                }
            }
        } catch (e: Throwable) {
            // Ignore foreground error
        }

        _statusMessage.value = "Virtual Location Active ($locationName)"
        _isMockingActive.value = true
        SpooferTileService.requestTileUpdate(this)

        try {
            startMocking(lat, lng, altitude, locationName, mockIp, movementMode)
        } catch (e: Throwable) {
            // Safe fallback
        }

        return START_STICKY
    }

    private fun startMocking(
        targetLat: Double,
        targetLng: Double,
        targetAlt: Double,
        locationName: String,
        mockIp: String,
        movementMode: String
    ) {
        serviceJob?.cancel()

        _isMockingActive.value = true
        _currentLat.value = targetLat
        _currentLng.value = targetLng
        _currentLocationName.value = locationName
        _currentMockIp.value = mockIp

        setupTestProviders()

        serviceJob = scope.launch {
            var step = 0
            while (isActive) {
                var currentStepLat = targetLat
                var currentStepLng = targetLng

                if (movementMode == "WALK") {
                    // Small circular walk offset (~50 meters radius)
                    val angle = (step % 360) * (Math.PI / 180.0)
                    currentStepLat += Math.sin(angle) * 0.0004
                    currentStepLng += Math.cos(angle) * 0.0004
                    step += 10
                } else if (movementMode == "DRIVE") {
                    // Larger motion (~500 meters drift)
                    val angle = (step % 360) * (Math.PI / 180.0)
                    currentStepLat += Math.sin(angle) * 0.0030
                    currentStepLng += Math.cos(angle) * 0.0030
                    step += 5
                }

                _currentLat.value = currentStepLat
                _currentLng.value = currentStepLng

                pushMockLocations(currentStepLat, currentStepLng, targetAlt)

                delay(300)
            }
        }
    }

    private fun setupTestProviders() {
        try {
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )

            for (provider in providers) {
                try {
                    locationManager?.removeTestProvider(provider)
                } catch (e: Exception) {
                    // Ignore
                }

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        locationManager?.addTestProvider(
                            provider,
                            false, false, false, false,
                            true, true, true,
                            ProviderProperties.POWER_USAGE_LOW,
                            ProviderProperties.ACCURACY_FINE
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        locationManager?.addTestProvider(
                            provider,
                            "requiresNetwork" == provider,
                            false,
                            false,
                            false,
                            true,
                            true,
                            true,
                            1,
                            1
                        )
                    }
                    locationManager?.setTestProviderEnabled(provider, true)
                } catch (e: Exception) {
                    // Ignore provider registration failure if unsupported by specific OS build
                }
            }

            try {
                fusedLocationClient?.setMockMode(true)
            } catch (e: Exception) {
                // Ignore if Play Services fails mock mode setup
            }

            _hasMockPermission.value = true
            _statusMessage.value = "System Test Provider Active"
        } catch (e: SecurityException) {
            _hasMockPermission.value = false
            _statusMessage.value = "Mock Location permission required in Developer Options"
        } catch (e: Exception) {
            _statusMessage.value = "Test Provider Note: ${e.localizedMessage ?: "Simulation Mode"}"
        }
    }

    private fun pushMockLocations(lat: Double, lng: Double, alt: Double) {
        val now = System.currentTimeMillis()
        val elapsedNanos = SystemClock.elapsedRealtimeNanos()

        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        for (provider in providers) {
            try {
                val mockLocation = Location(provider).apply {
                    latitude = lat
                    longitude = lng
                    altitude = alt
                    accuracy = 1.0f
                    speed = 0.0f
                    bearing = 0.0f
                    time = now
                    elapsedRealtimeNanos = elapsedNanos
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        verticalAccuracyMeters = 0.1f
                        speedAccuracyMetersPerSecond = 0.1f
                        bearingAccuracyDegrees = 0.1f
                    }
                }
                locationManager?.setTestProviderLocation(provider, mockLocation)
            } catch (e: Exception) {
                // Ignore single provider push error
            }
        }

        try {
            val fusedLoc = Location("fused").apply {
                latitude = lat
                longitude = lng
                altitude = alt
                accuracy = 1.0f
                speed = 0.0f
                bearing = 0.0f
                time = now
                elapsedRealtimeNanos = elapsedNanos
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    verticalAccuracyMeters = 0.1f
                    speedAccuracyMetersPerSecond = 0.1f
                    bearingAccuracyDegrees = 0.1f
                }
            }
            fusedLocationClient?.setMockLocation(fusedLoc)
        } catch (e: Exception) {
            // Ignore fused client error
        }
    }

    fun stopMocking() {
        serviceJob?.cancel()
        serviceJob = null
        _isMockingActive.value = false
        SpooferTileService.requestTileUpdate(this)
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
            "fused"
        )
        for (provider in providers) {
            try {
                locationManager?.removeTestProvider(provider)
            } catch (e: Exception) {
                // Cleanup
            }
        }
        try {
            fusedLocationClient?.setMockMode(false)
        } catch (e: Exception) {
            // Safe cleanup
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        stopMocking()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "GPS Mocker Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows status of system GPS and IP address spoofing"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(locationName: String, mockIp: String): android.app.Notification {
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, MockLocationService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GPS Mocker Active")
            .setContentText("Location: $locationName â€¢ Mock IP: $mockIp")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openAppIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "TURN OFF SPOOFER",
                stopIntent
            )
            .addAction(
                android.R.drawable.ic_menu_view,
                "OPEN APP",
                openAppIntent
            )
            .build()
    }

    companion object {
        const val CHANNEL_ID = "gps_ip_spoofer_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_STOP = "com.example.service.STOP_MOCKING"
        const val EXTRA_LATITUDE = "extra_lat"
        const val EXTRA_LONGITUDE = "extra_lng"
        const val EXTRA_ALTITUDE = "extra_alt"
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_IP = "extra_ip"
        const val EXTRA_MOVEMENT_MODE = "extra_movement_mode"

        private val _isMockingActive = MutableStateFlow(false)
        val isMockingActive: StateFlow<Boolean> = _isMockingActive.asStateFlow()

        fun setMockingActive(active: Boolean) {
            _isMockingActive.value = active
        }

        fun updateCurrentState(lat: Double, lng: Double, locationName: String, mockIp: String) {
            _currentLat.value = lat
            _currentLng.value = lng
            _currentLocationName.value = locationName
            _currentMockIp.value = mockIp
        }

        private val _currentLat = MutableStateFlow(35.6595)
        val currentLat: StateFlow<Double> = _currentLat.asStateFlow()

        private val _currentLng = MutableStateFlow(139.7004)
        val currentLng: StateFlow<Double> = _currentLng.asStateFlow()

        private val _currentLocationName = MutableStateFlow("Tokyo (Shibuya Crossing)")
        val currentLocationName: StateFlow<String> = _currentLocationName.asStateFlow()

        private val _currentMockIp = MutableStateFlow("203.0.113.195")
        val currentMockIp: StateFlow<String> = _currentMockIp.asStateFlow()

        private val _hasMockPermission = MutableStateFlow(false)
        val hasMockPermission: StateFlow<Boolean> = _hasMockPermission.asStateFlow()

        private val _statusMessage = MutableStateFlow("Standby")
        val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()
    }
}
