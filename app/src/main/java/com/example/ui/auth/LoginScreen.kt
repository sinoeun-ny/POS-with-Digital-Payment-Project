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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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
import com.example.ui.components.ZandoOutlinedTextField
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onNavigateToDeliveryLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(AuthTab.EMAIL) }
    var phoneNumber by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
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

            Spacer(modifier = Modifier.height(24.dp))

            // Brand / Header Title
            Text(
                text = "Welcome back 👋",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Ink950,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sign in to access your favorite restaurants, track real-time deliveries, and manage your account.",
                fontSize = 14.sp,
                color = Ink600,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Main Login Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Modern Segmented Toggle Tabs
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Cream100,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
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
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Input Fields
                    if (selectedTab == AuthTab.PHONE) {
                        ZandoOutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = "Mobile Number",
                            placeholder = "12 345 678",
                            isPhonePrefix = true,
                            phonePrefix = "+855",
                            keyboardType = KeyboardType.Phone
                        )
                    } else {
                        ZandoOutlinedTextField(
                            value = emailAddress,
                            onValueChange = { emailAddress = it },
                            label = "Email Address",
                            placeholder = "name@example.com",
                            isPhonePrefix = false,
                            keyboardType = KeyboardType.Email
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ZandoOutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        placeholder = "Enter your password",
                        isPassword = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Forgot Password
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Forgot password?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Forest700,
                            modifier = Modifier
                                .clickable {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Password reset link will be sent to your registered contact.")
                                    }
                                }
                                .padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary LOGIN Button
                    Button(
                        onClick = {
                            val identifier = if (selectedTab == AuthTab.PHONE) "+855$phoneNumber" else emailAddress
                            if (identifier.isBlank() || password.isBlank()) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Please enter both your identifier and password.")
                                }
                            } else {
                                viewModel.login(identifier, password)
                            }
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
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Sign In",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1-Tap Quick Fill Demo Account
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedTab = AuthTab.EMAIL
                                emailAddress = "customer@example.com"
                                password = "password123"
                            },
                        color = Forest50,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Forest200)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Forest600,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "1-Tap Fill Demo Account (customer@example.com)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Forest800
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // OR Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Sage200)
                Text(
                    text = "OR",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink400,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Sage200)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Continue with Google Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clickable {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Google Sign-In is coming soon. Please sign in with email or phone.")
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Sage300)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "G",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color(0xFF4285F4)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Continue with Google",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink950
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Register Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    fontSize = 14.sp,
                    color = Ink600
                )
                Text(
                    text = "Create Account",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Forest600,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
