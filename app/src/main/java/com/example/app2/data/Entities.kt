package com.example.app2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 优先级：1 = P1（最高，红）… 4 = 无优先级 */
const val PRIORITY_NONE = 4

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val note: String = "",
    /** 到期日，归一化到当天 00:00 的时间戳；null 表示没有日期 */
    val dueDate: Long? = null,
    /** 可选的提醒时间，例如 "14:00" */
    val dueTime: String? = null,
    val priority: Int = PRIORITY_NONE,
    val projectId: Long? = null,
    /**
     * 数据归属者。接后端后由服务端返回的 userId 填充；
     * 本地也靠它做隔离，换账号看不到别人的待办。
     * null 只出现在 v1 升级上来的历史数据里，首次登录会被认领。
     */
    val ownerId: String? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
) {
    val hasPriority: Boolean
        get() = priority in 1..3
}

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    /** ARGB 颜色值 */
    val color: Int,
    val sortOrder: Int = 0,
    val ownerId: String? = null
)
