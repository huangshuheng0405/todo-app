package com.example.app2.auth

import android.content.Context
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * **占位实现**：账号完全存在本机，没有网络请求。
 *
 * 目的只是让登录流程、登录态、按账号隔离数据能端到端跑起来，
 * 等后端就绪时换成 `ApiAuthRepository`（见 [AuthRepository] 注释），UI 不需要改动。
 *
 * 注意：密码用 PBKDF2 加盐哈希后存 SharedPreferences，只为了避免明文落盘；
 * 真正的强度校验与风控必须在服务端做。
 */
class LocalAuthRepository(context: Context) : AuthRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _session = MutableStateFlow(readPersistedSession())

    override val session: StateFlow<Session?> = _session

    override suspend fun signIn(email: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            val key = AuthValidation.normalizeEmail(email)
            val stored = prefs.getString(keyPassword(key), null)
                ?: return@withContext AuthResult.Failure(MSG_BAD_CREDENTIALS)

            val parts = stored.split(":")
            if (parts.size != 2 || hashPassword(password, parts[0]) != parts[1]) {
                return@withContext AuthResult.Failure(MSG_BAD_CREDENTIALS)
            }

            val userId = prefs.getString(keyUid(key), null)
                ?: return@withContext AuthResult.Failure("账号数据异常，请重新注册")

            startSession(newSession(userId, key))
        }

    override suspend fun signUp(email: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            val key = AuthValidation.normalizeEmail(email)
            AuthValidation.emailError(key)?.let { return@withContext AuthResult.Failure(it) }
            AuthValidation.passwordError(password)?.let { return@withContext AuthResult.Failure(it) }

            if (prefs.contains(keyPassword(key))) {
                return@withContext AuthResult.Failure("该邮箱已注册，直接登录吧")
            }

            val salt = newSalt()
            val userId = UUID.randomUUID().toString()
            prefs.edit()
                .putString(keyPassword(key), "$salt:${hashPassword(password, salt)}")
                .putString(keyUid(key), userId)
                .apply()

            startSession(newSession(userId, key))
        }

    override suspend fun signOut() {
        withContext(Dispatchers.IO) {
            prefs.edit()
                .remove(K_SESSION_UID)
                .remove(K_SESSION_EMAIL)
                .remove(K_SESSION_TOKEN)
                .apply()
        }
        _session.value = null
    }

    // ---------------- 内部实现 ----------------

    private fun newSession(userId: String, email: String) = Session(
        userId = userId,
        email = email,
        displayName = email.substringBefore("@"),
        token = UUID.randomUUID().toString()
    )

    private fun startSession(session: Session): AuthResult {
        prefs.edit()
            .putString(K_SESSION_UID, session.userId)
            .putString(K_SESSION_EMAIL, session.email)
            .putString(K_SESSION_TOKEN, session.token)
            .apply()
        _session.value = session
        return AuthResult.Success(session)
    }

    private fun readPersistedSession(): Session? {
        val uid = prefs.getString(K_SESSION_UID, null) ?: return null
        val email = prefs.getString(K_SESSION_EMAIL, null) ?: return null
        val token = prefs.getString(K_SESSION_TOKEN, null) ?: return null
        return Session(uid, email, email.substringBefore("@"), token)
    }

    private fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun hashPassword(password: String, saltBase64: String): String {
        val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        return Base64.encodeToString(factory.generateSecret(spec).encoded, Base64.NO_WRAP)
    }

    private fun keyPassword(email: String) = "pwd:$email"

    private fun keyUid(email: String) = "uid:$email"

    private companion object {
        const val PREFS_NAME = "todo_auth"
        const val K_SESSION_UID = "session_uid"
        const val K_SESSION_EMAIL = "session_email"
        const val K_SESSION_TOKEN = "session_token"
        const val PBKDF2_ITERATIONS = 10_000
        const val MSG_BAD_CREDENTIALS = "邮箱或密码不正确"
    }
}
