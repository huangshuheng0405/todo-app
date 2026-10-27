package com.huangshuheng.todo.ui.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.huangshuheng.todo.R
import com.huangshuheng.todo.TodoApp
import com.huangshuheng.todo.auth.AuthRepository
import com.huangshuheng.todo.auth.AuthResult
import com.huangshuheng.todo.auth.AuthValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode(
    @StringRes val labelRes: Int,
    @StringRes val submitLabelRes: Int
) {
    SIGN_IN(R.string.login_mode_signin, R.string.login_submit_signin),
    SIGN_UP(R.string.login_mode_signup, R.string.login_submit_signup)
}

data class LoginUiState(
    val mode: AuthMode = AuthMode.SIGN_IN,
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    @StringRes val error: Int? = null
) {
    /** 只有用户开始输入后才提示格式错误，避免一进来就满屏红字 */
    @get:StringRes
    val emailError: Int?
        get() = if (email.isEmpty()) null else AuthValidation.emailError(email)

    @get:StringRes
    val passwordError: Int?
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
                    it.copy(loading = false, error = result.messageRes)
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
