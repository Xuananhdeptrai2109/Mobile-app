package com.moneycare.app.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneycare.app.MoneyCareApp
import com.moneycare.app.data.api.RetrofitClient
import com.moneycare.app.data.model.auth.LoginRequest
import com.moneycare.app.ui.components.SocialProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Holds Login screen state and business logic (validation, submit).
 */
class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _effect = Channel<LoginUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.EmailOrPhoneChanged -> {
                _uiState.update {
                    it.copy(emailOrPhone = event.value, emailError = null, generalError = null)
                }
            }
            is LoginUiEvent.PasswordChanged -> {
                _uiState.update {
                    it.copy(password = event.value, passwordError = null, generalError = null)
                }
            }
            LoginUiEvent.TogglePasswordVisibility -> {
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            LoginUiEvent.Submit -> submit()
            LoginUiEvent.ForgotPasswordClicked -> {
                viewModelScope.launch { _effect.send(LoginUiEffect.NavigateToForgotPassword) }
            }
            LoginUiEvent.NavigateToRegister -> {
                viewModelScope.launch { _effect.send(LoginUiEffect.NavigateToRegister) }
            }
            is LoginUiEvent.SocialLoginClicked -> {
                viewModelScope.launch {
                    _effect.send(LoginUiEffect.ShowMessage("Đăng nhập với ${event.provider} (demo)"))
                }
            }
        }
    }

    private fun submit() {
        val state = _uiState.value
        val emailError = if (state.emailOrPhone.isBlank()) {
            "Bạn nhập email hoặc số điện thoại nhé."
        } else null
        val passwordError = if (state.password.length < 8) {
            "Mật khẩu cần ít nhất 8 ký tự nhé."
        } else null

        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            try {
                val response = RetrofitClient.apiService.login(
                    LoginRequest(
                        emailOrPhoneNumber = state.emailOrPhone,
                        password = state.password
                    )
                )

                val token = response.token
                val userId = response.userId
                if (response.success && token != null && userId != null) {
                    // Save session
                    MoneyCareApp.instance.sessionManager.saveSession(
                        token = token,
                        userId = userId,
                        userName = response.fullName ?: ""
                    )
                    _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                    _effect.send(LoginUiEffect.NavigateToHome)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = response.message ?: "Phản hồi đăng nhập không hợp lệ"
                        )
                    }
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val message = try {
                    val gson = com.google.gson.Gson()
                    val errorMap = gson.fromJson(errorBody, Map::class.java)
                    errorMap["message"]?.toString() ?: "Thông tin đăng nhập không chính xác (401)"
                } catch (parseEx: Exception) {
                    "Thông tin đăng nhập không chính xác (401)"
                }
                _uiState.update {
                    it.copy(isLoading = false, generalError = message)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = "Lỗi: ${e.localizedMessage ?: "Không thể kết nối tới server"}"
                    )
                }
            }
        }
    }
}

data class LoginUiState(
    val emailOrPhone: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val loginSuccess: Boolean = false
)

sealed interface LoginUiEvent {
    data class EmailOrPhoneChanged(val value: String) : LoginUiEvent
    data class PasswordChanged(val value: String) : LoginUiEvent
    data object TogglePasswordVisibility : LoginUiEvent
    data object Submit : LoginUiEvent
    data object ForgotPasswordClicked : LoginUiEvent
    data object NavigateToRegister : LoginUiEvent
    data class SocialLoginClicked(val provider: SocialProvider) : LoginUiEvent
}

sealed interface LoginUiEffect {
    data object NavigateToHome : LoginUiEffect
    data object NavigateToRegister : LoginUiEffect
    data object NavigateToForgotPassword : LoginUiEffect
    data class ShowMessage(val message: String) : LoginUiEffect
}
