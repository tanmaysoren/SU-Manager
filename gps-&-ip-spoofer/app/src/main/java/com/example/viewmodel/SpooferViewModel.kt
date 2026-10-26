package com.example.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.HistoryRepository
import com.example.data.LocationIpHistory
import com.example.model.PresetIpNode
import com.example.model.PresetLocation
import com.example.service.MockLocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HistoryFilter { ALL, RECENT, HIGH_ACCURACY }

class SpooferViewModel(private val repository: HistoryRepository) : ViewModel() {

    private val _locationName = MutableStateFlow("Tokyo (Shibuya Crossing)")
    val locationName: StateFlow<String> = _locationName.asStateFlow()

    private val _latitude = MutableStateFlow(35.6595)
    val latitude: StateFlow<Double> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow(139.7004)
    val longitude: StateFlow<Double> = _longitude.asStateFlow()

    private val _altitude = MutableStateFlow(15.0)
    val altitude: StateFlow<Double> = _altitude.asStateFlow()

    private val _accuracy = MutableStateFlow(3.5f)
    val accuracy: StateFlow<Float> = _accuracy.asStateFlow()

    private val _movementMode = MutableStateFlow("STATIC") // STATIC, WALK, DRIVE
    val movementMode: StateFlow<String> = _movementMode.asStateFlow()

    private val _mockIp = MutableStateFlow("203.0.113.195")
    val mockIp: StateFlow<String> = _mockIp.asStateFlow()

    private val _ipCountry = MutableStateFlow("Japan")
    val ipCountry: StateFlow<String> = _ipCountry.asStateFlow()

    private val _ipCity = MutableStateFlow("Tokyo")
    val ipCity: StateFlow<String> = _ipCity.asStateFlow()

    private val _carrierName = MutableStateFlow("NTT Communications")
    val carrierName: StateFlow<String> = _carrierName.asStateFlow()

    private val _asn = MutableStateFlow("AS2914")
    val asn: StateFlow<String> = _asn.asStateFlow()

    val isSpoofingActive: StateFlow<Boolean> = MockLocationService.isMockingActive
    val hasMockPermission: StateFlow<Boolean> = MockLocationService.hasMockPermission
    val statusMessage: StateFlow<String> = MockLocationService.statusMessage

    // Search and Filter History
    val searchQuery = MutableStateFlow("")
    val historyFilter = MutableStateFlow(HistoryFilter.ALL)

    val historyList: StateFlow<List<LocationIpHistory>> = combine(
        repository.allHistory.catch { emit(emptyList()) },
        searchQuery,
        historyFilter
    ) { list, query, filter ->
        var filtered = list
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.locationName.contains(query, ignoreCase = true) ||
                        it.mockIpAddress.contains(query, ignoreCase = true) ||
                        it.ipCountry.contains(query, ignoreCase = true) ||
                        it.ipCity.contains(query, ignoreCase = true)
            }
        }
        when (filter) {
            HistoryFilter.RECENT -> filtered.take(10)
            HistoryFilter.HIGH_ACCURACY -> filtered.filter { it.accuracy <= 5.0f }
            HistoryFilter.ALL -> filtered
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _locations = MutableStateFlow<List<PresetLocation>>(PresetLocation.PRESETS)
    val locations: StateFlow<List<PresetLocation>> = _locations.asStateFlow()

    private val _ipNodes = MutableStateFlow<List<PresetIpNode>>(PresetIpNode.NODES)
    val ipNodes: StateFlow<List<PresetIpNode>> = _ipNodes.asStateFlow()

    fun addOrUpdateLocation(location: PresetLocation, context: Context? = null) {
        val current = _locations.value.toMutableList()
        val index = current.indexOfFirst { it.id == location.id }
        if (index >= 0) {
            current[index] = location
        } else {
            current.add(0, location)
        }
        _locations.value = current
        selectPresetLocation(location, context)
    }

    fun deleteLocation(location: PresetLocation) {
        _locations.value = _locations.value.filter { it.id != location.id }
    }

    fun addOrUpdateIpNode(node: PresetIpNode, context: Context? = null) {
        val current = _ipNodes.value.toMutableList()
        val index = current.indexOfFirst { it.ip == node.ip }
        if (index >= 0) {
            current[index] = node
        } else {
            current.add(0, node)
        }
        _ipNodes.value = current
        selectPresetIp(node, context)
    }

    fun deleteIpNode(node: PresetIpNode) {
        _ipNodes.value = _ipNodes.value.filter { it.ip != node.ip }
    }

    fun loadPreferences(context: Context) {
        try {
            val prefs = context.getSharedPreferences("spoofer_prefs", Context.MODE_PRIVATE)
            val savedName = prefs.getString("location_name", null)

            if (!savedName.isNullOrEmpty()) {
                _locationName.value = savedName
                _latitude.value = prefs.getFloat("latitude", 35.6595f).toDouble()
                _longitude.value = prefs.getFloat("longitude", 139.7004f).toDouble()
                _mockIp.value = prefs.getString("mock_ip", "203.0.113.195") ?: "203.0.113.195"
                _ipCountry.value = prefs.getString("ip_country", "Japan") ?: "Japan"
                _ipCity.value = prefs.getString("ip_city", "Tokyo") ?: "Tokyo"
                _carrierName.value = prefs.getString("carrier_name", "NTT Communications") ?: "NTT Communications"
                _asn.value = prefs.getString("asn", "AS2914") ?: "AS2914"
                _movementMode.value = prefs.getString("movement_mode", "STATIC") ?: "STATIC"

                MockLocationService.updateCurrentState(_latitude.value, _longitude.value, _locationName.value, _mockIp.value)
            }
        } catch (e: Throwable) {
            // Safe fallback
        }
    }

    private fun savePreferences(context: Context?) {
        if (context == null) return
        try {
            val prefs = context.getSharedPreferences("spoofer_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("location_name", _locationName.value)
                .putFloat("latitude", _latitude.value.toFloat())
                .putFloat("longitude", _longitude.value.toFloat())
                .putString("mock_ip", _mockIp.value)
                .putString("ip_country", _ipCountry.value)
                .putString("ip_city", _ipCity.value)
                .putString("carrier_name", _carrierName.value)
                .putString("asn", _asn.value)
                .putString("movement_mode", _movementMode.value)
                .apply()
        } catch (e: Throwable) {
            // Safe fallback
        }
    }

    fun setLocationName(name: String, context: Context? = null) {
        _locationName.value = name
        savePreferences(context)
        MockLocationService.updateCurrentState(_latitude.value, _longitude.value, _locationName.value, _mockIp.value)
    }

    fun setCoordinates(lat: Double, lng: Double, context: Context? = null) {
        _latitude.value = lat
        _longitude.value = lng
        savePreferences(context)
        MockLocationService.updateCurrentState(_latitude.value, _longitude.value, _locationName.value, _mockIp.value)
    }

    fun setAltitude(alt: Double) { _altitude.value = alt }
    fun setAccuracy(acc: Float) { _accuracy.value = acc }

    fun setMovementMode(mode: String, context: Context? = null) {
        _movementMode.value = mode
        savePreferences(context)
    }

    fun setMockIp(
        ip: String,
        country: String,
        city: String,
        carrier: String = _carrierName.value,
        asn: String = _asn.value,
        context: Context? = null
    ) {
        _mockIp.value = ip
        _ipCountry.value = country
        _ipCity.value = city
        _carrierName.value = carrier
        _asn.value = asn
        savePreferences(context)
        MockLocationService.updateCurrentState(_latitude.value, _longitude.value, _locationName.value, _mockIp.value)
    }

    fun selectPresetLocation(preset: PresetLocation, context: Context? = null) {
        _locationName.value = preset.name
        _latitude.value = preset.latitude
        _longitude.value = preset.longitude
        _mockIp.value = preset.defaultIp
        _ipCountry.value = preset.country
        _ipCity.value = preset.name.substringBefore(" (")
        val matchedNode = PresetIpNode.NODES.firstOrNull { it.ip == preset.defaultIp }
        if (matchedNode != null) {
            _carrierName.value = matchedNode.carrier
            _asn.value = matchedNode.asn
        }
        savePreferences(context)
        MockLocationService.updateCurrentState(_latitude.value, _longitude.value, _locationName.value, _mockIp.value)
    }

    fun selectPresetIp(node: PresetIpNode, context: Context? = null) {
        _mockIp.value = node.ip
        _ipCountry.value = node.country
        _ipCity.value = node.city
        _carrierName.value = node.carrier
        _asn.value = node.asn
        savePreferences(context)
        MockLocationService.updateCurrentState(_latitude.value, _longitude.value, _locationName.value, _mockIp.value)
    }

    fun randomizeIp(context: Context? = null) {
        val current = _mockIp.value
        val availableNodes = PresetIpNode.NODES.filter { it.ip != current }
        val randomNode = availableNodes.randomOrNull() ?: PresetIpNode.NODES.first()
        selectPresetIp(randomNode, context)
    }

    fun toggleSpoofing(context: Context) {
        if (isSpoofingActive.value) {
            stopSpoofing(context)
        } else {
            startSpoofing(context)
        }
    }

    fun startSpoofing(context: Context) {
        MockLocationService.setMockingActive(true)
        MockLocationService.updateCurrentState(_latitude.value, _longitude.value, _locationName.value, _mockIp.value)
        savePreferences(context)

        runCatching {
            val locationIntent = Intent(context, MockLocationService::class.java).apply {
                putExtra(MockLocationService.EXTRA_LATITUDE, _latitude.value)
                putExtra(MockLocationService.EXTRA_LONGITUDE, _longitude.value)
                putExtra(MockLocationService.EXTRA_ALTITUDE, _altitude.value)
                putExtra(MockLocationService.EXTRA_NAME, _locationName.value)
                putExtra(MockLocationService.EXTRA_IP, _mockIp.value)
                putExtra(MockLocationService.EXTRA_MOVEMENT_MODE, _movementMode.value)
            }
            context.startService(locationIntent)
        }

        // Save session log into Room Database
        viewModelScope.launch {
            try {
                val newEntry = LocationIpHistory(
                    locationName = _locationName.value,
                    latitude = _latitude.value,
                    longitude = _longitude.value,
                    altitude = _altitude.value,
                    accuracy = _accuracy.value,
                    mockIpAddress = _mockIp.value,
                    ipCountry = _ipCountry.value,
                    ipCity = _ipCity.value,
                    carrierName = _carrierName.value,
                    asn = _asn.value,
                    timestamp = System.currentTimeMillis(),
                    isActive = true,
                    notes = "Mode: ${_movementMode.value}"
                )
                repository.insert(newEntry)
            } catch (e: Throwable) {
                // Ignore DB error
            }
        }
    }

    fun stopSpoofing(context: Context) {
        MockLocationService.setMockingActive(false)
        try {
            val stopLocIntent = Intent(context, MockLocationService::class.java).apply {
                action = MockLocationService.ACTION_STOP
            }
            context.startService(stopLocIntent)
        } catch (e: Throwable) {
            // Safe catch
        }

        viewModelScope.launch {
            try {
                repository.deactivateAll()
            } catch (e: Throwable) {
                // Ignore DB error
            }
        }
    }

    fun reapplyHistoryItem(item: LocationIpHistory, context: Context) {
        _locationName.value = item.locationName
        _latitude.value = item.latitude
        _longitude.value = item.longitude
        _altitude.value = item.altitude
        _accuracy.value = item.accuracy
        _mockIp.value = item.mockIpAddress
        _ipCountry.value = item.ipCountry
        _ipCity.value = item.ipCity
        _carrierName.value = item.carrierName
        _asn.value = item.asn

        startSpoofing(context)
    }

    fun deleteHistoryItem(item: LocationIpHistory) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}

class SpooferViewModelFactory(private val repository: HistoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpooferViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SpooferViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
