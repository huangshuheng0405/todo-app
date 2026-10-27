package com.example.app2

import android.app.Application
import android.content.Context
import com.example.app2.auth.AuthRepository
import com.example.app2.auth.LocalAuthRepository
import com.example.app2.data.TodoDatabase
import com.example.app2.data.TodoRepository

class TodoApp : Application() {

    val database: TodoDatabase by lazy { TodoDatabase.build(this) }

    val repository: TodoRepository by lazy { TodoRepository(database.dao()) }

    lateinit var prefs: AppPrefs
        private set

    /**
     * 认证实现。接后端时把这里换成 ApiAuthRepository 即可，
     * 其余代码只依赖 [AuthRepository] 接口。
     */
    lateinit var auth: AuthRepository
        private set

    override fun onCreate() {
        super.onCreate()
        prefs = AppPrefs(this)
        auth = LocalAuthRepository(this)
    }
}

/** 只存外观设置这类轻量偏好，没必要为它上 DataStore。 */
class AppPrefs(context: Context) {

    private val sp = context.getSharedPreferences("todoist_prefs", Context.MODE_PRIVATE)

    /** 0 = 跟随系统，1 = 浅色，2 = 深色 */
    var themeMode: Int
        get() = sp.getInt(KEY_THEME, 0)
        set(value) = sp.edit().putInt(KEY_THEME, value).apply()

    private companion object {
        const val KEY_THEME = "theme_mode"
    }
}
