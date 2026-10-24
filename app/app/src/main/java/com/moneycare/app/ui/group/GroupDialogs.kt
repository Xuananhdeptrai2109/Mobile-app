package com.moneycare.app.ui.group

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.moneycare.app.data.model.ExpenseGroup

@Composable
fun EditGroupDialog(
    group: ExpenseGroup,
    budgetOnly: Boolean = false,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, budget: Long) -> Unit
) {
    var name by rememberSaveable(group.id) { mutableStateOf(group.name) }
    var description by rememberSaveable(group.id) { mutableStateOf(group.description) }
    var budget by rememberSaveable(group.id) { mutableStateOf(group.budget.toString()) }
    val parsedBudget = budget.toLongOrNull()
    val valid = name.isNotBlank() && name.length <= 120 && parsedBudget != null && parsedBudget >= 0

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(if (budgetOnly) "Quản lý ngân sách" else "Chỉnh sửa nhóm") },
        text = {
            Column {
                if (!budgetOnly) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { if (it.length <= 120) name = it },
                        label = { Text("Tên nhóm") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { if (it.length <= 500) description = it },
                        label = { Text("Mô tả") },
                        minLines = 2,
                        maxLines = 4
                    )
                    Spacer(Modifier.height(12.dp))
                }
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it.filter(Char::isDigit) },
                    label = { Text("Ngân sách (đ)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                enabled = valid && !isSaving,
                onClick = { onConfirm(name.trim(), description.trim(), parsedBudget ?: 0L) }
            ) {
                Text(if (isSaving) "Đang lưu…" else "Lưu")
            }
        },
        dismissButton = {
            TextButton(enabled = !isSaving, onClick = onDismiss) { Text("Hủy") }
        }
    )
}

@Composable
fun EditMemberDialog(
    initialName: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("Sửa tên thành viên") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 255) name = it },
                label = { Text("Tên thành viên") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && !isSaving,
                onClick = { onConfirm(name.trim()) }
            ) { Text(if (isSaving) "Đang lưu…" else "Lưu") }
        },
        dismissButton = {
            TextButton(enabled = !isSaving, onClick = onDismiss) { Text("Hủy") }
        }
    )
}

@Composable
fun ConfirmGroupActionDialog(
    title: String,
    message: String,
    confirmLabel: String,
    destructive: Boolean = false,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                enabled = !isSaving,
                onClick = onConfirm,
                colors = if (destructive) {
                    ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) { Text(if (isSaving) "Đang xử lý…" else confirmLabel) }
        },
        dismissButton = {
            TextButton(enabled = !isSaving, onClick = onDismiss) { Text("Hủy") }
        }
    )
}
