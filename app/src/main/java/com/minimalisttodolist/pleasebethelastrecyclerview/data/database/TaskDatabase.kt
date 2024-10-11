package com.minimalisttodolist.pleasebethelastrecyclerview.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.DeletedTask
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.Task

@Database(
    entities = [Task::class, DeletedTask::class],
    version = 2
)
abstract class TaskDatabase: RoomDatabase() {

    abstract val dao: TaskDao
}