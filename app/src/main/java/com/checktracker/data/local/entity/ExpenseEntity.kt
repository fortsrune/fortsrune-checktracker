package com.checktracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "expenses", indices = [Index("timestamp")])
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val merchant: String,
    val amountMinor: Long,
    val currency: String,
    val category: String,
    val timestamp: Long,
)
