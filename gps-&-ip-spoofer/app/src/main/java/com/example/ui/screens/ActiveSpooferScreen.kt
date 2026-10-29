package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.IpConfigModal
import com.example.ui.components.LocationPickerModal
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberError
import com.example.ui.theme.CyberOutline
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSecondary
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTertiary
import com.example.ui.theme.rDp
import com.example.ui.theme.rIconDp
import com.example.ui.theme.rSp
import com.example.viewmodel.SpooferViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActiveSpooferScreen(
    viewModel: SpooferViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadPreferences(context)
    }

    val locationName by viewModel.locationName.collectAsStateWithLifecycle()
    val latitude by viewModel.latitude.collectAsStateWithLifecycle()
    val longitude by viewModel.longitude.collectAsStateWithLifecycle()
    val mockIp by viewModel.mockIp.collectAsStateWithLifecycle()
    val ipCountry by viewModel.ipCountry.collectAsStateWithLifecycle()
    val ipCity by viewModel.ipCity.collectAsStateWithLifecycle()
    val carrierName by viewModel.carrierName.collectAsStateWithLifecycle()
    val asn by viewModel.asn.collectAsStateWithLifecycle()
    val movementMode by viewModel.movementMode.collectAsStateWithLifecycle()

    val isSpoofingActive by viewModel.isSpoofingActive.collectAsStateWithLifecycle()
    val hasMockPermission by viewModel.hasMockPermission.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    val locations by viewModel.locations.collectAsStateWithLifecycle()
    val ipNodes by viewModel.ipNodes.collectAsStateWithLifecycle()

    var editingLocation by remember { mutableStateOf<com.example.model.PresetLocation?>(null) }
    var editingIpNode by remember { mutableStateOf<com.example.model.PresetIpNode?>(null) }

    var showLocationPicker by remember { mutableStateOf(false) }
    var showIpConfigModal by remember { mutableStateOf(false) }
    var showDiagnosticGuide by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.toggleSpoofing(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Main Power Control Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isSpoofingActive) Color(0xFF102820) else CyberSurface
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isSpoofingActive) CyberTertiary else CyberOutline
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isSpoofingActive) CyberTertiary else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSpoofingActive) "SYSTEM SPOOFING ACTIVE" else "SPOOFER STANDBY",
                            color = if (isSpoofingActive) CyberTertiary else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isSpoofingActive) "$locationName ($mockIp)" else "Ready to inject mock location & IP",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = statusMessage,
                        color = CyberPrimary,
                        fontSize = 11.sp
                    )
                }

                // Big Power Toggle Button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSpoofingActive) Brush.linearGradient(
                                listOf(
                                    CyberTertiary,
                                    Color(0xFF00B0FF)
                                )
                            )
                            else Brush.linearGradient(listOf(CyberSurfaceVariant, Color(0xFF2A354D)))
                        )
                        .clickable {
                            if (isSpoofingActive) {
                                viewModel.stopSpoofing(context)
                            } else {
                                val fineGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context, android.Manifest.permission.ACCESS_FINE_LOCATION
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                val coarseGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context, android.Manifest.permission.ACCESS_COARSE_LOCATION
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                if (!fineGranted && !coarseGranted) {
                                    val perms = mutableListOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                        perms.add(android.Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    permissionLauncher.launch(perms.toTypedArray())
                                } else {
                                    viewModel.startSpoofing(context)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Power",
                        tint = if (isSpoofingActive) Color.Black else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GPS Location Config Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberOutline),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = CyberPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mock GPS Location",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberSurfaceVariant)
                            .clickable { showLocationPicker = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = CyberPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Change",
                                color = CyberPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = locationName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "Lat: $latitude° | Lng: $longitude°",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Movement mode selector
                Text(
                    text = "Movement Drift Mode",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    MovementChip(
                        label = "Static",
                        icon = Icons.Default.Navigation,
                        isSelected = movementMode == "STATIC",
                        onClick = { viewModel.setMovementMode("STATIC", context) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    MovementChip(
                        label = "Walk (5km/h)",
                        icon = Icons.Default.DirectionsWalk,
                        isSelected = movementMode == "WALK",
                        onClick = { viewModel.setMovementMode("WALK", context) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    MovementChip(
                        label = "Drive (50km/h)",
                        icon = Icons.Default.DirectionsCar,
                        isSelected = movementMode == "DRIVE",
                        onClick = { viewModel.setMovementMode("DRIVE", context) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System IP Address & Proxy Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberOutline),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "IP",
                            tint = CyberSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "System Virtual IP Proxy",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberSurfaceVariant)
                                .clickable { viewModel.randomizeIp(context) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🎲 Randomize",
                                color = CyberSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberSurfaceVariant)
                                .clickable { showIpConfigModal = true }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit IP",
                                    tint = CyberSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Config",
                                    color = CyberSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = mockIp,
                                color = CyberSecondary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "$ipCity, $ipCountry",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Carrier: $carrierName ($asn)",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick IP Node Selectors
                Text(
                    text = "Quick IP Proxy Nodes",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ipNodes.size, key = { ipNodes[it].ip }) { index ->
                        val node = ipNodes[index]
                        val isSelected = mockIp == node.ip
                        var showContextMenu by remember { mutableStateOf(false) }

                        Box {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color.White else CyberSurfaceVariant)
                                    .border(
                                        1.dp,
                                        if (isSelected) Color.White else CyberOutline,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .combinedClickable(
                                        onClick = { viewModel.selectPresetIp(node, context) },
                                        onLongClick = { showContextMenu = true }
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = node.flagEmoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = node.city,
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            androidx.compose.material3.DropdownMenu(
                                expanded = showContextMenu,
                                onDismissRequest = { showContextMenu = false },
                                modifier = Modifier.background(CyberSurface)
                            ) {
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("Edit IP Node", color = Color.White) },
                                    leadingIcon = { Icon(Icons.Default.Edit, null, tint = Color.White) },
                                    onClick = {
                                        showContextMenu = false
                                        editingIpNode = node
                                    }
                                )
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("Delete IP Node", color = Color(0xFFFF5252)) },
                                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFFF5252)) },
                                    onClick = {
                                        showContextMenu = false
                                        viewModel.deleteIpNode(node)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Developer Setup Assistant Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDiagnosticGuide = true },
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberOutline),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = CyberPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Android System Setup Guide",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "How to enable 'Select mock location app' in Developer Options",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Back button handling inside screen to dismiss modals if open
    androidx.activity.compose.BackHandler(
        enabled = showLocationPicker || showIpConfigModal || showDiagnosticGuide || editingLocation != null || editingIpNode != null
    ) {
        showLocationPicker = false
        showIpConfigModal = false
        showDiagnosticGuide = false
        editingLocation = null
        editingIpNode = null
    }

    // Modals
    if (showLocationPicker) {
        LocationPickerModal(
            currentLat = latitude,
            currentLng = longitude,
            currentName = locationName,
            presetList = locations,
            onPresetSelected = { preset -> viewModel.selectPresetLocation(preset, context) },
            onCustomCoordinatesSubmitted = { lat, lng, name ->
                val locationName = if (name.isBlank()) "Custom Coordinates" else name
                val newPreset = com.example.model.PresetLocation(
                    id = "custom_${System.currentTimeMillis()}",
                    name = locationName,
                    country = "Custom",
                    flagEmoji = "📍",
                    latitude = lat,
                    longitude = lng,
                    defaultIp = mockIp,
                    description = "User-defined location"
                )
                viewModel.addOrUpdateLocation(newPreset, context)
            },
            onEditRequested = { loc -> editingLocation = loc },
            onDeleteRequested = { loc -> viewModel.deleteLocation(loc) },
            onDismiss = { showLocationPicker = false }
        )
    }

    if (showIpConfigModal) {
        IpConfigModal(
            currentIp = mockIp,
            currentCountry = ipCountry,
            currentCity = ipCity,
            currentCarrier = carrierName,
            currentAsn = asn,
            nodesList = ipNodes,
            onPresetSelected = { node -> viewModel.selectPresetIp(node, context) },
            onCustomIpSubmitted = { ip, country, city, carrier, asnVal ->
                val newIpNode = com.example.model.PresetIpNode(
                    ip = if (ip.isBlank()) "104.28.19.42" else ip,
                    country = if (country.isBlank()) "United States" else country,
                    city = if (city.isBlank()) "New York" else city,
                    flagEmoji = "📍",
                    latencyMs = 0,
                    provider = "Custom",
                    carrier = if (carrier.isBlank()) "Cloudflare Inc." else carrier,
                    asn = if (asnVal.isBlank()) "AS13335" else asnVal
                )
                viewModel.addOrUpdateIpNode(newIpNode, context)
            },
            onEditRequested = { node -> editingIpNode = node },
            onDeleteRequested = { node -> viewModel.deleteIpNode(node) },
            onDismiss = { showIpConfigModal = false }
        )
    }

    if (editingLocation != null) {
        com.example.ui.components.EditLocationDialog(
            location = editingLocation!!,
            onSave = { updated -> viewModel.addOrUpdateLocation(updated) },
            onDismiss = { editingLocation = null }
        )
    }

    if (editingIpNode != null) {
        com.example.ui.components.EditIpNodeDialog(
            node = editingIpNode!!,
            onSave = { updated -> viewModel.addOrUpdateIpNode(updated) },
            onDismiss = { editingIpNode = null }
        )
    }

    if (showDiagnosticGuide) {
        DiagnosticGuideDialog(
            hasMockPermission = hasMockPermission,
            onDismiss = { showDiagnosticGuide = false }
        )
    }
}

@Composable
private fun MovementChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) CyberPrimary else CyberSurfaceVariant)
            .border(
                1.dp,
                if (isSelected) CyberPrimary else CyberOutline,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.Black else Color.White,
                modifier = Modifier.size(14.rIconDp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = if (isSelected) Color.Black else Color.White,
                fontSize = 11.rSp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
