package com.tufanpirihan.akillikampusbildirim.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.tufanpirihan.akillikampusbildirim.model.Notification
import com.tufanpirihan.akillikampusbildirim.model.NotificationType
import com.tufanpirihan.akillikampusbildirim.viewmodel.NotificationViewModel
import java.net.URLEncoder
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: NotificationViewModel = viewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val showOnlyOpen by viewModel.showOnlyOpen.collectAsState()
    val showOnlyFollowed by viewModel.showOnlyFollowed.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Bildirimler", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF141414),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("map") }) {
                        Icon(
                            Icons.Filled.Map,
                            contentDescription = "Harita",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(
                            Icons.Filled.AccountCircle,
                            contentDescription = "Profil",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { navController.navigate("admin") }) {
                        Icon(
                            Icons.Filled.ManageAccounts,
                            contentDescription = "ADMİN",
                            tint = Color.White
                        )
                    }

                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_notification") },
                containerColor = Color(0xFF2979FF),
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Yeni Bildirim")
            }
        },
        containerColor = Color(0xFF050505)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                placeholder = { Text("Başlık veya açıklama ara...", color = Color(0xFF888888)) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "Ara", tint = Color(0xFF888888))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Temizle", tint = Color(0xFF888888))
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1F1F1F),
                    unfocusedContainerColor = Color(0xFF1F1F1F),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF2979FF),
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { viewModel.updateFilter(null) },
                    label = { Text("Tümü") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2979FF),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1F1F1F),
                        labelColor = Color(0xFF888888)
                    )
                )

                NotificationType.values().forEach { type ->
                    FilterChip(
                        selected = selectedFilter == type,
                        onClick = { viewModel.updateFilter(type) },
                        label = { Text(type.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2979FF),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1F1F1F),
                            labelColor = Color(0xFF888888)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showOnlyOpen,
                    onClick = { viewModel.toggleShowOnlyOpen() },
                    label = { Text("Sadece Açık") },
                    leadingIcon = if (showOnlyOpen) {
                        { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFF9800),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1F1F1F),
                        labelColor = Color(0xFF888888)
                    )
                )

                FilterChip(
                    selected = showOnlyFollowed,
                    onClick = { viewModel.toggleShowOnlyFollowed() },
                    label = { Text("Takip Edilenler") },
                    leadingIcon = if (showOnlyFollowed) {
                        { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF4CAF50),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1F1F1F),
                        labelColor = Color(0xFF888888)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF2979FF))
                }
            } else if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📭", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Henüz bildirim yok",
                            color = Color(0xFF888888),
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notifications) { notification ->
                        NotificationCard(notification = notification) {
                            val gson = Gson()
                            val json = URLEncoder.encode(gson.toJson(notification), "UTF-8")
                            navController.navigate("notification_detail/$json")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(getTypeColor(notification.type), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(getTypeIcon(notification.type), fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = notification.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        if (notification.isFollowed) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = "Takip Ediliyor",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = notification.description,
                        color = Color(0xFF888888),
                        fontSize = 14.sp,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .background(getStatusColor(notification.status), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = getStatusText(notification.status),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = notification.type,
                    color = Color(0xFF666666),
                    fontSize = 12.sp
                )
                Text(
                    text = notification.createdAt,
                    color = Color(0xFF666666),
                    fontSize = 12.sp
                )
            }
        }
    }
}

fun getTypeColor(type: String): Color {
    return when (type.uppercase()) {
        "SAĞLIK", "HEALTH" -> Color(0xFF4CAF50)
        "GÜVENLİK", "SECURITY" -> Color(0xFFF44336)
        "ÇEVRE", "ENVIRONMENT" -> Color(0xFF8BC34A)
        "KAYIP-BULUNDU", "LOST_FOUND" -> Color(0xFFFF9800)
        "TEKNİK ARIZA", "TECHNICAL" -> Color(0xFF2196F3)
        else -> Color.Gray
    }
}

fun getTypeIcon(type: String): String {
    return when (type.uppercase()) {
        "SAĞLIK", "HEALTH" -> "🏥"
        "GÜVENLİK", "SECURITY" -> "🚨"
        "ÇEVRE", "ENVIRONMENT" -> "🌱"
        "KAYIP-BULUNDU", "LOST_FOUND" -> "🔍"
        "TEKNİK ARIZA", "TECHNICAL" -> "🔧"
        else -> "❓"
    }
}

fun getStatusColor(status: String): Color {
    return when (status.uppercase()) {
        "AÇIK", "OPEN" -> Color(0xFFFF9800)
        "İNCELENİYOR", "IN_PROGRESS" -> Color(0xFF2196F3)
        "ÇÖZÜLDÜ", "RESOLVED" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
}

fun getStatusText(status: String): String {
    return when (status.uppercase()) {
        "AÇIK", "OPEN" -> "Açık"
        "İNCELENİYOR", "IN_PROGRESS" -> "İnceleniyor"
        "ÇÖZÜLDÜ", "RESOLVED" -> "Çözüldü"
        else -> status
    }
}
