package com.example.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.ApiClient
import com.example.data.repository.FoodEatsRepository
import com.example.security.UserRole
import com.example.ui.auth.AuthViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToAddresses: () -> Unit,
    onNavigateToCountry: () -> Unit,
    onNavigateToAdminRoleDivision: () -> Unit ,
    onNavigateToDriver: () -> Unit = {},
    onNavigateToDeliveryLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by authViewModel.uiState.collectAsState()
    val session = uiState.activeSession
    val currentUser = uiState.currentUser

    var serverUrlInput by remember { mutableStateOf(ApiClient.getBaseUrl()) }
    var connectionTestResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Cream50,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Profile & Settings",
                        fontWeight = FontWeight.Black,
                        fontSize = 19.sp,
                        color = Ink950
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (session != null) Forest500 else Sage200),
                        contentAlignment = Alignment.Center
                    ) {
                        if (session != null) {
                            Text(
                                text = (currentUser?.fullName?.take(1) ?: "U").uppercase(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PersonOutline,
                                contentDescription = "Guest",
                                tint = Ink600,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (session != null) (currentUser?.fullName ?: "Signed In User") else "Guest User",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink950
                        )
                        Text(
                            text = if (session != null) (currentUser?.email ?: "") else "Not signed in. Please sign in or register.",
                            fontSize = 12.sp,
                            color = Ink500
                        )
                        Surface(
                            color = if (session != null) Mint100 else Cream200,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = if (session != null) "ROLE: ${currentUser?.currentActiveRole?.removePrefix("ROLE_") ?: "CUSTOMER"}" else "GUEST",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (session != null) Forest800 else Rust900,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (session != null) {
                        IconButton(onClick = { authViewModel.logout() }) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Sign Out",
                                tint = Rust500
                            )
                        }
                    }
                }
            }

            // Backend Server & Database Connection Card (CRITICAL FOR PHYSICAL PHONE TESTING)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = "Server",
                                tint = Forest500
                            )
                            Text(
                                text = "Local Laptop & MySQL Connection",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink950
                            )
                        }
                    }

                    Text(
                        text = "The emulator connects directly to Spring Boot & MySQL running locally on this laptop via Android's internal loopback (10.0.2.2:8080). Zero external devices or Wi-Fi needed.",
                        fontSize = 12.sp,
                        color = Ink600,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = serverUrlInput,
                        onValueChange = { serverUrlInput = it },
                        label = { Text("Backend URL (Host Laptop)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            if (serverUrlInput != ApiClient.DEFAULT_EMULATOR_URL) {
                                TextButton(onClick = {
                                    serverUrlInput = ApiClient.DEFAULT_EMULATOR_URL
                                    ApiClient.setBaseUrl(context, serverUrlInput)
                                    Toast.makeText(context, "Reset to Emulator URL", Toast.LENGTH_SHORT).show()
                                }) {
                                    Text("Reset", fontSize = 11.sp, color = Forest600, fontWeight = FontWeight.Bold)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Forest500,
                            unfocusedBorderColor = Sage200
                        )
                    )

                    // Action buttons: Save & Test
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                ApiClient.setBaseUrl(context, serverUrlInput)
                                FoodEatsRepository.loadFromBackend(search = null , cuisine = null )
                                Toast.makeText(context, "URL Saved!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save URL", color = Ink950, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                isTestingConnection = true
                                coroutineScope.launch {
                                    ApiClient.setBaseUrl(context, serverUrlInput)
                                    val result = ApiClient.testConnection()
                                    connectionTestResult = result
                                    isTestingConnection = false
                                    if (result.first) {
                                        FoodEatsRepository.loadFromBackend(search = null , cuisine = null)
                                    }
                                }
                            },
                            enabled = !isTestingConnection,
                            colors = ButtonDefaults.buttonColors(containerColor = Forest500),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.4f)
                        ) {
                            if (isTestingConnection) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Test",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test DB Sync", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    // Connection Result Badge
                    connectionTestResult?.let { (success, message) ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = if (success) Mint100 else Cream200,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (success) Forest400 else Rust500.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (success) Forest600 else Rust500,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = message,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (success) Forest800 else Rust900
                                )
                            }
                        }
                    }
                }
            }

            // Quick Links: Address Book, RBAC Matrix, Country
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Address Book
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onNavigateToAddresses)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HomeWork,
                                contentDescription = "Addresses",
                                tint = Forest500
                            )
                            Column {
                                Text(
                                    text = "Saved Delivery Addresses",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink950
                                )
                                Text(
                                    text = if (session != null) "${uiState.userAddresses.size} saved address(es)" else "Sign in to view saved addresses",
                                    fontSize = 11.sp,
                                    color = Ink500
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Open",
                            tint = Ink400,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    HorizontalDivider(color = Sage100, modifier = Modifier.padding(horizontal = 8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (currentUser?.currentActiveRole == "ROLE_DRIVER") {
                                    onNavigateToDriver()
                                } else {
                                    onNavigateToDeliveryLogin()
                                }
                            }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("🛵 Delivery Partner Portal & Dispatch", fontWeight = FontWeight.Bold, color = Forest700, fontSize = 14.sp)
                            Text("Rider sign-in, vehicle telemetry & active jobs", fontSize = 11.sp, color = Ink500)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Ink400, modifier = Modifier.size(14.dp))
                    }

                    // RBAC Matrix
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onNavigateToAdminRoleDivision)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "RBAC",
                                tint = Forest600
                            )
                            Column {
                                Text(
                                    text = "Admin & Role Division Matrix",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink950
                                )
                                Text(
                                    text = "Customer, Merchant, Driver & Admin specifications",
                                    fontSize = 11.sp,
                                    color = Ink500
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Open",
                            tint = Ink400,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Auth Switch buttons if Guest
            if (session == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToRegister,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Register", color = Forest600, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onNavigateToLogin,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Forest500),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sign In", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
