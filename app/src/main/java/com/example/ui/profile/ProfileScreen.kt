package com.example.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.example.ui.components.ZandoOutlinedTextField
import androidx.compose.ui.graphics.vector.ImageVector
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



    // Dialog Visibility State
    var showSignOutConfirmDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showVouchersDialog by remember { mutableStateOf(false) }
    var showFavoritesDialog by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }

    // Developer / Diagnostics Section Collapsible
    var showDevDiagnostics by remember { mutableStateOf(false) }
    var serverUrlInput by remember { mutableStateOf(ApiClient.getBaseUrl()) }
    var connectionTestResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }

    // Form inputs for editing profile
    var editFullName by remember(currentUser) { mutableStateOf(currentUser?.fullName ?: "") }
    var editPhone by remember(currentUser) { mutableStateOf(currentUser?.phone ?: "") }
    var editEmail by remember(currentUser) { mutableStateOf(currentUser?.email ?: "") }

    val defaultAddress = uiState.userAddresses.firstOrNull { it.isDefault } ?: uiState.userAddresses.firstOrNull()

    Scaffold(
        containerColor = Cream50,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Your account", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Ink950) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream50)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Forest800)
            ) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(Modifier.size(60.dp).clip(CircleShape).background(Forest600), contentAlignment = Alignment.Center) {
                            if (session != null) {
                                Text((currentUser?.fullName?.take(1) ?: "U").uppercase(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            } else {
                                Icon(Icons.Default.PersonOutline, null, tint = Color.White, modifier = Modifier.size(30.dp))
                            }
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(if (session != null) currentUser?.fullName ?: "Welcome back" else "Welcome to Xora", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(if (session != null) currentUser?.email?.takeIf { it.isNotBlank() } ?: currentUser?.phone.orEmpty() else "Good food. All in one place.", style = MaterialTheme.typography.bodyMedium, color = Forest100)
                        }
                    }
                    if (session == null) {
                        Text("Sign in to manage your details, save delivery addresses, and find your recent orders.", style = MaterialTheme.typography.bodyMedium, color = Forest100)
                        Button(onClick = onNavigateToLogin, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Forest800), shape = RoundedCornerShape(14.dp)) {
                            Text("Sign in", fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = onNavigateToRegister, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.textButtonColors(contentColor = Color.White)) { Text("Create an account") }
                    } else {
                        Text("${orders.size} orders · ${favoriteIds.size} favorite restaurants", style = MaterialTheme.typography.bodyMedium, color = Forest100)
                        OutlinedButton(
                            onClick = {
                                editFullName = currentUser?.fullName.orEmpty()
                                editPhone = currentUser?.phone.orEmpty()
                                editEmail = currentUser?.email.orEmpty()
                                showEditProfileDialog = true
                            },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Forest200), shape = RoundedCornerShape(14.dp)
                        ) { Text("Edit personal details") }
                    }
                }
            }

            ProfileSection("Made for you") {
                ProfileAction(Icons.Default.FavoriteBorder, "Favorite restaurants", "${favoriteIds.size} saved places to enjoy again") { showFavoritesDialog = true }
                HorizontalDivider(color = Sage100)
                ProfileAction(Icons.Default.LocalOffer, "Vouchers & offers", "${availableVouchers.size} available offers") { showVouchersDialog = true }
            }

            ProfileSection("Delivery") {
                ProfileAction(
                    Icons.Default.LocationOn, "Saved addresses",
                    if (session == null) "Sign in to save a delivery address" else defaultAddress?.let { "${it.streetAddress}, ${it.city}" } ?: "Add an address for an easier checkout",
                    onClick = if (session == null) onNavigateToLogin else onNavigateToAddresses
                )
                HorizontalDivider(color = Sage100)
                Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Icon(Icons.Default.Payments, null, tint = Forest700)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Cash on delivery", style = MaterialTheme.typography.titleSmall, color = Ink950)
                        Text("Pay your courier when your order arrives.", style = MaterialTheme.typography.bodyMedium, color = Ink600)
                    }
                }
            }

            ProfileSection("Help") {
                ProfileAction(Icons.Default.HelpOutline, "Common questions", "Delivery, payments, and your orders") { showFaqDialog = true }
            }

            if (session != null) {
                ProfileSection("Account & security") {
                    ProfileAction(Icons.Default.Lock, "Change password", "Keep your account secure") { showChangePasswordDialog = true }
                    HorizontalDivider(color = Sage100)
                    ProfileAction(Icons.AutoMirrored.Filled.Logout, "Sign out", "Your saved details will be here when you return") { showSignOutConfirmDialog = true }
                    HorizontalDivider(color = Sage100)
                    ProfileAction(Icons.Default.DeleteOutline, "Delete account", "Permanently remove your account", destructive = true) { showDeleteAccountDialog = true }
                }
            }
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
                            .clickable { showDevDiagnostics = !showDevDiagnostics }.heightIn(min = 48.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = Forest500, modifier = Modifier.size(18.dp))
                            Text("Connection settings", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink800)
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
                            fontSize = 13.sp,
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
                                        Text("Reset", fontSize = 13.sp, color = Forest600, fontWeight = FontWeight.Bold)
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
                                colors = ButtonDefaults.buttonColors(containerColor = Forest700),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.3f)
                            ) {
                                if (isTestingConnection) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Test connection", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                                    text = message.replace("FoodEats", "Xora"),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (success) Forest800 else Rust900,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

        }
    }

    // MODAL DIALOGS
    // =========================================================================

    // 0. Sign Out Confirmation Dialog
    if (showSignOutConfirmDialog) {
        AlertDialog(
            containerColor = Color.White,
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
                    text = "Are you sure you want to sign out? Your saved addresses, orders, and vouchers will be here when you sign in again.",
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
            containerColor = Color.White,
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Personal Information", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink950) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Update your contact details for delivery couriers:", fontSize = 12.sp, color = Ink500)
                    ZandoOutlinedTextField(
                        value = editFullName,
                        onValueChange = { editFullName = it },
                        label = "Full name",
                        placeholder = "Your name",
                        enabled = !uiState.isLoading
                    )
                    ZandoOutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = "Phone number",
                        placeholder = "Your contact number",
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                        enabled = !uiState.isLoading
                    )
                    if (editEmail.isNotBlank()) Text("Sign-in email: $editEmail", style = MaterialTheme.typography.bodyMedium, color = Ink600)
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
                    enabled = !uiState.isLoading && editFullName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Forest700),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (uiState.isLoading) "Saving…" else "Save changes", fontWeight = FontWeight.Bold)
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
            containerColor = Color.White,
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
                        Text("Your saved restaurants:", fontSize = 12.sp, color = Ink500)
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
                                        Text("${restaurant.cuisine} • ⭐ ${restaurant.rating} • ⚡ ${restaurant.deliveryTime}", fontSize = 13.sp, color = Forest700)
                                        Text(restaurant.address, fontSize = 12.sp, color = Ink500, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                Button(onClick = { showFavoritesDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Forest700), shape = RoundedCornerShape(10.dp)) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 3. Vouchers Dialog (Dynamic vouchers with one-tap copy)
    if (showVouchersDialog) {
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

        AlertDialog(
            containerColor = Color.White,
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
                                                fontSize = 12.sp,
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
                                        Text("Copy Code", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Forest700)
                                    }
                                }

                                Text(voucher.title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Ink950)
                                Text(voucher.description, fontSize = 12.sp, color = Ink600)
                                Text("Min spend $${String.format(java.util.Locale.US, "%.2f", voucher.minSpend)} • Expires: ${voucher.expiryDate}", fontSize = 12.sp, color = Ink500)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showVouchersDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Forest700), shape = RoundedCornerShape(10.dp)) {
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
            containerColor = Color.White,
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Password", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink950) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ZandoOutlinedTextField(value = currentPw, onValueChange = { currentPw = it }, label = "Current password", placeholder = "Enter your current password", isPassword = true, enabled = !uiState.isLoading)
                    ZandoOutlinedTextField(value = newPw, onValueChange = { newPw = it }, label = "New password", placeholder = "At least 6 characters", isPassword = true, enabled = !uiState.isLoading)
                    ZandoOutlinedTextField(value = confirmPw, onValueChange = { confirmPw = it }, label = "Confirm new password", placeholder = "Re-enter your new password", isPassword = true, imeAction = androidx.compose.ui.text.input.ImeAction.Done, enabled = !uiState.isLoading)
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
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Forest700),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (uiState.isLoading) "Updating…" else "Update password", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) { Text("Cancel", color = Ink600) }
            }
        )
    }

    // 6. Delete Account Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            containerColor = Color.White,
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
                            Toast.makeText(context, "Your account has been deleted.", Toast.LENGTH_LONG).show()
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

    // 10. FAQs Dialog
    if (showFaqDialog) {
        AlertDialog(
            containerColor = Color.White,
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
                        Text("• How does Cash on delivery work?", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Ink950)
                        Text("You pay the courier in cash upon arrival. Pay when your order arrives.", fontSize = 13.sp, color = Ink600)
                    }
                    Column {
                        Text("• Can I cancel an order once placed?", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Ink950)
                        Text("You can cancel while the order is pending. Once the merchant begins cooking or the courier is dispatched, orders are locked.", fontSize = 13.sp, color = Ink600)
                    }
                    Column {
                        Text("• How do I change delivery address?", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Ink950)
                        Text("Open Saved addresses in your account to add or edit delivery locations.", fontSize = 13.sp, color = Ink600)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showFaqDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Forest700), shape = RoundedCornerShape(10.dp)) {
                    Text("Got It", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

}


@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Ink950)
        Card(
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Sage200)
        ) { Column(content = content) }
    }
}

@Composable
private fun ProfileAction(icon: ImageVector, title: String, subtitle: String, destructive: Boolean = false, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).heightIn(min = 76.dp).padding(18.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null, tint = if (destructive) Rust900 else Forest700, modifier = Modifier.size(24.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = if (destructive) Rust900 else Ink950)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Ink600)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = Ink400, modifier = Modifier.size(14.dp))
    }
}
