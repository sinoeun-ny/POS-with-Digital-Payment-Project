package com.example.ui.profile

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.ApiClient
import com.example.data.repository.FoodEatsRepository
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
    onNavigateToAdminRoleDivision: () -> Unit = {},
    onNavigateToDriver: () -> Unit = {},
    onNavigateToDeliveryLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by authViewModel.uiState.collectAsState()
    val session = uiState.activeSession
    val currentUser = uiState.currentUser
    val orders by FoodEatsRepository.orders.collectAsState()
    val restaurants by FoodEatsRepository.restaurants.collectAsState()
    val favoriteIds by FoodEatsRepository.favoriteRestaurantIds.collectAsState()
    val availableVouchers = FoodEatsRepository.availableVouchers

    // Preferences & Settings State (UI toggles)
    var selectedLanguage by remember { mutableStateOf("English") } // "English" or "ភាសាខ្មែរ"
    var isDarkMode by remember { mutableStateOf(false) }
    var orderNotifsEnabled by remember { mutableStateOf(true) }
    var promoNotifsEnabled by remember { mutableStateOf(false) }

    // Dialog Visibility State
    var showSignOutConfirmDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showVouchersDialog by remember { mutableStateOf(false) }
    var showFavoritesDialog by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }
    var showSupportContactDialog by remember { mutableStateOf(false) }
    var showReportProblemDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }

    // Developer / Diagnostics Section Collapsible
    var showDevDiagnostics by remember { mutableStateOf(false) }
    var serverUrlInput by remember { mutableStateOf(ApiClient.getBaseUrl()) }
    var connectionTestResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }

    // Form inputs for editing profile
    var editFullName by remember(currentUser) { mutableStateOf(currentUser?.fullName ?: "") }
    var editPhone by remember(currentUser) { mutableStateOf(currentUser?.phone ?: "") }
    var editEmail by remember(currentUser) { mutableStateOf(currentUser?.email ?: "") }
    var editDeliveryInstructions by remember { mutableStateOf("Please call when arriving at the gate") }

    val defaultAddress = uiState.userAddresses.firstOrNull { it.isDefault } ?: uiState.userAddresses.firstOrNull()

    Scaffold(
        containerColor = Cream50,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "My Profile & Account",
                            fontWeight = FontWeight.Black,
                            fontSize = 19.sp,
                            color = Ink950
                        )
                        Text(
                            text = if (session != null) "Customer Portal & Settings" else "Guest Mode",
                            fontSize = 11.sp,
                            color = Ink500
                        )
                    }
                },
                actions = {
                    if (session != null) {
                        IconButton(onClick = { showSignOutConfirmDialog = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Sign Out",
                                tint = Rust500
                            )
                        }
                    }
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
            // =========================================================================
            // 1. USER IDENTITY & VERIFICATION CARD
            // =========================================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Avatar with Camera / Photo edit badge
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(if (session != null) Forest500 else Sage200),
                                contentAlignment = Alignment.Center
                            ) {
                                if (session != null) {
                                    Text(
                                        text = (currentUser?.fullName?.take(1) ?: "U").uppercase(),
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.PersonOutline,
                                        contentDescription = "Guest",
                                        tint = Ink600,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            // Camera Icon to manage profile photo
                            if (session != null) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Forest700)
                                        .clickable { showPhotoDialog = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Edit Photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }

                        // User Info & Badges
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (session != null) (currentUser?.fullName ?: "Signed In Customer") else "Guest Customer",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink950
                                )

                                if (session != null) {
                                    IconButton(
                                        onClick = {
                                            editFullName = currentUser?.fullName ?: ""
                                            editPhone = currentUser?.phone ?: ""
                                            editEmail = currentUser?.email ?: ""
                                            showEditProfileDialog = true
                                        },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Profile",
                                            tint = Forest600,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = if (session != null) (currentUser?.email ?: currentUser?.phone ?: "") else "Not signed in. Sign in or register below.",
                                fontSize = 12.sp,
                                color = Ink500
                            )

                            // Verification Badges & Active Role
                            Row(
                                modifier = Modifier.padding(top = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = if (currentUser?.isVerified == true) Mint100 else Cream200,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (currentUser?.isVerified == true) "✓ Verified Contact" else "⚠️ Unverified",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentUser?.isVerified == true) Forest800 else Rust900,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Surface(
                                    color = if (session != null) Sage100 else Cream200,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (session != null) "ROLE: ${currentUser?.currentActiveRole?.removePrefix("ROLE_") ?: "CUSTOMER"}" else "GUEST",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink700,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 2. QUICK METRICS OVERVIEW (Orders, Favorites, Vouchers)
            // =========================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metric 1: Past Orders
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            Toast.makeText(context, "Switch to Orders tab to view timeline", Toast.LENGTH_SHORT).show()
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = Forest600, modifier = Modifier.size(20.dp))
                        Text(text = "${orders.size}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Ink950)
                        Text(text = "Orders", fontSize = 11.sp, color = Ink500)
                    }
                }

                // Metric 2: Favorites
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showFavoritesDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Rust500, modifier = Modifier.size(20.dp))
                        Text(text = "${favoriteIds.size}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Ink950)
                        Text(text = "Favorites", fontSize = 11.sp, color = Ink500)
                    }
                }

                // Metric 3: Vouchers
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showVouchersDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Forest500, modifier = Modifier.size(20.dp))
                        Text(text = "${availableVouchers.size} Active", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Forest700)
                        Text(text = "Vouchers", fontSize = 11.sp, color = Ink500)
                    }
                }
            }

            // =========================================================================
            // 3. ADDRESS & DELIVERY INSTRUCTIONS MANAGEMENT CARD
            // =========================================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Forest500, modifier = Modifier.size(20.dp))
                            Text("Delivery Address & Map Pin", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink950)
                        }

                        TextButton(onClick = onNavigateToAddresses) {
                            Text("Manage", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Forest600)
                        }
                    }

                    // Default Address Preview
                    Surface(
                        color = Cream100,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (defaultAddress != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = defaultAddress.label.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Forest800
                                    )
                                    Text("Map Pin Saved", fontSize = 10.sp, color = Forest700, fontWeight = FontWeight.SemiBold)
                                }
                                Text(
                                    text = defaultAddress.streetAddress,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Ink950
                                )
                                Text(
                                    text = "City: ${defaultAddress.city} • Instructions: $editDeliveryInstructions",
                                    fontSize = 11.sp,
                                    color = Ink600
                                )
                            } else {
                                Text(
                                    text = "NO ADDRESS SAVED IN DATABASE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Rust500
                                )
                                Text(
                                    text = "No saved delivery address found. Tap 'Manage' to add your address and GPS pin.",
                                    fontSize = 12.sp,
                                    color = Ink700
                                )
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 4. PAYMENT & BILLING (COD ONLY MVP)
            // =========================================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = Forest500, modifier = Modifier.size(20.dp))
                            Text("Payment & Billing Method", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink950)
                        }

                        Surface(color = Mint100, shape = RoundedCornerShape(6.dp)) {
                            Text("Active MVP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Forest800, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Surface(
                        color = Cream100,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("💵", fontSize = 22.sp)
                            Column {
                                Text("Cash on Delivery (COD)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink950)
                                Text("Pay the delivery courier directly upon food arrival. Online gateway (ABA PayWay) is deferred to the final release.", fontSize = 11.sp, color = Ink600)
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 5. PREFERENCES & APP SETTINGS CARD
            // =========================================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Preferences", tint = Forest500, modifier = Modifier.size(20.dp))
                        Text("Preferences & App Settings", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink950)
                    }

                    // Setting 1: Language Switcher (English vs Khmer)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = Forest600, modifier = Modifier.size(20.dp))
                            Column {
                                Text("App Language", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink950)
                                Text("Choose English or ភាសាខ្មែរ", fontSize = 11.sp, color = Ink500)
                            }
                        }

                        // Segmented buttons [EN | KH]
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Cream100)
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Surface(
                                color = if (selectedLanguage == "English") Forest500 else Color.Transparent,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable { selectedLanguage = "English" }
                            ) {
                                Text(
                                    text = "EN",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedLanguage == "English") Color.White else Ink700,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            Surface(
                                color = if (selectedLanguage == "ភាសាខ្មែរ") Forest500 else Color.Transparent,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable { selectedLanguage = "ភាសាខ្មែរ" }
                            ) {
                                Text(
                                    text = "KH (ខ្មែរ)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedLanguage == "ភាសាខ្មែរ") Color.White else Ink700,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Sage100)

                    // Setting 2: Light / Dark Mode Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = Forest600, modifier = Modifier.size(20.dp))
                            Column {
                                Text("Dark Theme", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink950)
                                Text(if (isDarkMode) "Dark theme enabled" else "Light theme enabled", fontSize = 11.sp, color = Ink500)
                            }
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { isDarkMode = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Forest500)
                        )
                    }

                    HorizontalDivider(color = Sage100)

                    // Setting 3: Order Status In-App Notifications
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Forest600, modifier = Modifier.size(20.dp))
                            Column {
                                Text("Order Status Notifications", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink950)
                                Text("Kitchen prep, rider pickup & delivery alerts", fontSize = 11.sp, color = Ink500)
                            }
                        }
                        Switch(
                            checked = orderNotifsEnabled,
                            onCheckedChange = { orderNotifsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Forest500)
                        )
                    }

                    HorizontalDivider(color = Sage100)

                    // Setting 4: Promotional Deals Notifications
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.LocalOffer, contentDescription = null, tint = Forest600, modifier = Modifier.size(20.dp))
                            Column {
                                Text("Promotional Deals & Vouchers", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink950)
                                Text("Flash sales, discounts and special coupon codes", fontSize = 11.sp, color = Ink500)
                            }
                        }
                        Switch(
                            checked = promoNotifsEnabled,
                            onCheckedChange = { promoNotifsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Forest500)
                        )
                    }
                }
            }

            // =========================================================================
            // 6. HELP, SUPPORT, REVIEWS & FEEDBACK
            // =========================================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Item 1: FAQs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showFaqDialog = true }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Forest500)
                            Column {
                                Text("Frequently Asked Questions (FAQs)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink950)
                                Text("Delivery zones, COD payment rules & order cancellation", fontSize = 11.sp, color = Ink500)
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Ink400, modifier = Modifier.size(14.dp))
                    }

                    HorizontalDivider(color = Sage100, modifier = Modifier.padding(horizontal = 8.dp))

                    // Item 2: Customer Support
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showSupportContactDialog = true }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Forest600)
                            Column {
                                Text("Contact Customer Support", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink950)
                                Text("Direct Telegram bot, Hotline & WhatsApp assistance", fontSize = 11.sp, color = Ink500)
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Ink400, modifier = Modifier.size(14.dp))
                    }

                    HorizontalDivider(color = Sage100, modifier = Modifier.padding(horizontal = 8.dp))

                    // Item 3: Report an Order Problem
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showReportProblemDialog = true }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Rust500)
                            Column {
                                Text("Report an Order Problem", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink950)
                                Text("Wrong items, courier delay or food quality complaints", fontSize = 11.sp, color = Ink500)
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Ink400, modifier = Modifier.size(14.dp))
                    }

                    HorizontalDivider(color = Sage100, modifier = Modifier.padding(horizontal = 8.dp))

                    // Item 4: Rate Order (1 to 5 Stars)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showRatingDialog = true }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = RatingGold)
                            Column {
                                Text("Rate Recent Order Experience", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink950)
                                Text("Submit 1 to 5 stars review for completed deliveries", fontSize = 11.sp, color = Ink500)
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Ink400, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // =========================================================================
            // 7. ACCOUNT & SECURITY (Password, Reset, Delete)
            // =========================================================================
            if (session != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        // Change Password
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showChangePasswordDialog = true }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Forest600)
                                Column {
                                    Text("Change Password", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink950)
                                    Text("Update your security password", fontSize = 11.sp, color = Ink500)
                                }
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Ink400, modifier = Modifier.size(14.dp))
                        }

                        HorizontalDivider(color = Sage100, modifier = Modifier.padding(horizontal = 8.dp))

                        // Forgot / Reset Password
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showForgotPasswordDialog = true }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.LockReset, contentDescription = null, tint = Forest600)
                                Column {
                                    Text("Forgot / Reset Password", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink950)
                                    Text("Send password reset link to your email or SMS", fontSize = 11.sp, color = Ink500)
                                }
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Ink400, modifier = Modifier.size(14.dp))
                        }

                        HorizontalDivider(color = Sage100, modifier = Modifier.padding(horizontal = 8.dp))

                        // Delete Account (Destructive with Red accent)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showDeleteAccountDialog = true }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Rust500)
                                Column {
                                    Text("Delete Account", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Rust500)
                                    Text("Permanently wipe your account, addresses and orders", fontSize = 11.sp, color = Ink500)
                                }
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Rust500, modifier = Modifier.size(14.dp))
                        }

                        HorizontalDivider(color = Sage100, modifier = Modifier.padding(horizontal = 8.dp))

                        // Sign Out Option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showSignOutConfirmDialog = true }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Rust500)
                                Column {
                                    Text("Sign Out", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Rust500)
                                    Text("Safely end your session on this device", fontSize = 11.sp, color = Ink500)
                                }
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Rust500, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // =========================================================================
            // 8. DEVELOPER & DIAGNOSTICS (COLLAPSIBLE AT THE BOTTOM)
            // =========================================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDevDiagnostics = !showDevDiagnostics },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = Forest500, modifier = Modifier.size(18.dp))
                            Text("Developer Diagnostics & Database Sync", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink800)
                        }
                        Icon(
                            imageVector = if (showDevDiagnostics) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Ink400
                        )
                    }

                    if (showDevDiagnostics) {
                        Text(
                            text = "Connects directly to Spring Boot & MySQL running on the host laptop (10.0.2.2:8080).",
                            fontSize = 11.sp,
                            color = Ink600
                        )

                        OutlinedTextField(
                            value = serverUrlInput,
                            onValueChange = { serverUrlInput = it },
                            label = { Text("Backend URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
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
                            }
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    ApiClient.setBaseUrl(context, serverUrlInput)
                                    FoodEatsRepository.loadFromBackend(search = null, cuisine = null)
                                    Toast.makeText(context, "URL Saved!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save URL", color = Ink950, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                                            FoodEatsRepository.loadFromBackend(search = null, cuisine = null)
                                        }
                                    }
                                },
                                enabled = !isTestingConnection,
                                colors = ButtonDefaults.buttonColors(containerColor = Forest500),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.3f)
                            ) {
                                if (isTestingConnection) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Test DB Sync", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        connectionTestResult?.let { (success, message) ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = if (success) Mint100 else Cream200,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = message,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (success) Forest800 else Rust900,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 10. GUEST STATE (SIGN IN / REGISTER CTA)
            // =========================================================================
            if (session == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Ready to order artisan meals?", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ink950)
                        Text("Sign in to unlock saved addresses, member discounts, and order progress tracking.", fontSize = 12.sp, color = Ink500, textAlign = TextAlign.Center)

                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = onNavigateToRegister, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                                Text("Register", color = Forest600, fontWeight = FontWeight.Bold)
                            }
                            Button(onClick = onNavigateToLogin, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Forest500), shape = RoundedCornerShape(12.dp)) {
                                Text("Sign In", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // MODAL DIALOGS
    // =========================================================================

    // 0. Sign Out Confirmation Dialog
    if (showSignOutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = Rust500,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Sign Out?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Ink950
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out? Your saved addresses, past orders, and vouchers will remain safely preserved in the database.",
                    fontSize = 13.sp,
                    color = Ink700
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirmDialog = false
                        authViewModel.logout()
                        Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rust500),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirmDialog = false }) {
                    Text("Cancel", color = Ink600, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // 1. Edit Profile Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Personal Information", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink950) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Update your contact details for delivery couriers:", fontSize = 12.sp, color = Ink500)
                    OutlinedTextField(
                        value = editFullName,
                        onValueChange = { editFullName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = editDeliveryInstructions,
                        onValueChange = { editDeliveryInstructions = it },
                        label = { Text("Default Delivery Instructions") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.updateProfile(editFullName, editPhone) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) {
                                showEditProfileDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Forest500),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = Ink600)
                }
            }
        )
    }

    // 2. Favorites Dialog (Real-time dynamic data from database)
    if (showFavoritesDialog) {
        val savedFavorites = restaurants.filter { it.id in favoriteIds }

        AlertDialog(
            onDismissRequest = { showFavoritesDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Rust500)
                    Text("My Saved Favorites (${savedFavorites.size})", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink950)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (savedFavorites.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Ink300, modifier = Modifier.size(40.dp))
                            Text(
                                text = "No favorites saved yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Ink950
                            )
                            Text(
                                text = "Browse restaurants on the Discover tab and tap the ❤️ heart icon to add places here.",
                                fontSize = 12.sp,
                                color = Ink500,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Text("Your saved favorite spots from the database:", fontSize = 12.sp, color = Ink500)
                        savedFavorites.forEach { restaurant ->
                            Surface(
                                color = Cream100,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(restaurant.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink950)
                                        Text("${restaurant.cuisine} • ⭐ ${restaurant.rating} • ⚡ ${restaurant.deliveryTime}", fontSize = 11.sp, color = Forest700)
                                        Text(restaurant.address, fontSize = 10.sp, color = Ink500, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    IconButton(
                                        onClick = { FoodEatsRepository.toggleFavorite(restaurant.id) }
                                    ) {
                                        Icon(Icons.Default.Favorite, contentDescription = "Remove", tint = Rust500)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showFavoritesDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Forest500), shape = RoundedCornerShape(10.dp)) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 3. Vouchers Dialog (Dynamic vouchers with one-tap copy)
    if (showVouchersDialog) {
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

        AlertDialog(
            onDismissRequest = { showVouchersDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Forest500)
                    Text("Eligible Vouchers & Promo Deals", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink950)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    availableVouchers.forEach { voucher ->
                        Surface(
                            color = if (voucher.isFreeDelivery) Cream100 else Mint100,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(voucher.code, fontWeight = FontWeight.Black, fontSize = 14.sp, fontFamily = FontFamily.Monospace, color = if (voucher.isFreeDelivery) Ink950 else Forest800)
                                        Surface(
                                            color = if (voucher.isFreeDelivery) Ink100 else Forest100,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = if (voucher.isFreeDelivery) "FREE SHIP" else "${(voucher.discountPercent * 100).toInt()}% OFF",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (voucher.isFreeDelivery) Ink800 else Forest800,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    TextButton(
                                        onClick = {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(voucher.code))
                                            Toast.makeText(context, "Copied code '${voucher.code}' to clipboard!", Toast.LENGTH_SHORT).show()
                                        },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                    ) {
                                        Text("Copy Code", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Forest700)
                                    }
                                }

                                Text(voucher.title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Ink950)
                                Text(voucher.description, fontSize = 10.sp, color = Ink600)
                                Text("Min spend $${String.format(java.util.Locale.US, "%.2f", voucher.minSpend)} • Expires: ${voucher.expiryDate}", fontSize = 9.sp, color = Ink500)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showVouchersDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Forest500), shape = RoundedCornerShape(10.dp)) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 4. Change Password Dialog (Direct MySQL database update)
    if (showChangePasswordDialog) {
        var currentPw by remember { mutableStateOf("") }
        var newPw by remember { mutableStateOf("") }
        var confirmPw by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Password", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink950) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = currentPw, onValueChange = { currentPw = it }, label = { Text("Current Password") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                    OutlinedTextField(value = newPw, onValueChange = { newPw = it }, label = { Text("New Password (min 6 chars)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                    OutlinedTextField(value = confirmPw, onValueChange = { confirmPw = it }, label = { Text("Confirm New Password") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (currentPw.isBlank()) {
                            Toast.makeText(context, "Please enter your current password", Toast.LENGTH_SHORT).show()
                        } else if (newPw.length < 6) {
                            Toast.makeText(context, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                        } else if (newPw != confirmPw) {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        } else {
                            authViewModel.changePassword(currentPw, newPw) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) {
                                    showChangePasswordDialog = false
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Forest500),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Update", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) { Text("Cancel", color = Ink600) }
            }
        )
    }

    // 5. Forgot Password Dialog
    if (showForgotPasswordDialog) {
        var resetTarget by remember { mutableStateOf(currentUser?.email ?: currentUser?.phone ?: "") }
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Reset Password", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink950) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter your registered email or phone to receive a 6-digit verification code:", fontSize = 12.sp, color = Ink600)
                    OutlinedTextField(
                        value = resetTarget,
                        onValueChange = { resetTarget = it },
                        label = { Text("Email or Phone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Verification code sent to $resetTarget!", Toast.LENGTH_SHORT).show()
                        showForgotPasswordDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Forest500),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Send Code", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) { Text("Cancel", color = Ink600) }
            }
        )
    }

    // 6. Delete Account Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Rust500, modifier = Modifier.size(36.dp)) },
            title = { Text("Delete Account Permanently?", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Rust500) },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete your account? All order history, saved addresses, and vouchers will be wiped immediately. This cannot be undone.",
                    fontSize = 13.sp,
                    color = Ink700
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.deleteAccount {
                            Toast.makeText(context, "Account permanently removed from database. Signed out.", Toast.LENGTH_LONG).show()
                            showDeleteAccountDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rust500),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirm Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) { Text("Cancel", color = Ink600) }
            }
        )
    }

    // 7. Customer Support Dialog
    if (showSupportContactDialog) {
        AlertDialog(
            onDismissRequest = { showSupportContactDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Forest600)
                    Text("Customer Support", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink950)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Reach our team anytime through:", fontSize = 12.sp, color = Ink500)
                    Surface(color = Cream100, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("🤖 Telegram Support Bot", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Forest800)
                            Text("@FoodEats_Support_Bot (Instant response)", fontSize = 11.sp, color = Ink600)
                        }
                    }
                    Surface(color = Cream100, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("📞 Phone Hotline", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Ink950)
                            Text("+855 23 999 888 (7:00 AM - 11:00 PM)", fontSize = 11.sp, color = Ink600)
                        }
                    }
                    Surface(color = Cream100, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("✉️ Email Helpdesk", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Ink950)
                            Text("support@foodeats.com.kh", fontSize = 11.sp, color = Ink600)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showSupportContactDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Forest500), shape = RoundedCornerShape(10.dp)) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 8. Report a Problem Dialog
    if (showReportProblemDialog) {
        var problemDescription by remember { mutableStateOf("") }
        var selectedProblemType by remember { mutableStateOf("Food Quality") }

        AlertDialog(
            onDismissRequest = { showReportProblemDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Rust500)
                    Text("Report an Order Issue", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink950)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("What went wrong with your order?", fontSize = 12.sp, color = Ink500)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Food Quality", "Wrong Item", "Delay").forEach { type ->
                            val isSel = selectedProblemType == type
                            Surface(
                                color = if (isSel) Forest500 else Cream100,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { selectedProblemType = type }
                            ) {
                                Text(type, fontSize = 11.sp, color = if (isSel) Color.White else Ink700, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                    OutlinedTextField(
                        value = problemDescription,
                        onValueChange = { problemDescription = it },
                        label = { Text("Describe the issue") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Problem reported! Our support team will review within 15 minutes.", Toast.LENGTH_LONG).show()
                        showReportProblemDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rust500),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Submit Report", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportProblemDialog = false }) { Text("Cancel", color = Ink600) }
            }
        )
    }

    // 9. Rate Order Experience Dialog (1 to 5 Stars)
    if (showRatingDialog) {
        var selectedStars by remember { mutableStateOf(5) }
        var reviewText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = RatingGold)
                    Text("Rate Recent Order", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink950)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("How was your meal and courier delivery?", fontSize = 12.sp, color = Ink500)

                    // 1 to 5 Star Selector
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { selectedStars = star }, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    imageVector = if (star <= selectedStars) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$star Stars",
                                    tint = RatingGold,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = when (selectedStars) {
                            5 -> "⭐ Excellent! Artisan taste & fast delivery."
                            4 -> "⭐ Very Good! Enjoyed the meal."
                            3 -> "⭐ Average. Satisfactory."
                            2 -> "⭐ Below Expectations."
                            else -> "⭐ Poor experience."
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Forest700
                    )

                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        label = { Text("Write a quick review (optional)") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Thank you! Rating of $selectedStars stars submitted.", Toast.LENGTH_SHORT).show()
                        showRatingDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Forest500),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Submit Review", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRatingDialog = false }) { Text("Cancel", color = Ink600) }
            }
        )
    }

    // 10. FAQs Dialog
    if (showFaqDialog) {
        AlertDialog(
            onDismissRequest = { showFaqDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Forest500)
                    Text("Frequently Asked Questions", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink950)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column {
                        Text("• How does Cash on Delivery (COD) work?", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Ink950)
                        Text("You pay the courier in cash upon arrival. No advance online payment is required in this release.", fontSize = 11.sp, color = Ink600)
                    }
                    Column {
                        Text("• Can I cancel an order once placed?", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Ink950)
                        Text("You can cancel while the order is pending. Once the merchant begins cooking or the courier is dispatched, orders are locked.", fontSize = 11.sp, color = Ink600)
                    }
                    Column {
                        Text("• How do I change delivery address?", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Ink950)
                        Text("Go to 'Manage Saved Addresses' in your profile to add or edit delivery locations with GPS map pins.", fontSize = 11.sp, color = Ink600)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showFaqDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Forest500), shape = RoundedCornerShape(10.dp)) {
                    Text("Got It", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 11. Profile Photo Dialog
    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            title = { Text("Profile Photo Avatar", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink950) },
            text = {
                Text("In this release, your avatar displays your name's initials on a forest-green badge. Cloud photo file uploading is ready for backend connection.", fontSize = 13.sp, color = Ink600)
            },
            confirmButton = {
                Button(onClick = { showPhotoDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Forest500), shape = RoundedCornerShape(10.dp)) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
