package com.moneycare.app.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneycare.app.MoneyCareApp
import com.moneycare.app.data.api.RetrofitClient
import com.moneycare.app.data.model.auth.RegisterRequest
import com.moneycare.app.ui.components.SocialProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    private val _effect = Channel<RegisterUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: RegisterUiEvent) {
        when (event) {
            is RegisterUiEvent.FullNameChanged ->
                _uiState.update { it.copy(fullName = event.value, fullNameError = null) }
            is RegisterUiEvent.EmailChanged ->
                _uiState.update { it.copy(email = event.value, emailError = null) }
            is RegisterUiEvent.PasswordChanged ->
                _uiState.update { it.copy(password = event.value, passwordError = null, confirmPasswordError = null) }
            is RegisterUiEvent.ConfirmPasswordChanged ->
                _uiState.update { it.copy(confirmPassword = event.value, confirmPasswordError = null) }
            RegisterUiEvent.TogglePasswordVisibility ->
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            RegisterUiEvent.ToggleConfirmPasswordVisibility ->
                _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
            RegisterUiEvent.NavigateToLogin ->
                viewModelScope.launch { _effect.send(RegisterUiEffect.NavigateToLogin) }
            is RegisterUiEvent.SocialLoginClicked ->
                viewModelScope.launch { _effect.send(RegisterUiEffect.ShowMessage("Đăng ký với ${event.provider} (demo)")) }
            RegisterUiEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val state = _uiState.value

        val fullNameError = if (state.fullName.isBlank()) "Bạn nhập họ và tên nhé." else null
        val emailError = if (!isValidEmail(state.email)) "Email chưa đúng định dạng, kiểm tra lại nhé." else null
        val passwordError = if (state.password.length < 8) "Mật khẩu cần ít nhất 8 ký tự nhé." else null
        val confirmPasswordError = if (state.confirmPassword != state.password) {
            "Hai mật khẩu chưa giống nhau, thử lại nhé 🥲"
        } else null

        if (listOf(fullNameError, emailError, passwordError, confirmPasswordError).any { it != null }) {
            _uiState.update {
                it.copy(
                    fullNameError = fullNameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            try {
                val response = RetrofitClient.apiService.register(
                    RegisterRequest(
                        fullName = state.fullName,
                        email = state.email,
                        phoneNumber = null,
                        password = state.password,
                        confirmPassword = state.confirmPassword
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
                    _uiState.update { it.copy(isLoading = false, registerSuccess = true) }
                    delay(1400)
                    _effect.send(RegisterUiEffect.NavigateToHome)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = response.message ?: "Phản hồi đăng ký không hợp lệ"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = "Không thể kết nối tới server. Vui lòng thử lại sau! 🥲"
                    )
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean =
        email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val fullNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val registerSuccess: Boolean = false
)

sealed interface RegisterUiEvent {
    data class FullNameChanged(val value: String) : RegisterUiEvent
    data class EmailChanged(val value: String) : RegisterUiEvent
    data class PasswordChanged(val value: String) : RegisterUiEvent
    data class ConfirmPasswordChanged(val value: String) : RegisterUiEvent
    data object TogglePasswordVisibility : RegisterUiEvent
    data object ToggleConfirmPasswordVisibility : RegisterUiEvent
    data object Submit : RegisterUiEvent
    data object NavigateToLogin : RegisterUiEvent
    data class SocialLoginClicked(val provider: SocialProvider) : RegisterUiEvent
}

sealed interface RegisterUiEffect {
    data object NavigateToHome : RegisterUiEffect
    data object NavigateToLogin : RegisterUiEffect
    data class ShowMessage(val message: String) : RegisterUiEffect
}
