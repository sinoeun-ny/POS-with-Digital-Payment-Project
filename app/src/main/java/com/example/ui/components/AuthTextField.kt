package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

@Composable
fun ZandoOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPhonePrefix: Boolean = false,
    phonePrefix: String = "+855",
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    imeAction: ImeAction = ImeAction.Next,
    onDone: (() -> Unit)? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth().heightIn(min = 58.dp),
        enabled = enabled,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        prefix = if (isPhonePrefix) ({ Text(phonePrefix) }) else null,
        singleLine = true,
        isError = isError,
        supportingText = if (supportingText != null) ({ Text(supportingText) }) else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Next) },
            onDone = {
                focusManager.clearFocus()
                onDone?.invoke()
            }
        ),
        trailingIcon = if (isPassword) ({
            IconButton(onClick = { passwordVisible = !passwordVisible }, enabled = enabled) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                )
            }
        }) else null,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Forest700,
            unfocusedBorderColor = Sage300,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedTextColor = Ink950,
            unfocusedTextColor = Ink950,
            focusedLabelColor = Forest700,
            unfocusedLabelColor = Ink600,
            focusedPlaceholderColor = Ink500,
            unfocusedPlaceholderColor = Ink500,
            focusedSupportingTextColor = Ink600,
            unfocusedSupportingTextColor = Ink600,
            focusedPrefixColor = Ink700,
            unfocusedPrefixColor = Ink700,
            focusedTrailingIconColor = Ink600,
            unfocusedTrailingIconColor = Ink600,
            disabledContainerColor = Sage50,
            disabledTextColor = Ink600,
            disabledLabelColor = Ink600,
            disabledBorderColor = Sage200,
            errorTextColor = Ink950,
            errorContainerColor = Color.White,
            errorBorderColor = Rust600,
            errorLabelColor = Rust600,
            errorSupportingTextColor = Rust600
        )
    )
}
