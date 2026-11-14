package com.checktracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.checktracker.data.local.dao.AccountDao
import com.checktracker.data.local.dao.CategoryDao
import com.checktracker.data.local.dao.ExpenseDao
import com.checktracker.data.local.entity.AccountEntity
import com.checktracker.data.local.entity.CategoryEntity
import com.checktracker.data.local.entity.ExpenseEntity

@Database(
    entities = [ExpenseEntity::class, AccountEntity::class, CategoryEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "checktracker.db")
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    // При первом создании базы засеиваем стандартные категории и стартовый счёт
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        db.execSQL(
                            "INSERT INTO categories (name, emoji) VALUES " +
                                "('Продукты','🛒'),('Кафе и рестораны','☕'),('Транспорт','🚌')," +
                                "('Дом и ЖКХ','🏠'),('Здоровье','💊'),('Развлечения','🎬'),('Другое','📦')",
                        )
                        db.execSQL(
                            "INSERT INTO accounts (name, initialBalanceMinor, currency) VALUES " +
                                "('Наличные BYN', 0, 'BYN')",
                        )
                    }
                })
                .build()
    }
}
