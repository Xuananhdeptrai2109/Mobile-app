package com.moneycare.app.ui.auth.register

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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneycare.app.ui.components.AppTextField
import com.moneycare.app.ui.components.AppTopBar
import com.moneycare.app.ui.components.AuthIllustration
import com.moneycare.app.ui.components.AuthIllustrationType
import com.moneycare.app.ui.components.PasswordTextField
import com.moneycare.app.ui.components.PrimaryButton
import com.moneycare.app.ui.components.SocialLoginRow
import com.moneycare.app.ui.components.StaggerItem
import com.moneycare.app.ui.components.ValidationMessage
import com.moneycare.app.ui.theme.MintPrimary
import com.moneycare.app.ui.theme.MoneyCareSpacing

private const val SOCIAL_AUTH_ENABLED = false

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                RegisterUiEffect.NavigateToHome -> onNavigateToHome()
                RegisterUiEffect.NavigateToLogin -> onNavigateToLogin()
                is RegisterUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        RegisterContent(
            state = state,
            onBackClick = onBackClick,
            onEvent = viewModel::onEvent
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(MoneyCareSpacing.M)
        )
    }
}

@Composable
private fun RegisterContent(
    state: RegisterUiState,
    onBackClick: () -> Unit,
    onEvent: (RegisterUiEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MoneyCareSpacing.L)
    ) {
        Spacer(Modifier.height(MoneyCareSpacing.M))

        StaggerItem(index = 0) {
            AppTopBar(onBackClick = onBackClick)
        }

        Spacer(Modifier.height(MoneyCareSpacing.L))

        StaggerItem(index = 1) {
            Text(
                text = "Tạo tài khoản",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(MoneyCareSpacing.M))

        StaggerItem(index = 2) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                AuthIllustration(type = AuthIllustrationType.REGISTER)
            }
        }

        Spacer(Modifier.height(MoneyCareSpacing.L))

        StaggerItem(index = 3) {
            Column(verticalArrangement = Arrangement.spacedBy(MoneyCareSpacing.M)) {
                AppTextField(
                    value = state.fullName,
                    onValueChange = { onEvent(RegisterUiEvent.FullNameChanged(it)) },
                    placeholder = "Họ và tên",
                    isError = state.fullNameError != null,
                    errorMessage = state.fullNameError
                )

                AppTextField(
                    value = state.email,
                    onValueChange = { onEvent(RegisterUiEvent.EmailChanged(it)) },
                    placeholder = "Email",
                    isError = state.emailError != null,
                    errorMessage = state.emailError
                )

                PasswordTextField(
                    value = state.password,
                    onValueChange = { onEvent(RegisterUiEvent.PasswordChanged(it)) },
                    placeholder = "Mật khẩu",
                    isPasswordVisible = state.isPasswordVisible,
                    onToggleVisibility = { onEvent(RegisterUiEvent.TogglePasswordVisibility) },
                    isError = state.passwordError != null,
                    errorMessage = state.passwordError
                )

                PasswordTextField(
                    value = state.confirmPassword,
                    onValueChange = { onEvent(RegisterUiEvent.ConfirmPasswordChanged(it)) },
                    placeholder = "Xác nhận mật khẩu",
                    isPasswordVisible = state.isConfirmPasswordVisible,
                    onToggleVisibility = { onEvent(RegisterUiEvent.ToggleConfirmPasswordVisibility) },
                    isError = state.confirmPasswordError != null,
                    errorMessage = state.confirmPasswordError
                )

                if (state.generalError != null) {
                    ValidationMessage(message = state.generalError, isError = true)
                }
            }
        }

        Spacer(Modifier.height(MoneyCareSpacing.L))

        StaggerItem(index = 4) {
            PrimaryButton(
                text = "Đăng ký",
                onClick = { onEvent(RegisterUiEvent.Submit) },
                isLoading = state.isLoading
            )
        }

        if (SOCIAL_AUTH_ENABLED) {
            Spacer(Modifier.height(MoneyCareSpacing.L))

            StaggerItem(index = 5) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = "  Hoặc đăng ký với  ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
                    }

                    Spacer(Modifier.height(MoneyCareSpacing.M))

                    SocialLoginRow(
                        onGoogleClick = { onEvent(RegisterUiEvent.SocialLoginClicked(com.moneycare.app.ui.components.SocialProvider.GOOGLE)) },
                        onFacebookClick = { onEvent(RegisterUiEvent.SocialLoginClicked(com.moneycare.app.ui.components.SocialProvider.FACEBOOK)) },
                        onAppleClick = { onEvent(RegisterUiEvent.SocialLoginClicked(com.moneycare.app.ui.components.SocialProvider.APPLE)) }
                    )
                }
            }
        }

        Spacer(Modifier.height(MoneyCareSpacing.L))

        StaggerItem(index = 6) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Đã có tài khoản? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Đăng nhập",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MintPrimary,
                    modifier = Modifier.clickable { onEvent(RegisterUiEvent.NavigateToLogin) }
                )
            }
        }

        Spacer(Modifier.height(MoneyCareSpacing.L))
    }
}
