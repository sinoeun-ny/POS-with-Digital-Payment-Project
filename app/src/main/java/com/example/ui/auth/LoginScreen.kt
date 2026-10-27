package com.example.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.ZandoOutlinedTextField
import com.example.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onNavigateToDeliveryLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var selectedTab by rememberSaveable { mutableStateOf(AuthTab.EMAIL) }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var emailAddress by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var attemptedSubmit by remember { mutableStateOf(false) }
    val contact = if (selectedTab == AuthTab.PHONE) phoneNumber else emailAddress

    fun submit() {
        attemptedSubmit = true
        if (!uiState.isLoading && contact.isNotBlank() && password.isNotBlank()) {
            focusManager.clearFocus()
            val identifier = if (selectedTab == AuthTab.PHONE) "+855${phoneNumber.trim()}" else emailAddress.trim()
            viewModel.login(identifier, password)
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            viewModel.clearMessages()
            onLoginSuccess()
        }
    }

    AuthScreenLayout(onNavigateBack = onNavigateBack) {
        AuthHeader("Welcome back", "Your next great meal is a sign-in away.")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Sage200)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AuthContactTabs(selectedTab, !uiState.isLoading) {
                    selectedTab = it
                    attemptedSubmit = false
                    viewModel.clearMessages()
                }
                ZandoOutlinedTextField(
                    value = contact,
                    onValueChange = { if (selectedTab == AuthTab.PHONE) phoneNumber = it else emailAddress = it },
                    label = if (selectedTab == AuthTab.PHONE) "Phone number" else "Email address",
                    placeholder = if (selectedTab == AuthTab.PHONE) "12 345 678" else "name@example.com",
                    isPhonePrefix = selectedTab == AuthTab.PHONE,
                    keyboardType = if (selectedTab == AuthTab.PHONE) KeyboardType.Phone else KeyboardType.Email,
                    enabled = !uiState.isLoading,
                    isError = attemptedSubmit && contact.isBlank(),
                    supportingText = if (attemptedSubmit && contact.isBlank()) "Enter your ${if (selectedTab == AuthTab.PHONE) "phone number" else "email address"}." else null
                )
                ZandoOutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    placeholder = "Enter your password",
                    isPassword = true,
                    enabled = !uiState.isLoading,
                    isError = attemptedSubmit && password.isBlank(),
                    supportingText = if (attemptedSubmit && password.isBlank()) "Enter your password." else null,
                    imeAction = ImeAction.Done,
                    onDone = ::submit
                )
                uiState.errorMessage?.let { AuthErrorMessage(it) }
                Button(
                    onClick = ::submit,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Forest700, contentColor = Color.White, disabledContainerColor = Sage200, disabledContentColor = Ink600),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(if (uiState.isLoading) "Signing in…" else "Sign in", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = {
                        selectedTab = AuthTab.EMAIL
                        emailAddress = "customer@example.com"
                        password = "password123"
                        attemptedSubmit = false
                        viewModel.clearMessages()
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Forest700),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Fill demo account") }
                Text("Exploring the app? Fill the demo details, then tap Sign in.", style = MaterialTheme.typography.bodySmall, color = Ink600)
            }
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("New to Xora?", style = MaterialTheme.typography.bodyMedium, color = Ink600)
            TextButton(onClick = onNavigateToRegister, enabled = !uiState.isLoading, colors = ButtonDefaults.textButtonColors(contentColor = Forest700)) {
                Text("Create an account", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
internal fun AuthScreenLayout(onNavigateBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Cream50).imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.widthIn(max = 520.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedIconButton(onClick = onNavigateBack, modifier = Modifier.size(48.dp), shape = RoundedCornerShape(14.dp), colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = Ink950)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
            }
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.app_name), color = Forest700, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.widthIn(max = 520.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(24.dp), content = content)
    }
}

@Composable
internal fun AuthHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Ink950)
        Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = Ink600)
    }
}

@Composable
internal fun AuthContactTabs(selected: AuthTab, enabled: Boolean, onSelect: (AuthTab) -> Unit) {
    Surface(color = Cream100, shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(4.dp).selectableGroup(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(AuthTab.EMAIL, AuthTab.PHONE).forEach { tab ->
                Surface(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).selectable(
                        selected = selected == tab, enabled = enabled, role = Role.Tab, onClick = { onSelect(tab) }
                    ),
                    color = if (selected == tab) Forest600 else Color.Transparent,
                    contentColor = if (selected == tab) Color.White else Ink600,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.heightIn(min = 48.dp).padding(horizontal = 8.dp, vertical = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (tab == AuthTab.EMAIL) Icons.Default.Email else Icons.Default.Phone, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (tab == AuthTab.EMAIL) "Email" else "Phone", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
internal fun AuthErrorMessage(message: String) {
    Surface(color = Color(0xFFFFEDE7), shape = RoundedCornerShape(12.dp)) {
        Text(message, Modifier.fillMaxWidth().padding(14.dp).semantics { liveRegion = LiveRegionMode.Polite }, color = Rust900, style = MaterialTheme.typography.bodyMedium)
    }
}
