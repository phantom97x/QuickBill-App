package com.example.quickbill.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.quickbill.data.model.Bill
import com.example.quickbill.data.model.Item
import com.example.quickbill.data.model.MetaCounter
import com.example.quickbill.data.model.ShopProfile

@Database(
    entities = [
        ShopProfile::class,
        Item::class,
        Bill::class,
        MetaCounter::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shopDao(): ShopDao
    abstract fun itemDao(): ItemDao
    abstract fun billDao(): BillDao
    abstract fun metaDao(): MetaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quickbill_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
