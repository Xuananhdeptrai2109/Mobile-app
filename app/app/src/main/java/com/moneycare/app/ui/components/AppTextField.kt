package com.moneycare.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.moneycare.app.ui.theme.MintPrimary
import com.moneycare.app.ui.theme.SoftGreenSuccess
import com.moneycare.app.ui.theme.SoftRed

/**
 * Reusable rounded text field used across the app (email, name, phone...).
 * Wraps [OutlinedTextField] with app-consistent shape and color, plus an
 * animated error / success helper row (via [ValidationMessage]) so the
 * layout never jumps abruptly when validation state changes.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorMessage: String? = null,
    isValid: Boolean = false,
    validMessage: String = "Trông ổn đấy! ✅",
    singleLine: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon ?: if (isValid) {
                { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SoftGreenSuccess) }
            } else null,
            singleLine = singleLine,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MintPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = SoftRed,
                focusedLeadingIconColor = MintPrimary,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MintPrimary
            )
        )

        if (isError && !errorMessage.isNullOrBlank()) {
            ValidationMessage(message = errorMessage, isError = true)
        } else if (isValid) {
            ValidationMessage(message = validMessage, isError = false)
        }
    }
}
