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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.tufanpirihan.akillikampusbildirim.model.Notification
import com.tufanpirihan.akillikampusbildirim.model.NotificationType
import com.tufanpirihan.akillikampusbildirim.model.ProfileResponse
import com.tufanpirihan.akillikampusbildirim.repository.RetrofitClient
import com.tufanpirihan.akillikampusbildirim.viewmodel.ProfileViewModel
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = viewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val followedReports by viewModel.followedReports.collectAsState()
    val notificationPrefs by viewModel.notificationPrefs.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editDepartment by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
        viewModel.fetchFollowedReports()
    }

    LaunchedEffect(updateState) {
        if (updateState is ProfileViewModel.UpdateState.Success) {
            showEditDialog = false
            viewModel.resetUpdateState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil", fontWeight = FontWeight.Bold) },
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
        when (val state = profileState) {
            is ProfileViewModel.ProfileState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF2979FF))
                }
            }
            is ProfileViewModel.ProfileState.Success -> {
                Column(modifier = Modifier.padding(padding)) {
                    ProfileHeader(
                        profile = state.profile,
                        onEditClick = {
                            editDepartment = state.profile.department
                            showEditDialog = true
                        },
                        onLogoutClick = {
                            RetrofitClient.clearSession()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFF141414),
                        contentColor = Color(0xFF2979FF)
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Bildirim Ayarları") },
                            selectedContentColor = Color(0xFF2979FF),
                            unselectedContentColor = Color(0xFF888888)
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Takip Edilenler") },
                            selectedContentColor = Color(0xFF2979FF),
                            unselectedContentColor = Color(0xFF888888)
                        )
                    }

                    when (selectedTab) {
                        0 -> NotificationPrefsTab(
                            notificationPrefs = notificationPrefs,
                            onPrefsChanged = { viewModel.updateNotificationPrefs(it) }
                        )
                        1 -> FollowedReportsTab(
                            followedReports = followedReports,
                            onReportClick = { notification ->
                                val gson = Gson()
                                val json = URLEncoder.encode(gson.toJson(notification), "UTF-8")
                                navController.navigate("notification_detail/$json")
                            }
                        )
                    }
                }
            }
            is ProfileViewModel.ProfileState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("❌", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            color = Color(0xFFFF5252),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.fetchProfile() }) {
                            Text("Tekrar Dene")
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Profili Düzenle", color = Color.White) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editDepartment,
                        onValueChange = { editDepartment = it },
                        label = { Text("Birim") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF2979FF),
                            unfocusedBorderColor = Color(0xFF888888),
                            focusedLabelColor = Color(0xFF2979FF),
                            unfocusedLabelColor = Color(0xFF888888)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.updateProfile(editDepartment, null) },
                    enabled = updateState !is ProfileViewModel.UpdateState.Loading
                ) {
                    if (updateState is ProfileViewModel.UpdateState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text("Kaydet")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("İptal", color = Color(0xFF888888))
                }
            },
            containerColor = Color(0xFF1F1F1F)
        )
    }
}

@Composable
fun ProfileHeader(
    profile: ProfileResponse,
    onEditClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF141414))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF2979FF)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = profile.fullName.take(1).uppercase(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = profile.fullName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = profile.email,
            fontSize = 14.sp,
            color = Color(0xFF888888)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = { },
                label = { Text(profile.department) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Business,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFF1F1F1F),
                    labelColor = Color.White,
                    leadingIconContentColor = Color(0xFF2979FF)
                )
            )
            AssistChip(
                onClick = { },
                label = { Text(profile.role.replaceFirstChar { it.uppercase() }) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFF1F1F1F),
                    labelColor = Color.White,
                    leadingIconContentColor = Color(0xFF2979FF)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onEditClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF2979FF)
                )
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Düzenle")
            }
            Button(
                onClick = onLogoutClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5252)
                )
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Çıkış Yap")
            }
        }
    }
}

@Composable
fun NotificationPrefsTab(
    notificationPrefs: Set<String>,
    onPrefsChanged: (Set<String>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Hangi tür bildirimleri almak istiyorsunuz?",
            color = Color(0xFF888888),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        NotificationType.values().forEach { type ->
            val isChecked = notificationPrefs.contains(type.name)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        val newPrefs = if (isChecked) {
                            notificationPrefs - type.name
                        } else {
                            notificationPrefs + type.name
                        }
                        onPrefsChanged(newPrefs)
                    },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(getTypeColor(type.displayName)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(getTypeIcon(type.displayName), fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = type.displayName,
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )

                    Switch(
                        checked = isChecked,
                        onCheckedChange = {
                            val newPrefs = if (it) {
                                notificationPrefs + type.name
                            } else {
                                notificationPrefs - type.name
                            }
                            onPrefsChanged(newPrefs)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF2979FF),
                            uncheckedThumbColor = Color(0xFF888888),
                            uncheckedTrackColor = Color(0xFF1F1F1F)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun FollowedReportsTab(
    followedReports: List<Notification>,
    onReportClick: (Notification) -> Unit
) {
    if (followedReports.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⭐", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Henüz takip edilen bildirim yok",
                    color = Color(0xFF888888),
                    fontSize = 16.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(followedReports) { notification ->
                FollowedReportCard(
                    notification = notification,
                    onClick = { onReportClick(notification) }
                )
            }
        }
    }
}

@Composable
fun FollowedReportCard(
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
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = notification.type,
                    color = Color(0xFF888888),
                    fontSize = 12.sp
                )
            }

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
    }
}