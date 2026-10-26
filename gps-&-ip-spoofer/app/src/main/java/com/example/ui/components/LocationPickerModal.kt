package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.PresetLocation
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberOutline
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.rDp
import com.example.ui.theme.rIconDp
import com.example.ui.theme.rSp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocationPickerModal(
    currentLat: Double,
    currentLng: Double,
    currentName: String,
    presetList: List<PresetLocation> = PresetLocation.PRESETS,
    onPresetSelected: (PresetLocation) -> Unit,
    onCustomCoordinatesSubmitted: (lat: Double, lng: Double, name: String) -> Unit,
    onEditRequested: ((PresetLocation) -> Unit)? = null,
    onDeleteRequested: ((PresetLocation) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Presets, 1: Custom Coords
    var customLatText by remember { mutableStateOf(currentLat.toString()) }
    var customLngText by remember { mutableStateOf(currentLng.toString()) }
    var customNameText by remember { mutableStateOf(currentName) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredPresets = remember(searchQuery, presetList) {
        if (searchQuery.isBlank()) presetList
        else presetList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.country.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, CyberOutline, RoundedCornerShape(20.dp)),
            color = CyberSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Target Location",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberSurfaceVariant)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == 0) Color.White else Color.Transparent)
                            .combinedClickable(onClick = { selectedTab = 0 })
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Saved Cities (${filteredPresets.size})",
                            color = if (selectedTab == 0) Color.Black else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == 1) Color.White else Color.Transparent)
                            .combinedClickable(onClick = { selectedTab = 1 })
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ New Location",
                            color = if (selectedTab == 1) Color.Black else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    Text(
                        text = "💡 Press & hold any location to edit or delete it",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search city or country...", color = Color.Gray) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = CyberOutline,
                            focusedBorderColor = Color.White,
                            focusedContainerColor = CyberBackground,
                            unfocusedContainerColor = CyberBackground,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        itemsIndexed(filteredPresets, key = { index, preset -> "${preset.id}_$index" }) { _, preset ->
                            var showContextMenu by remember { mutableStateOf(false) }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .combinedClickable(
                                            onClick = {
                                                onPresetSelected(preset)
                                                onDismiss()
                                            },
                                            onLongClick = {
                                                showContextMenu = true
                                            }
                                        ),
                                    colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberOutline),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = preset.flagEmoji,
                                            fontSize = 24.sp,
                                            modifier = Modifier.padding(end = 12.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = preset.name,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${preset.latitude}°, ${preset.longitude}°",
                                                color = Color.LightGray,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Options",
                                            tint = Color.Gray,
                                            modifier = Modifier
                                                .combinedClickable(onClick = { showContextMenu = true })
                                                .padding(4.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showContextMenu,
                                    onDismissRequest = { showContextMenu = false },
                                    modifier = Modifier.background(CyberSurface)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit Location", color = Color.White) },
                                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = Color.White) },
                                        onClick = {
                                            showContextMenu = false
                                            onEditRequested?.invoke(preset)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete Location", color = Color(0xFFFF5252)) },
                                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFFF5252)) },
                                        onClick = {
                                            showContextMenu = false
                                            onDeleteRequested?.invoke(preset)
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Custom Latitude & Longitude Input
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = customNameText,
                            onValueChange = { customNameText = it },
                            label = { Text("Location Name", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = CyberOutline,
                                focusedBorderColor = Color.White,
                                focusedContainerColor = CyberBackground,
                                unfocusedContainerColor = CyberBackground,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = customLatText,
                                onValueChange = { customLatText = it },
                                label = { Text("Latitude", color = Color.Gray) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = CyberOutline,
                                    focusedBorderColor = Color.White,
                                    focusedContainerColor = CyberBackground,
                                    unfocusedContainerColor = CyberBackground,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedTextField(
                                value = customLngText,
                                onValueChange = { customLngText = it },
                                label = { Text("Longitude", color = Color.Gray) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = CyberOutline,
                                    focusedBorderColor = Color.White,
                                    focusedContainerColor = CyberBackground,
                                    unfocusedContainerColor = CyberBackground,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val lat = customLatText.toDoubleOrNull() ?: currentLat
                                val lng = customLngText.toDoubleOrNull() ?: currentLng
                                onCustomCoordinatesSubmitted(lat, lng, customNameText)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save & Inject Location", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

