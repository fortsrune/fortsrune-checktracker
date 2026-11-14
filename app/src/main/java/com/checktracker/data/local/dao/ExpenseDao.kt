package com.checktracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.checktracker.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    fun observeBetween(from: Long, to: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    suspend fun getAllOnce(): List<ExpenseEntity>

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Delete
    suspend fun delete(expense: ExpenseEntity)
}
