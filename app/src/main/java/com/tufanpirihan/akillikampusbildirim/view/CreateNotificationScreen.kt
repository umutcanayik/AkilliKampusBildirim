package com.tufanpirihan.akillikampusbildirim.view

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.tufanpirihan.akillikampusbildirim.model.NotificationType
import com.tufanpirihan.akillikampusbildirim.viewmodel.CreateNotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNotificationScreen(
    navController: NavHostController,
    viewModel: CreateNotificationViewModel = viewModel()
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<NotificationType?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var locationText by remember { mutableStateOf("Konum seçilmedi") }

    val notificationState by viewModel.notificationState.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        latitude = it.latitude
                        longitude = it.longitude
                        locationText = "Lat: %.4f, Lng: %.4f".format(it.latitude, it.longitude)
                    }
                }
            } catch (e: SecurityException) {
                locationText = "Konum alınamadı"
            }
        }
    }

    LaunchedEffect(notificationState) {
        if (notificationState is CreateNotificationViewModel.CreateNotificationState.Success) {
            navController.navigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yeni Bildirim", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Bildirim Türü",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotificationType.values().forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.displayName) },
                        leadingIcon = {
                            Text(
                                when (type) {
                                    NotificationType.HEALTH -> "🏥"
                                    NotificationType.SECURITY -> "🚨"
                                    NotificationType.ENVIRONMENT -> "🌱"
                                    NotificationType.LOST_FOUND -> "🔍"
                                    NotificationType.TECHNICAL -> "🔧"
                                },
                                fontSize = 16.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2979FF),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1F1F1F),
                            labelColor = Color(0xFF888888)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Başlık",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Bildirim başlığı", color = Color(0xFF888888)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1F1F1F),
                    unfocusedContainerColor = Color(0xFF1F1F1F),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF2979FF),
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color(0xFF2979FF)
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Açıklama",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                placeholder = { Text("Detaylı açıklama yazın...", color = Color(0xFF888888)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1F1F1F),
                    unfocusedContainerColor = Color(0xFF1F1F1F),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF2979FF),
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color(0xFF2979FF)
                ),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Fotoğraf (Opsiyonel)",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedImageUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Seçilen Fotoğraf",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { selectedImageUri = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Kaldır", tint = Color.White)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color(0xFF1F1F1F), RoundedCornerShape(16.dp))
                        .border(2.dp, Color(0xFF333333), RoundedCornerShape(16.dp))
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.AddAPhoto,
                            contentDescription = "Fotoğraf Ekle",
                            tint = Color(0xFF888888),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Fotoğraf Seç",
                            color = Color(0xFF888888),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Konum (Opsiyonel)",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val permission = Manifest.permission.ACCESS_FINE_LOCATION
                        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                            try {
                                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                    location?.let {
                                        latitude = it.latitude
                                        longitude = it.longitude
                                        locationText = "Lat: %.4f, Lng: %.4f".format(it.latitude, it.longitude)
                                    }
                                }
                            } catch (e: SecurityException) {
                                locationText = "Konum alınamadı"
                            }
                        } else {
                            locationPermissionLauncher.launch(permission)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F1F1F)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.MyLocation, contentDescription = null, tint = Color(0xFF2979FF))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mevcut Konum", color = Color.White)
                }

                if (latitude != null) {
                    Button(
                        onClick = {
                            latitude = null
                            longitude = null
                            locationText = "Konum seçilmedi"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Clear, contentDescription = null)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = locationText,
                color = if (latitude != null) Color(0xFF4CAF50) else Color(0xFF888888),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (selectedType != null) {
                        viewModel.createNotification(
                            title = title,
                            description = description,
                            type = selectedType!!,
                            latitude = latitude,
                            longitude = longitude,
                            imageUri = selectedImageUri,
                            context = context
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF)),
                shape = RoundedCornerShape(16.dp),
                enabled = title.isNotBlank() && description.isNotBlank() && selectedType != null &&
                        notificationState !is CreateNotificationViewModel.CreateNotificationState.Loading
            ) {
                if (notificationState is CreateNotificationViewModel.CreateNotificationState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bildirimi Gönder", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            if (notificationState is CreateNotificationViewModel.CreateNotificationState.Error) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = (notificationState as CreateNotificationViewModel.CreateNotificationState.Error).message,
                    color = Color(0xFFFF5252),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private val CircleShape = RoundedCornerShape(50)
