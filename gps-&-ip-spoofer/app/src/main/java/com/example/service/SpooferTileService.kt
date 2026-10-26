package com.example.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class SpooferTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val isCurrentlyActive = MockLocationService.isMockingActive.value

        if (isCurrentlyActive) {
            // Stop spoofing
            val stopIntent = Intent(this, MockLocationService::class.java).apply {
                action = MockLocationService.ACTION_STOP
            }
            startService(stopIntent)
        } else {
            // Start spoofing using saved SharedPreferences
            val prefs = getSharedPreferences("spoofer_prefs", Context.MODE_PRIVATE)
            val lat = prefs.getFloat("latitude", 35.6595f).toDouble()
            val lng = prefs.getFloat("longitude", 139.7004f).toDouble()
            val alt = prefs.getFloat("altitude", 0f).toDouble()
            val name = prefs.getString("location_name", "Tokyo (Shibuya Crossing)") ?: "Tokyo"
            val ip = prefs.getString("ip_address", "203.0.113.195") ?: "203.0.113.195"
            val movementMode = prefs.getString("movement_mode", "STATIC") ?: "STATIC"

            val startIntent = Intent(this, MockLocationService::class.java).apply {
                putExtra(MockLocationService.EXTRA_LATITUDE, lat)
                putExtra(MockLocationService.EXTRA_LONGITUDE, lng)
                putExtra(MockLocationService.EXTRA_ALTITUDE, alt)
                putExtra(MockLocationService.EXTRA_NAME, name)
                putExtra(MockLocationService.EXTRA_IP, ip)
                putExtra(MockLocationService.EXTRA_MOVEMENT_MODE, movementMode)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(startIntent)
            } else {
                startService(startIntent)
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            updateTileState()
        }, 300)
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isActive = MockLocationService.isMockingActive.value

        tile.state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = if (isActive) "Spoofer ON" else "Spoofer OFF"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (isActive) "Active" else "Tap to Turn On"
        }

        try {
            tile.icon = Icon.createWithResource(this, android.R.drawable.ic_menu_mylocation)
        } catch (e: Exception) {
            // Fallback
        }

        tile.updateTile()
    }

    companion object {
        fun requestTileUpdate(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    requestListeningState(
                        context,
                        ComponentName(context, SpooferTileService::class.java)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
