package com.example.ui.auth

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.UserRole
import com.example.ui.components.ZandoOutlinedTextField

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
    var selectedRole by remember { mutableStateOf(UserRole.CUSTOMER) }
    var gender by remember { mutableStateOf("Male") }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var selectedCity by remember { mutableStateOf("Phnom Penh") }
    var isCityDropdownExpanded by remember { mutableStateOf(false) }

    var storeName by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf("Motorbike (125cc)") }

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
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Back Arrow
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF111111)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Header Title
            Text(
                text = "Create an account",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111111)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Access your Bag & Orders on any of your devices",
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Account Type / Role Selection Tabs (Customer, Merchant, Driver, Admin)
            Text(
                text = "Account Type (Role)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111111)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                UserRole.values().forEach { role ->
                    val isSelected = selectedRole == role
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(
                                color = if (isSelected) Color.Black else Color(0xFFF2F2F2),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedRole = role },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = role.displayName.split(" ")[0],
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF444444)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Phone / Email Toggle Tabs
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .clickable { selectedTab = AuthTab.PHONE }
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Phone",
                        fontSize = 15.sp,
                        fontWeight = if (selectedTab == AuthTab.PHONE) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == AuthTab.PHONE) Color.Black else Color.Gray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(2.dp)
                            .background(if (selectedTab == AuthTab.PHONE) Color.Black else Color.Transparent)
                    )
                }

                Spacer(modifier = Modifier.width(28.dp))

                Column(
                    modifier = Modifier
                        .clickable { selectedTab = AuthTab.EMAIL }
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Email",
                        fontSize = 15.sp,
                        fontWeight = if (selectedTab == AuthTab.EMAIL) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == AuthTab.EMAIL) Color.Black else Color.Gray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(2.dp)
                            .background(if (selectedTab == AuthTab.EMAIL) Color.Black else Color.Transparent)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

            Spacer(modifier = Modifier.height(20.dp))

            // Gender Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Gender (Required)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF111111)
                )
                Spacer(modifier = Modifier.width(16.dp))

                RadioButton(
                    selected = gender == "Male",
                    onClick = { gender = "Male" },
                    colors = RadioButtonDefaults.colors(selectedColor = Color.Black)
                )
                Text(text = "Male", fontSize = 14.sp, color = Color(0xFF111111))

                Spacer(modifier = Modifier.width(16.dp))

                RadioButton(
                    selected = gender == "Female",
                    onClick = { gender = "Female" },
                    colors = RadioButtonDefaults.colors(selectedColor = Color.Black)
                )
                Text(text = "Female", fontSize = 14.sp, color = Color(0xFF111111))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // First Name & Last Name Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ZandoOutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = "First name",
                    placeholder = "Enter first name",
                    modifier = Modifier.weight(1f)
                )

                ZandoOutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = "Last name",
                    placeholder = "Enter last name",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contact Info
            if (selectedTab == AuthTab.PHONE) {
                ZandoOutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone",
                    placeholder = "Enter phone number",
                    isPhonePrefix = true,
                    phonePrefix = "+855",
                    keyboardType = KeyboardType.Phone
                )
            } else {
                ZandoOutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email Address",
                    placeholder = "Enter email address",
                    keyboardType = KeyboardType.Email
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            ZandoOutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "Enter password",
                isPassword = true
            )

            // Role-Specific Onboarding Details
            if (selectedRole == UserRole.MERCHANT) {
                Spacer(modifier = Modifier.height(16.dp))
                ZandoOutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = "Store / Restaurant Name",
                    placeholder = "e.g., Kimsour Khmer Cuisine"
                )
            } else if (selectedRole == UserRole.DRIVER) {
                Spacer(modifier = Modifier.height(16.dp))
                ZandoOutlinedTextField(
                    value = vehicleType,
                    onValueChange = { vehicleType = it },
                    label = "Vehicle Type & Model",
                    placeholder = "e.g., Motorbike (Honda Dream 125)"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // City / Province Dropdown
            Text(
                text = "City/province (Required)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF111111),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .border(1.dp, Color(0xFF222222), RoundedCornerShape(8.dp))
                        .clickable { isCityDropdownExpanded = true }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCity,
                            fontSize = 14.sp,
                            color = Color(0xFF111111)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select City",
                            tint = Color(0xFF333333)
                        )
                    }
                }

                DropdownMenu(
                    expanded = isCityDropdownExpanded,
                    onDismissRequest = { isCityDropdownExpanded = false }
                ) {
                    cities.forEach { city ->
                        DropdownMenuItem(
                            text = { Text(city) },
                            onClick = {
                                selectedCity = city
                                isCityDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Terms Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = termsAgreed,
                    onCheckedChange = { termsAgreed = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color.Black)
                )
                Text(
                    text = "I have read and agree to the ",
                    fontSize = 13.sp,
                    color = Color(0xFF333333)
                )
                Text(
                    text = "Term and Conditions",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3F51B5),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // REGISTER Primary Button
            Button(
                onClick = {
                    val fullPhone = if (selectedTab == AuthTab.PHONE) "+855$phone" else phone
                    viewModel.register(
                        firstName = firstName,
                        lastName = lastName,
                        phone = fullPhone,
                        email = email,
                        password = password,
                        cityProvince = selectedCity,
                        gender = gender,
                        selectedRole = selectedRole,
                        storeName = storeName,
                        vehicleType = vehicleType
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp),
                enabled = !uiState.isLoading && termsAgreed
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "REGISTER",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Already have an account link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 14.sp,
                    color = Color(0xFF555555)
                )
                Text(
                    text = "Login",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
