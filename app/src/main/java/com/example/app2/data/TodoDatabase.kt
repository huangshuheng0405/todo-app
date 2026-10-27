package com.example.app2.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Task::class, Project::class],
    version = 2,
    exportSchema = true
)
abstract class TodoDatabase : RoomDatabase() {

    abstract fun dao(): TodoDao

    companion object {
        fun build(context: Context): TodoDatabase =
            Room.databaseBuilder(context, TodoDatabase::class.java, "todoist.db")
                .addMigrations(*TodoMigrations.ALL)
                .build()
    }
}
