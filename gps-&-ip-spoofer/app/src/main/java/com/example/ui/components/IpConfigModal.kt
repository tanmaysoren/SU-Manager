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
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Router
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
import com.example.model.PresetIpNode
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberOutline
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.rDp
import com.example.ui.theme.rIconDp
import com.example.ui.theme.rSp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IpConfigModal(
    currentIp: String,
    currentCountry: String,
    currentCity: String,
    currentCarrier: String = "Cloudflare Inc.",
    currentAsn: String = "AS13335",
    nodesList: List<PresetIpNode> = PresetIpNode.NODES,
    onPresetSelected: (PresetIpNode) -> Unit,
    onCustomIpSubmitted: (ip: String, country: String, city: String, carrier: String, asn: String) -> Unit,
    onEditRequested: ((PresetIpNode) -> Unit)? = null,
    onDeleteRequested: ((PresetIpNode) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Server Nodes, 1: Custom IP
    var customIpText by remember(currentIp) { mutableStateOf(currentIp) }
    var customCountryText by remember(currentCountry) { mutableStateOf(currentCountry) }
    var customCityText by remember(currentCity) { mutableStateOf(currentCity) }
    var customCarrierText by remember(currentCarrier) { mutableStateOf(currentCarrier) }
    var customAsnText by remember(currentAsn) { mutableStateOf(currentAsn) }

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
                            imageVector = Icons.Default.Public,
                            contentDescription = "Public IP",
                            tint = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "IP & Proxy Region",
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
                            text = "Nodes (${nodesList.size})",
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
                            text = "+ New IP / Carrier",
                            color = if (selectedTab == 1) Color.Black else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    Text(
                        text = "💡 Press & hold any proxy node to edit or delete it",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        itemsIndexed(nodesList, key = { index, node -> "${node.ip}_$index" }) { _, node ->
                            var showContextMenu by remember { mutableStateOf(false) }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .combinedClickable(
                                            onClick = {
                                                onPresetSelected(node)
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
                                            text = node.flagEmoji,
                                            fontSize = 24.sp,
                                            modifier = Modifier.padding(end = 12.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${node.city}, ${node.country}",
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "IP: ${node.ip} • ${node.carrier}",
                                                color = Color.LightGray,
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = "ASN: ${node.asn} • ${node.provider}",
                                                color = Color.Gray,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Text(
                                            text = "${node.latencyMs} ms",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showContextMenu,
                                    onDismissRequest = { showContextMenu = false },
                                    modifier = Modifier.background(CyberSurface)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit IP Node", color = Color.White) },
                                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = Color.White) },
                                        onClick = {
                                            showContextMenu = false
                                            onEditRequested?.invoke(node)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete IP Node", color = Color(0xFFFF5252)) },
                                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFFF5252)) },
                                        onClick = {
                                            showContextMenu = false
                                            onDeleteRequested?.invoke(node)
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = customIpText,
                            onValueChange = { customIpText = it },
                            label = { Text("IP Address (IPv4 / IPv6)", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
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

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = customCityText,
                                onValueChange = { customCityText = it },
                                label = { Text("City", color = Color.Gray) },
                                modifier = Modifier.weight(1f),
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
                                value = customCountryText,
                                onValueChange = { customCountryText = it },
                                label = { Text("Country", color = Color.Gray) },
                                modifier = Modifier.weight(1f),
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

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = customCarrierText,
                                onValueChange = { customCarrierText = it },
                                label = { Text("Carrier / ISP Name", color = Color.Gray) },
                                modifier = Modifier.weight(1f),
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
                                value = customAsnText,
                                onValueChange = { customAsnText = it },
                                label = { Text("ASN (e.g., AS13335)", color = Color.Gray) },
                                modifier = Modifier.weight(1f),
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
                                onCustomIpSubmitted(
                                    customIpText,
                                    customCountryText,
                                    customCityText,
                                    customCarrierText,
                                    customAsnText
                                )
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Router, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Custom IP Config", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

