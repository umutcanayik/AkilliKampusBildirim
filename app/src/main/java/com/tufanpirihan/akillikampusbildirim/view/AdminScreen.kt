package com.tufanpirihan.akillikampusbildirim.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.tufanpirihan.akillikampusbildirim.viewmodel.AdminViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    navController: NavHostController,
    viewModel: AdminViewModel = viewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val emergencyState by viewModel.emergencyState.collectAsState()

    var showEmergencyDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var selectedNotification by remember { mutableStateOf<Notification?>(null) }


    val snackbarHostState = remember { SnackbarHostState() }


    LaunchedEffect(updateState) {
        when (updateState) {
            is AdminViewModel.UpdateState.Success -> {
                snackbarHostState.showSnackbar(
                    (updateState as AdminViewModel.UpdateState.Success).message
                )
                viewModel.resetUpdateState()
            }
            is AdminViewModel.UpdateState.Error -> {
                snackbarHostState.showSnackbar(
                    (updateState as AdminViewModel.UpdateState.Error).message
                )
                viewModel.resetUpdateState()
            }
            else -> {}
        }
    }


    LaunchedEffect(emergencyState) {
        when (emergencyState) {
            is AdminViewModel.EmergencyState.Success -> {
                snackbarHostState.showSnackbar(
                    (emergencyState as AdminViewModel.EmergencyState.Success).message
                )
                viewModel.resetEmergencyState()
            }
            is AdminViewModel.EmergencyState.Error -> {
                snackbarHostState.showSnackbar(
                    (emergencyState as AdminViewModel.EmergencyState.Error).message
                )
                viewModel.resetEmergencyState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.AdminPanelSettings,
                            contentDescription = "admin",
                            tint = Color(0xFF2979FF)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Admin Paneli", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF141414),
                    titleContentColor = Color.White
                ),
                actions = {

                    IconButton(onClick = { viewModel.fetchAllNotifications() }) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Yenile",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showEmergencyDialog = true },
                containerColor = Color(0xFFF44336),
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Warning, contentDescription = "Acil Durum")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Acil Durum Bildirimi")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF050505)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Toplam",
                    count = notifications.size,
                    color = Color(0xFF2979FF)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Açık",
                    count = notifications.count { it.status.uppercase() == "AÇIK" },
                    color = Color(0xFFFF9800)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Çözüldü",
                    count = notifications.count { it.status.uppercase() == "ÇÖZÜLDÜ" },
                    color = Color(0xFF4CAF50)
                )
            }


            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF2979FF))
                }
            }


            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notification ->
                    AdminNotificationCard(
                        notification = notification,
                        onStatusClick = {
                            selectedNotification = notification
                            showStatusDialog = true
                        }
                    )
                }
            }
        }
    }


    if (showStatusDialog && selectedNotification != null) {
        StatusUpdateDialog(
            notification = selectedNotification!!,
            onDismiss = {
                showStatusDialog = false
                selectedNotification = null
            },
            onStatusSelected = { newStatus ->
                viewModel.updateNotificationStatus(selectedNotification!!.id, newStatus)
                showStatusDialog = false
                selectedNotification = null
            }
        )
    }


    if (showEmergencyDialog) {
        EmergencyDialog(
            onDismiss = { showEmergencyDialog = false },
            onSend = { title, message ->
                viewModel.sendEmergencyNotification(title, message)
                showEmergencyDialog = false
            },
            isLoading = emergencyState is AdminViewModel.EmergencyState.Loading
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    count: Int,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                color = color,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                color = Color(0xFF888888),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun AdminNotificationCard(
    notification: Notification,
    onStatusClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                getTypeColor(notification.type),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(getTypeIcon(notification.type), fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = notification.type,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }


                Surface(
                    modifier = Modifier.clickable { onStatusClick() },
                    color = getStatusColor(notification.status),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getStatusText(notification.status),
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Düzenle",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))


            Text(
                text = notification.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))


            Text(
                text = notification.description,
                color = Color(0xFFCCCCCC),
                fontSize = 14.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = "Konum",
                        tint = Color(0xFF888888),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (notification.location != null)
                            "${notification.location.lat}, ${notification.location.lng}"
                        else
                            "Konum yok",
                        color = Color(0xFF888888),
                        fontSize = 12.sp
                    )

                }


                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Schedule,
                        contentDescription = "Tarih",
                        tint = Color(0xFF888888),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = notification.createdAt,
                        color = Color(0xFF888888),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))


            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = "Kullanıcı",
                    tint = Color(0xFF888888),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Kullanıcı ID: ${notification.userId}",
                    color = Color(0xFF888888),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun StatusUpdateDialog(
    notification: Notification,
    onDismiss: () -> Unit,
    onStatusSelected: (String) -> Unit
) {
    val statusOptions = listOf(
        "Açık" to Color(0xFFFF9800),
        "İnceleniyor" to Color(0xFF2196F3),
        "Çözüldü" to Color(0xFF4CAF50)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141414),
        title = {
            Text(
                "Durum Güncelle",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = notification.title,
                    color = Color(0xFF888888),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                statusOptions.forEach { (status, color) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onStatusSelected(status) },
                        color = if (notification.status.uppercase() == status.uppercase())
                            color.copy(alpha = 0.3f) else Color(0xFF1F1F1F),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(color, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = status,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            if (notification.status.uppercase() == status.uppercase()) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = "Seçili",
                                    tint = color
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = Color(0xFF888888))
            }
        }
    )
}

@Composable
fun EmergencyDialog(
    onDismiss: () -> Unit,
    onSend: (String, String) -> Unit,
    isLoading: Boolean
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141414),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = "Acil",
                    tint = Color(0xFFF44336)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Acil Durum Bildirimi",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Bu bildirim tüm kullanıcılara gönderilecek!",
                    color = Color(0xFFFF9800),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Başlık", color = Color(0xFF888888)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1F1F1F),
                        unfocusedContainerColor = Color(0xFF1F1F1F),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color(0xFFF44336),
                        unfocusedIndicatorColor = Color(0xFF555555)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Mesaj", color = Color(0xFF888888)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 4,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1F1F1F),
                        unfocusedContainerColor = Color(0xFF1F1F1F),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color(0xFFF44336),
                        unfocusedIndicatorColor = Color(0xFF555555)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSend(title, message) },
                enabled = title.isNotBlank() && message.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF44336)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(Icons.Filled.Send, contentDescription = "Gönder")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gönder")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = Color(0xFF888888))
            }
        }
    )
}


