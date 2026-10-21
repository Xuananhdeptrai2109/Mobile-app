package com.moneycare.app.ui.auth.login

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
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
private const val PASSWORD_RECOVERY_ENABLED = false

@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                LoginUiEffect.NavigateToHome -> onNavigateToHome()
                LoginUiEffect.NavigateToRegister -> onNavigateToRegister()
                LoginUiEffect.NavigateToForgotPassword -> onNavigateToForgotPassword()
                is LoginUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LoginContent(
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
private fun LoginContent(
    state: LoginUiState,
    onBackClick: () -> Unit,
    onEvent: (LoginUiEvent) -> Unit
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
                text = "Đăng nhập",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(MoneyCareSpacing.M))

        StaggerItem(index = 2) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                AuthIllustration(type = AuthIllustrationType.LOGIN)
            }
        }

        Spacer(Modifier.height(MoneyCareSpacing.L))

        StaggerItem(index = 3) {
            Column(verticalArrangement = Arrangement.spacedBy(MoneyCareSpacing.M)) {
                Text(
                    text = "Email hoặc số điện thoại",
                    style = MaterialTheme.typography.labelLarge
                )
                AppTextField(
                    value = state.emailOrPhone,
                    onValueChange = { onEvent(LoginUiEvent.EmailOrPhoneChanged(it)) },
                    placeholder = "Nhập email hoặc số điện thoại",
                    leadingIcon = { androidx.compose.material3.Icon(Icons.Filled.Email, contentDescription = null) },
                    isError = state.emailError != null,
                    errorMessage = state.emailError
                )

                Text(
                    text = "Mật khẩu",
                    style = MaterialTheme.typography.labelLarge
                )
                PasswordTextField(
                    value = state.password,
                    onValueChange = { onEvent(LoginUiEvent.PasswordChanged(it)) },
                    placeholder = "Nhập mật khẩu",
                    isPasswordVisible = state.isPasswordVisible,
                    onToggleVisibility = { onEvent(LoginUiEvent.TogglePasswordVisibility) },
                    isError = state.passwordError != null,
                    errorMessage = state.passwordError
                )

                if (PASSWORD_RECOVERY_ENABLED) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { onEvent(LoginUiEvent.ForgotPasswordClicked) }) {
                            Text(
                                "Quên mật khẩu?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MintPrimary
                            )
                        }
                    }
                }

                if (state.generalError != null) {
                    ValidationMessage(message = state.generalError, isError = true)
                }
            }
        }

        Spacer(Modifier.height(MoneyCareSpacing.S))

        StaggerItem(index = 4) {
            PrimaryButton(
                text = "Đăng nhập",
                onClick = { onEvent(LoginUiEvent.Submit) },
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
                            text = "  Hoặc đăng nhập với  ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
                    }

                    Spacer(Modifier.height(MoneyCareSpacing.M))

                    SocialLoginRow(
                        onGoogleClick = { onEvent(LoginUiEvent.SocialLoginClicked(com.moneycare.app.ui.components.SocialProvider.GOOGLE)) },
                        onFacebookClick = { onEvent(LoginUiEvent.SocialLoginClicked(com.moneycare.app.ui.components.SocialProvider.FACEBOOK)) },
                        onAppleClick = { onEvent(LoginUiEvent.SocialLoginClicked(com.moneycare.app.ui.components.SocialProvider.APPLE)) }
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
                    text = "Chưa có tài khoản? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Đăng ký ngay",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MintPrimary,
                    modifier = Modifier.clickable { onEvent(LoginUiEvent.NavigateToRegister) }
                )
            }
        }

        Spacer(Modifier.height(MoneyCareSpacing.L))
    }
}
