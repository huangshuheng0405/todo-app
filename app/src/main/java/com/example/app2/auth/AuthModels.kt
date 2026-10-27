package com.example.app2.auth

/**
 * 登录态。接后端后，[token] 换成服务端下发的 JWT / session id，
 * 所有请求带上它即可，UI 层不用改。
 */
data class Session(
    val userId: String,
    val email: String,
    val displayName: String,
    val token: String
) {
    /** 头像占位用的首字母 */
    val initial: String
        get() = displayName.firstOrNull()?.uppercase() ?: email.firstOrNull()?.uppercase() ?: "?"
}

sealed interface AuthResult {
    data class Success(val session: Session) : AuthResult

    /** [message] 直接展示给用户，后端接入后可以映射 HTTP 错误码 */
    data class Failure(val message: String) : AuthResult
}
