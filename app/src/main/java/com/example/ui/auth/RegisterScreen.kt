package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.UserRole
import com.example.ui.components.ZandoOutlinedTextField
import com.example.ui.theme.*

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf(AuthTab.PHONE) }
    var gender by remember { mutableStateOf("Male") }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var selectedCity by remember { mutableStateOf("Phnom Penh") }
    var isCityDropdownExpanded by remember { mutableStateOf(false) }

    var termsAgreed by remember { mutableStateOf(true) }

    val cities = listOf("Phnom Penh", "Siem Reap", "Battambang", "Sihanoukville", "Kampot", "Kandal")

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
            onRegisterSuccess()
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
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Top Bar Back Arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Sage200, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ink950,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Header Title
            Text(
                text = "Create Account 🎉",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Ink950,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Join FoodEats to order gourmet meals, save multiple delivery addresses, and track orders live.",
                fontSize = 14.sp,
                color = Ink600,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Registration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Modern Segmented Toggle Tabs (Phone / Email)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Cream100,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Phone Tab Pill
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedTab = AuthTab.PHONE },
                                color = if (selectedTab == AuthTab.PHONE) Forest500 else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = if (selectedTab == AuthTab.PHONE) Color.White else Ink600,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Mobile Phone",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTab == AuthTab.PHONE) Color.White else Ink600
                                    )
                                }
                            }

                            // Email Tab Pill
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedTab = AuthTab.EMAIL },
                                color = if (selectedTab == AuthTab.EMAIL) Forest500 else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = if (selectedTab == AuthTab.EMAIL) Color.White else Ink600,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Email",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTab == AuthTab.EMAIL) Color.White else Ink600
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // First Name & Last Name Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ZandoOutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = "First Name",
                            placeholder = "Sok",
                            modifier = Modifier.weight(1f)
                        )

                        ZandoOutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = "Last Name",
                            placeholder = "Chan",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Contact Info
                    if (selectedTab == AuthTab.PHONE) {
                        ZandoOutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = "Mobile Number",
                            placeholder = "12 345 678",
                            isPhonePrefix = true,
                            phonePrefix = "+855",
                            keyboardType = KeyboardType.Phone
                        )
                    } else {
                        ZandoOutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email Address",
                            placeholder = "name@example.com",
                            isPhonePrefix = false,
                            keyboardType = KeyboardType.Email
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password
                    ZandoOutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        placeholder = "Create a secure password (min 6 chars)",
                        isPassword = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gender Selector
                    Text(
                        text = "Gender",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink700
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Male", "Female").forEach { g ->
                            val isSel = gender == g
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { gender = g },
                                color = if (isSel) Forest100 else Sage50,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSel) Forest500 else Sage200
                                )
                            ) {
                                Text(
                                    text = if (isSel) "✓ $g" else g,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) Forest800 else Ink700,
                                    modifier = Modifier
                                        .padding(vertical = 10.dp)
                                        .wrapContentWidth(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // City / Province Dropdown
                    Text(
                        text = "City / Delivery Zone",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink700
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { isCityDropdownExpanded = true },
                            color = Sage50,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Sage200)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedCity,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Ink950
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select City",
                                    tint = Ink600
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isCityDropdownExpanded,
                            onDismissRequest = { isCityDropdownExpanded = false }
                        ) {
                            cities.forEach { city ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = city,
                                            fontWeight = if (selectedCity == city) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedCity == city) Forest700 else Ink950
                                        )
                                    },
                                    onClick = {
                                        selectedCity = city
                                        isCityDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Terms Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = termsAgreed,
                            onCheckedChange = { termsAgreed = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Forest500,
                                uncheckedColor = Sage400
                            )
                        )
                        Text(
                            text = "I agree to FoodEats Terms of Service & Privacy Policy",
                            fontSize = 12.sp,
                            color = Ink700
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // REGISTER Primary Button
                    Button(
                        onClick = {
                            val fullPhone = if (selectedTab == AuthTab.PHONE) "+855$phone" else phone
                            viewModel.register(
                                firstName = firstName.trim(),
                                lastName = lastName.trim(),
                                phone = fullPhone.trim(),
                                email = email.trim(),
                                password = password,
                                cityProvince = selectedCity,
                                gender = gender,
                                selectedRole = UserRole.CUSTOMER
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Forest500,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        enabled = !uiState.isLoading && termsAgreed && (firstName.isNotBlank() || lastName.isNotBlank())
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Create Account",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Already have an account link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 14.sp,
                    color = Ink600
                )
                Text(
                    text = "Sign In",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Forest600,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
