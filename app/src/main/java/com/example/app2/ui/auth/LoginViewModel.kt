package com.example.app2.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.app2.TodoApp
import com.example.app2.auth.AuthRepository
import com.example.app2.auth.AuthResult
import com.example.app2.auth.AuthValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode(val label: String, val submitLabel: String) {
    SIGN_IN("登录", "登录"),
    SIGN_UP("注册", "创建账号")
}

data class LoginUiState(
    val mode: AuthMode = AuthMode.SIGN_IN,
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null
) {
    /** 只有用户开始输入后才提示格式错误，避免一进来就满屏红字 */
    val emailError: String?
        get() = if (email.isEmpty()) null else AuthValidation.emailError(email)

    val passwordError: String?
        get() = if (password.isEmpty()) null else AuthValidation.passwordError(password)

    val canSubmit: Boolean
        get() = !loading &&
            AuthValidation.emailError(email) == null &&
            AuthValidation.passwordError(password) == null
}

class LoginViewModel(private val auth: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, error = null) }
    }

    fun switchMode(mode: AuthMode) {
        _state.update { it.copy(mode = mode, error = null) }
    }

    fun submit() {
        val snapshot = _state.value
        if (!snapshot.canSubmit) return

        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = when (snapshot.mode) {
                AuthMode.SIGN_IN -> auth.signIn(snapshot.email, snapshot.password)
                AuthMode.SIGN_UP -> auth.signUp(snapshot.email, snapshot.password)
            }
            when (result) {
                // 成功后 session 流会推给 AppRoot，这个页面自然被替换掉
                is AuthResult.Success -> _state.update { it.copy(loading = false, password = "") }
                is AuthResult.Failure -> _state.update {
                    it.copy(loading = false, error = result.message)
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TodoApp
                LoginViewModel(app.auth)
            }
        }
    }
}
