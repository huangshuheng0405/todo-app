package com.huangshuheng.todo.auth

import androidx.annotation.StringRes

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

    /**
     * [messageRes] 是字符串资源 id 而不是字面量：
     * 一是便于本地化，二是 UI 直接 stringResource 显示，不用在数据层拼中文。
     * 接入后端后可把 HTTP 错误码映射成不同的资源 id。
     */
    data class Failure(@StringRes val messageRes: Int) : AuthResult
}
