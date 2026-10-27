package com.example.app2.data

import kotlinx.coroutines.flow.Flow

/**
 * 待办数据仓库。
 * 目前全部落本地 Room；接后端时把方法体换成网络请求即可，签名保持不变，
 * ownerId 参数届时可以由服务端从 token 里解析。
 */
class TodoRepository(private val dao: TodoDao) {

    fun observeTasks(ownerId: String): Flow<List<Task>> = dao.observeTasks(ownerId)

    fun observeProjects(ownerId: String): Flow<List<Project>> = dao.observeProjects(ownerId)

    suspend fun addTask(task: Task): Long = dao.insertTask(task)

    suspend fun updateTask(task: Task) = dao.updateTask(task)

    suspend fun deleteTask(id: Long, ownerId: String) = dao.deleteTask(id, ownerId)

    suspend fun setCompleted(id: Long, ownerId: String, done: Boolean) {
        dao.setCompleted(id, ownerId, done, if (done) System.currentTimeMillis() else null)
    }

    suspend fun deleteCompleted(ownerId: String) = dao.deleteCompleted(ownerId)

    suspend fun addProject(name: String, color: Int, ownerId: String): Long =
        dao.insertProject(Project(name = name, color = color, ownerId = ownerId))

    /**
     * 认领 v1 遗留的无主数据（ownerId 为空）。
     * 只在登录成功后调用，保证老用户升级后原有待办不会消失。
     */
    suspend fun claimLegacyData(ownerId: String) {
        dao.claimUnscopedTasks(ownerId)
        dao.claimUnscopedProjects(ownerId)
    }
}
