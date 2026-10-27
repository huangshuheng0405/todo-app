package com.huangshuheng.todo.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 数据库迁移登记处。
 *
 * 当前版本 v2：给 tasks / projects 加上 ownerId，用于按账号隔离数据。
 *
 * **以后再改表结构，务必在这里补一条 Migration 并升 version，不要直接删库**，
 * 否则老用户升级会直接崩（或者数据全丢）。
 * schema 快照会导出到 `app/schemas/`，可以用 androidx.room:room-testing 的
 * MigrationTestHelper 做升级校验。
 */
object TodoMigrations {

    /**
     * v1 → v2：新增 ownerId。
     * 有意为可空且不带 DEFAULT，这样历史数据保持 NULL，
     * 由首次登录时的 claimUnscopedXxx 认领给当前账号，用户数据不会凭空消失。
     */
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE tasks ADD COLUMN ownerId TEXT")
            db.execSQL("ALTER TABLE projects ADD COLUMN ownerId TEXT")
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2)
}
