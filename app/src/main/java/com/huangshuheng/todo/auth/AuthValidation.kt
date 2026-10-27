package com.huangshuheng.todo.auth

import androidx.annotation.StringRes
import com.huangshuheng.todo.R

/**
 * 纯校验逻辑，抽出来是为了能写单元测试（不含任何 Android 依赖）。
 * 返回的是字符串资源 id，文案放在 strings.xml。
 */
object AuthValidation {

    const val MIN_PASSWORD_LENGTH = 6

    private val EMAIL_REGEX =
        Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")

    fun normalizeEmail(email: String): String = email.trim().lowercase()

    @StringRes
    fun emailError(email: String): Int? = when {
        email.isBlank() -> R.string.error_email_required
        !EMAIL_REGEX.matches(email.trim()) -> R.string.error_email_invalid
        else -> null
    }

    @StringRes
    fun passwordError(password: String): Int? = when {
        password.isEmpty() -> R.string.error_password_required
        password.length < MIN_PASSWORD_LENGTH -> R.string.error_password_short
        else -> null
    }
}
