package com.huangshuheng.todo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * 所有读操作都按 ownerId 过滤，保证账号之间数据不串。
 * 接后端后这些查询可以直接换成按 token 请求的远端接口。
 */
@Dao
interface TodoDao {

    @Query("SELECT * FROM tasks WHERE ownerId = :ownerId ORDER BY sortOrder ASC, createdAt DESC")
    fun observeTasks(ownerId: String): Flow<List<Task>>

    @Query("SELECT * FROM projects WHERE ownerId = :ownerId ORDER BY sortOrder ASC, id ASC")
    fun observeProjects(ownerId: String): Flow<List<Project>>

    @Insert
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id AND ownerId = :ownerId")
    suspend fun deleteTask(id: Long, ownerId: String)

    @Query("UPDATE tasks SET isCompleted = :done, completedAt = :completedAt WHERE id = :id AND ownerId = :ownerId")
    suspend fun setCompleted(id: Long, ownerId: String, done: Boolean, completedAt: Long?)

    @Query("DELETE FROM tasks WHERE isCompleted = 1 AND ownerId = :ownerId")
    suspend fun deleteCompleted(ownerId: String)

    @Insert
    suspend fun insertProject(project: Project): Long

    /** v1 升级上来的历史数据 ownerId 为空，首次登录时认领给当前账号 */
    @Query("UPDATE tasks SET ownerId = :ownerId WHERE ownerId IS NULL")
    suspend fun claimUnscopedTasks(ownerId: String)

    @Query("UPDATE projects SET ownerId = :ownerId WHERE ownerId IS NULL")
    suspend fun claimUnscopedProjects(ownerId: String)
}
