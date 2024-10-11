package com.minimalisttodolist.pleasebethelastrecyclerview.data.database

import androidx.room.*
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.DeletedTask
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // Will break when Int exceeds limit
    @Upsert
    suspend fun upsertTask(task: Task): Long

    @Query("SELECT * FROM task WHERE id = :id")
    suspend fun getTaskById(id: Int): Task?

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("""
        SELECT * FROM task 
        ORDER BY priority DESC, 
                 CASE WHEN dueDate IS NULL THEN 1 ELSE 0 END, 
                 dueDate ASC
    """)
    fun getTasksSortedByPriority(): Flow<List<Task>>

    @Query("""
        SELECT * FROM task 
        ORDER BY CASE WHEN dueDate IS NULL THEN 1 ELSE 0 END, 
                 dueDate ASC
    """)
    fun getTasksSortedByDueDate(): Flow<List<Task>>

    @Query("SELECT * FROM task ORDER BY title ASC")
    fun getTasksOrderedAlphabetically(): Flow<List<Task>>

    @Query("SELECT * FROM task ORDER BY title DESC")
    fun getTasksOrderedAlphabeticallyRev(): Flow<List<Task>>

    // Deleted Tasks
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeletedTask(deletedTask: DeletedTask)

    @Delete
    suspend fun deleteDeletedTask(deletedTask: DeletedTask)

    @Query("DELETE FROM deleted_tasks")
    suspend fun deleteAllDeletedTasks()

    @Query("SELECT * FROM deleted_tasks ORDER BY deletedAt DESC")
    fun getDeletedTasks(): Flow<List<DeletedTask>>
}