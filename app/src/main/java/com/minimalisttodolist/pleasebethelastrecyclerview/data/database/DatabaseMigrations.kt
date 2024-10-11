package com.minimalisttodolist.pleasebethelastrecyclerview.data.database

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create the new DeletedTask table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `deleted_tasks` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `priority` INTEGER NOT NULL,
                `note` TEXT NOT NULL,
                `dueDate` INTEGER,
                `recurrenceType` TEXT NOT NULL DEFAULT 'NONE',
                `nextDueDate` INTEGER,
                `deletedAt` INTEGER NOT NULL
            )
        """.trimIndent())

        // Create the new Task table with the updated schema
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `Task_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `priority` INTEGER NOT NULL,
                `note` TEXT NOT NULL,
                `dueDate` INTEGER,
                `recurrenceType` TEXT NOT NULL DEFAULT 'NONE',
                `isCompleted` INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        // Copy data from the old Task table to the new Task table
        val cursor = db.query("SELECT id, taskName, note, date, time, priority FROM Task")
        val idIndex = cursor.getColumnIndex("id")
        val taskNameIndex = cursor.getColumnIndex("taskName")
        val noteIndex = cursor.getColumnIndex("note")
        val dateIndex = cursor.getColumnIndex("date")
        val timeIndex = cursor.getColumnIndex("time")
        val priorityIndex = cursor.getColumnIndex("priority")

        while (cursor.moveToNext()) {
            val id = cursor.getInt(idIndex)
            val title = cursor.getString(taskNameIndex)
            val note = cursor.getString(noteIndex)
            val date = cursor.getString(dateIndex)
            val time = cursor.getString(timeIndex)
            val priority = cursor.getInt(priorityIndex)

            // Handle empty or null date/time values
            val dueDate: Long? = if (date.isNullOrEmpty()) {
                null
            } else {
                try {
                    val localDate = LocalDate.ofEpochDay(date.toLong() / (24 * 60 * 60 * 1000))
                    val localTime = if (time.isNullOrEmpty()) LocalTime.MIDNIGHT else LocalTime.ofSecondOfDay(time.toLong() / 1000)
                    val localDateTime = LocalDateTime.of(localDate, localTime)
                    localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                } catch (e: NumberFormatException) {
                    null
                }
            }

            // Insert into the new Task table
            db.execSQL(
                """
                INSERT INTO Task_new (id, title, priority, note, dueDate, recurrenceType, isCompleted)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf(id, title, priority, note, dueDate, "NONE", 0)
            )
        }
        cursor.close()

        // Remove the old Task table
        db.execSQL("DROP TABLE Task")

        // Rename the new Task table to the old Task table name
        db.execSQL("ALTER TABLE Task_new RENAME TO Task")
    }
}
