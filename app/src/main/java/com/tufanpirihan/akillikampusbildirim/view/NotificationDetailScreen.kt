package com.tufanpirihan.akillikampusbildirim.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.tufanpirihan.akillikampusbildirim.model.Notification
import com.tufanpirihan.akillikampusbildirim.model.FollowRequest
import com.tufanpirihan.akillikampusbildirim.repository.RetrofitClient
import kotlinx.coroutines.launch
import java.net.URLDecoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    navController: NavHostController,
    notificationJson: String
) {
    val notification = remember {
        try {
            val decoded = URLDecoder.decode(notificationJson, "UTF-8")
            Gson().fromJson(decoded, Notification::class.java)
        } catch (e: Exception) {
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bildirim Detayı", fontWeight = FontWeight.Bold) },
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
        },
        containerColor = Color(0xFF050505)
    ) { padding ->
        if (notification == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("❌", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Bildirim yüklenemedi",
                        color = Color(0xFFFF5252),
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            NotificationDetailContent(
                notification = notification,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun NotificationDetailContent(
    notification: Notification,
    modifier: Modifier = Modifier
) {
    var isFollowed by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isCheckingFollow by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val userId = RetrofitClient.getUserId()

    LaunchedEffect(notification.id) {
        if (userId != null) {
            try {
                val response = RetrofitClient.apiService.getFollowedReports(userId)
                if (response.isSuccessful) {
                    val followedList = response.body() ?: emptyList()
                    isFollowed = followedList.any { it.id == notification.id }
                }
            } catch (e: Exception) {
            }
        }
        isCheckingFollow = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141414), shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(getTypeColor(notification.type), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(getTypeIcon(notification.type), fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = notification.type,
                    color = Color(0xFF888888),
                    fontSize = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .background(getStatusColor(notification.status), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = getStatusText(notification.status),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Açıklama",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = notification.description,
                    color = Color(0xFFCCCCCC),
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
        }

        notification.location?.let { location ->
            if (location.lat != 0.0 || location.lng != 0.0) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF2979FF)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Konum",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Enlem: ${location.lat}\nBoylam: ${location.lng}",
                            color = Color(0xFF888888),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.DateRange,
                        contentDescription = null,
                        tint = Color(0xFF888888)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Oluşturulma: ${notification.createdAt}",
                        color = Color(0xFF888888),
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (userId != null && !isLoading) {
                    isLoading = true
                    scope.launch {
                        try {
                            if (isFollowed) {
                                val response = RetrofitClient.apiService.unfollowReport(
                                    FollowRequest(userId = userId, reportId = notification.id)
                                )
                                if (response.isSuccessful) {
                                    isFollowed = false
                                }
                            } else {
                                val response = RetrofitClient.apiService.followReport(
                                    FollowRequest(userId = userId, reportId = notification.id)
                                )
                                if (response.isSuccessful) {
                                    isFollowed = true
                                }
                            }
                        } catch (e: Exception) {
                        }
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFollowed) Color(0xFF424242) else Color(0xFF2979FF)
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading && !isCheckingFollow && userId != null
        ) {
            if (isLoading || isCheckingFollow) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    if (isFollowed) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFollowed) "Takipten Çık" else "Bildirimi Takip Et",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}