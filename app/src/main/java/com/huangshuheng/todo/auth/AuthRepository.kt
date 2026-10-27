package com.huangshuheng.todo.auth

import kotlinx.coroutines.flow.StateFlow

/**
 * 认证契约。UI 只依赖这个接口，不关心底下是本地假账号还是真实 API。
 *
 * 接后端时新建一个 `ApiAuthRepository` 实现这三个方法即可：
 * - signIn  → POST /auth/login   { email, password } → { userId, token }
 * - signUp  → POST /auth/register
 * - signOut → 清本地 token（可选再通知服务端吊销）
 * 然后把 [com.huangshuheng.todo.TodoApp] 里的实现替换掉。
 */
interface AuthRepository {

    /** 当前登录态，null 表示未登录；UI 据此在登录页与主界面之间切换 */
    val session: StateFlow<Session?>

    suspend fun signIn(email: String, password: String): AuthResult

    suspend fun signUp(email: String, password: String): AuthResult

    suspend fun signOut()
}
