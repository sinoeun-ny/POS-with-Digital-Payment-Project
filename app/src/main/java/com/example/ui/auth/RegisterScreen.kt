package com.example.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
    val focusManager = LocalFocusManager.current
    var selectedTab by rememberSaveable { mutableStateOf(AuthTab.PHONE) }
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf("Male") }
    var selectedCity by rememberSaveable { mutableStateOf("Phnom Penh") }
    var cityMenuExpanded by remember { mutableStateOf(false) }
    var termsAgreed by rememberSaveable { mutableStateOf(true) }
    var attemptedSubmit by remember { mutableStateOf(false) }
    val cities = listOf("Phnom Penh", "Siem Reap", "Battambang", "Sihanoukville", "Kampot", "Kandal")
    val contact = if (selectedTab == AuthTab.PHONE) phone else email
    val missingName = firstName.isBlank() && lastName.isBlank()

    fun submit() {
        attemptedSubmit = true
        if (!uiState.isLoading && !missingName && contact.isNotBlank() && password.length >= 6 && termsAgreed) {
            focusManager.clearFocus()
            val fullPhone = if (selectedTab == AuthTab.PHONE) "+855${phone.trim()}" else phone
            viewModel.register(
                firstName = firstName.trim(), lastName = lastName.trim(),
                phone = fullPhone.trim(), email = email.trim(), password = password,
                cityProvince = selectedCity, gender = gender, selectedRole = UserRole.CUSTOMER
            )
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            viewModel.clearMessages()
            onRegisterSuccess()
        }
    }

    AuthScreenLayout(onNavigateBack) {
        AuthHeader("Make yourself at home", "Create your Xora account to save your favorites and keep track of every order.")
        Card(
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Sage200)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Your details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Ink950)
                ZandoOutlinedTextField(
                    value = firstName, onValueChange = { firstName = it }, label = "First name", placeholder = "Sok",
                    enabled = !uiState.isLoading, isError = attemptedSubmit && missingName,
                    supportingText = if (attemptedSubmit && missingName) "Enter your first or last name." else null
                )
                ZandoOutlinedTextField(
                    value = lastName, onValueChange = { lastName = it }, label = "Last name", placeholder = "Chan", enabled = !uiState.isLoading
                )
                HorizontalDivider(color = Sage100)
                Text("Choose how to sign in", style = MaterialTheme.typography.titleSmall, color = Ink950)
                AuthContactTabs(selectedTab, !uiState.isLoading) {
                    selectedTab = it
                    attemptedSubmit = false
                    viewModel.clearMessages()
                }
                ZandoOutlinedTextField(
                    value = contact,
                    onValueChange = { if (selectedTab == AuthTab.PHONE) phone = it else email = it },
                    label = if (selectedTab == AuthTab.PHONE) "Phone number" else "Email address",
                    placeholder = if (selectedTab == AuthTab.PHONE) "12 345 678" else "name@example.com",
                    isPhonePrefix = selectedTab == AuthTab.PHONE,
                    keyboardType = if (selectedTab == AuthTab.PHONE) KeyboardType.Phone else KeyboardType.Email,
                    enabled = !uiState.isLoading,
                    isError = attemptedSubmit && contact.isBlank(),
                    supportingText = if (attemptedSubmit && contact.isBlank()) "Enter your contact details." else null
                )
                ZandoOutlinedTextField(
                    value = password, onValueChange = { password = it }, label = "Password", placeholder = "Create a password",
                    isPassword = true, enabled = !uiState.isLoading, imeAction = ImeAction.Done,
                    isError = attemptedSubmit && password.length < 6, supportingText = "Use at least 6 characters."
                )
                Text("Gender", style = MaterialTheme.typography.labelLarge, color = Ink700)
                Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Male", "Female").forEach { option ->
                        Surface(
                            color = if (gender == option) Forest50 else Color.White,
                            shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, if (gender == option) Forest600 else Sage200),
                            modifier = Modifier.weight(1f).selectable(selected = gender == option, enabled = !uiState.isLoading, role = Role.RadioButton, onClick = { gender = option })
                        ) {
                            Row(Modifier.heightIn(min = 48.dp).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = gender == option, onClick = null, enabled = !uiState.isLoading)
                                Spacer(Modifier.width(8.dp))
                                Text(option, style = MaterialTheme.typography.bodyMedium, color = Ink950)
                            }
                        }
                    }
                }
                Text("City / delivery area", style = MaterialTheme.typography.labelLarge, color = Ink700)
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { cityMenuExpanded = true }, enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Forest700), shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(selectedCity, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Choose city")
                    }
                    DropdownMenu(expanded = cityMenuExpanded, onDismissRequest = { cityMenuExpanded = false }) {
                        cities.forEach { city ->
                            DropdownMenuItem(text = { Text(city) }, onClick = { selectedCity = city; cityMenuExpanded = false })
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().toggleable(value = termsAgreed, enabled = !uiState.isLoading, role = Role.Checkbox, onValueChange = { termsAgreed = it }).padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = termsAgreed, onCheckedChange = null, modifier = Modifier.size(48.dp), enabled = !uiState.isLoading)
                    Text("I agree to the Xora Terms of Service and Privacy Policy.", style = MaterialTheme.typography.bodySmall, color = Ink600)
                }
                if (attemptedSubmit && !termsAgreed) AuthErrorMessage("Please agree to the terms to create your account.")
                uiState.errorMessage?.let { AuthErrorMessage(it) }
                Button(onClick = ::submit, enabled = !uiState.isLoading, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp), colors = ButtonDefaults.buttonColors(containerColor = Forest700, contentColor = Color.White, disabledContainerColor = Sage200, disabledContentColor = Ink600), shape = RoundedCornerShape(14.dp)) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(if (uiState.isLoading) "Creating account…" else "Create account", fontWeight = FontWeight.Bold)
                }
            }
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Already have an account?", style = MaterialTheme.typography.bodyMedium, color = Ink600)
            TextButton(onClick = onNavigateToLogin, enabled = !uiState.isLoading, colors = ButtonDefaults.textButtonColors(contentColor = Forest700)) { Text("Sign in", fontWeight = FontWeight.Bold) }
        }
    }
}
