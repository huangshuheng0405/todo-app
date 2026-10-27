package com.example.app2.auth

/**
 * 纯校验逻辑，抽出来是为了能写单元测试（不含任何 Android 依赖）。
 */
object AuthValidation {

    const val MIN_PASSWORD_LENGTH = 6

    private val EMAIL_REGEX =
        Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")

    fun normalizeEmail(email: String): String = email.trim().lowercase()

    fun emailError(email: String): String? = when {
        email.isBlank() -> "请输入邮箱"
        !EMAIL_REGEX.matches(email.trim()) -> "邮箱格式不正确"
        else -> null
    }

    fun passwordError(password: String): String? = when {
        password.isEmpty() -> "请输入密码"
        password.length < MIN_PASSWORD_LENGTH -> "密码至少 $MIN_PASSWORD_LENGTH 位"
        else -> null
    }
}
