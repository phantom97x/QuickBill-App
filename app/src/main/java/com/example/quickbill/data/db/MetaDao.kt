package com.example.quickbill.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quickbill.data.model.MetaCounter

@Dao
interface MetaDao {
    @Query("SELECT * FROM meta_counters WHERE id = :id LIMIT 1")
    suspend fun getCounter(id: String): MetaCounter?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setCounter(counter: MetaCounter)
}
