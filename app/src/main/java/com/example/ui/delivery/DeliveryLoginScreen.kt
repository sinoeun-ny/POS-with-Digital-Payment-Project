package com.example.ui.delivery

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricMoped
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DriverProfileEntity
import com.example.security.UserRole
import com.example.ui.auth.AuthTab
import com.example.ui.auth.AuthViewModel
import com.example.ui.components.ZandoOutlinedTextField
import com.example.ui.theme.*

@Composable
fun DeliveryLoginScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRegisterDriver: () -> Unit,
    onNavigateToStandardLogin: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf(AuthTab.EMAIL) }
    var phoneNumber by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var readyForDispatch by remember { mutableStateOf(true) }

    // Live Vehicle & License Preview state
    var driverProfilePreview by remember { mutableStateOf<DriverProfileEntity?>(null) }
    var isCheckingProfile by remember { mutableStateOf(false) }

    // Trigger lookup when email/phone changes
    LaunchedEffect(emailAddress, phoneNumber, selectedTab) {
        val currentIdentifier = if (selectedTab == AuthTab.PHONE) "+855$phoneNumber" else emailAddress
        if (currentIdentifier.isNotBlank()) {
            isCheckingProfile = true
            driverProfilePreview = viewModel.getDriverProfile(currentIdentifier)
            isCheckingProfile = false
        }
    }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
            // Direct redirect to Driver Console!
            onLoginSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Cream50
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Cream50)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Bar with Back and Dispatch Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ink950
                    )
                }

                Surface(
                    color = Mint100,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Forest400.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Forest600)
                        )
                        Text(
                            text = "DISPATCH ONLINE",
                            color = Forest800,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hero Brand Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Forest900, Forest700)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                                contentDescription = "Delivery Partner",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Delivery Partner Portal",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Courier Fleet Sign-In • Instant Dispatch Ready",
                                fontSize = 12.sp,
                                color = Mint100.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =========================================================
            // 🛵 VEHICLE & LICENSE INFO PREVIEW (Feature Highlight)
            // =========================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Forest200)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = Forest600,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Rider & Vehicle Telemetry",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Ink950
                            )
                        }

                        Surface(
                            color = Forest100,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "ACTIVE ONBOARDED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Forest800,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Sage200)

                    // Vehicle & License details grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.3f)) {
                            Text(
                                text = "ASSIGNED VEHICLE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink400,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ElectricMoped,
                                    contentDescription = null,
                                    tint = Forest700,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = driverProfilePreview?.vehicleType ?: "Honda Dream 125 (Motorbike)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink950
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LICENSE PLATE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink400,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                color = Cream200,
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Forest500.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = driverProfilePreview?.licensePlate ?: "PP-1AB-9988",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Ink950,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Rating & Deliveries stats row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Mint50, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = RatingGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${driverProfilePreview?.rating ?: 4.9} ★ Courier Rating",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink800
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = Forest600,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${driverProfilePreview?.totalDeliveries ?: 142} trips",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Forest800
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Telegram Dispatch Notice
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val telegramIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://t.me/xoradelivery_bot")
                        )
                        context.startActivity(telegramIntent)
                    },
                color = Forest50,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Forest300)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Forest500, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Telegram",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Couriers Operate via Telegram",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Forest900
                        )
                        Text(
                            text = "Accept orders & live delivery at @xoradelivery_bot",
                            fontSize = 11.sp,
                            color = Ink600
                        )
                    }
                    Text(
                        text = "Open →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Forest700
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phone / Email Toggle Tabs
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .clickable { selectedTab = AuthTab.EMAIL }
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Rider Email",
                        fontSize = 15.sp,
                        fontWeight = if (selectedTab == AuthTab.EMAIL) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == AuthTab.EMAIL) Forest700 else Ink400
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(3.dp)
                            .background(if (selectedTab == AuthTab.EMAIL) Forest600 else Color.Transparent)
                    )
                }

                Spacer(modifier = Modifier.width(28.dp))

                Column(
                    modifier = Modifier
                        .clickable { selectedTab = AuthTab.PHONE }
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Phone Number",
                        fontSize = 15.sp,
                        fontWeight = if (selectedTab == AuthTab.PHONE) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == AuthTab.PHONE) Forest700 else Ink400
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(3.dp)
                            .background(if (selectedTab == AuthTab.PHONE) Forest600 else Color.Transparent)
                    )
                }
            }

            HorizontalDivider(color = Sage200, thickness = 1.dp)

            Spacer(modifier = Modifier.height(20.dp))

            // Input Fields
            if (selectedTab == AuthTab.PHONE) {
                ZandoOutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = "Registered Courier Phone",
                    placeholder = "Enter 8-9 digits",
                    isPhonePrefix = true,
                    phonePrefix = "+855",
                    keyboardType = KeyboardType.Phone
                )
            } else {
                ZandoOutlinedTextField(
                    value = emailAddress,
                    onValueChange = { emailAddress = it },
                    label = "Rider Account Email",
                    placeholder = "e.g., rider@delivery.com",
                    isPhonePrefix = false,
                    keyboardType = KeyboardType.Email
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            ZandoOutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = "Security Password / PIN",
                placeholder = "Enter your secret password",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Shift status toggle checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { readyForDispatch = !readyForDispatch },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = readyForDispatch,
                    onCheckedChange = { readyForDispatch = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Forest600,
                        uncheckedColor = Ink400
                    )
                )
                Text(
                    text = "Automatically set status to 'Available for Pickup' upon sign in",
                    fontSize = 12.sp,
                    color = Ink700,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Primary LOGIN & START SHIFT Button
            Button(
                onClick = {
                    val identifier = if (selectedTab == AuthTab.PHONE) "+855$phoneNumber" else emailAddress
                    viewModel.login(identifier, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Forest600,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "START SHIFT & OPEN CONSOLE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Register as Delivery Rider link
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToRegisterDriver() },
                color = Color.White,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Sage200)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Want to drive with Xora?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink950
                        )
                        Text(
                            text = "Register as a courier partner & start earning",
                            fontSize = 11.sp,
                            color = Ink500
                        )
                    }
                    Text(
                        text = "Sign Up →",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Forest700
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Switch back to customer / merchant login
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Customer or Restaurant? ",
                    fontSize = 13.sp,
                    color = Ink500
                )
                Text(
                    text = "Standard Login",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink950,
                    modifier = Modifier.clickable { onNavigateToStandardLogin() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
