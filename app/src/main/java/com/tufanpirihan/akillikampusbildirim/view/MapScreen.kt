package com.tufanpirihan.akillikampusbildirim.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.gson.Gson
import com.tufanpirihan.akillikampusbildirim.model.Notification
import com.tufanpirihan.akillikampusbildirim.viewmodel.MapViewModel
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavHostController,
    viewModel: MapViewModel = viewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    var selectedNotification by remember { mutableStateOf<Notification?>(null) }

    val ataturkUniversity = LatLng(39.9055, 41.2658)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ataturkUniversity, 15f)
    }

    LaunchedEffect(Unit) {
        viewModel.fetchNotifications()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Harita", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF141414),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(mapType = MapType.NORMAL),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false
                )
            ) {
                notifications.forEach { notification ->
                    notification.location?.let { location ->
                        if (location.lat != 0.0 && location.lng != 0.0) {
                            Marker(
                                state = MarkerState(position = LatLng(location.lat, location.lng)),
                                title = notification.title,
                                snippet = notification.type,
                                onClick = {
                                    selectedNotification = notification
                                    true
                                }
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF2979FF)
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { viewModel.setFilter(null) },
                    label = { Text("Tümü") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2979FF),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF141414),
                        labelColor = Color.White
                    )
                )
                listOf("Sağlık", "Güvenlik", "Çevre", "Kayıp-Bulundu", "Teknik Arıza").forEach { type ->
                    FilterChip(
                        selected = selectedFilter == type,
                        onClick = { viewModel.setFilter(type) },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2979FF),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF141414),
                            labelColor = Color.White
                        )
                    )
                }
            }

            selectedNotification?.let { notification ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            val gson = Gson()
                            val json = URLEncoder.encode(gson.toJson(notification), "UTF-8")
                            navController.navigate("notification_detail/$json")
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(getTypeColor(notification.type)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(getTypeIcon(notification.type), fontSize = 24.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = notification.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = notification.type,
                                color = Color(0xFF888888),
                                fontSize = 14.sp
                            )
                            Text(
                                text = notification.description,
                                color = Color(0xFFCCCCCC),
                                fontSize = 12.sp,
                                maxLines = 2
                            )
                        }

                        IconButton(onClick = { selectedNotification = null }) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Kapat",
                                tint = Color(0xFF888888)
                            )
                        }
                    }
                }
            }
        }
    }
}