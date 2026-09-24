package com.example.quickbill.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.quickbill.data.model.ShopProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {
    @Query("SELECT * FROM shops WHERE phone = :phone LIMIT 1")
    suspend fun getShopByPhone(phone: String): ShopProfile?

    @Query("SELECT * FROM shops ORDER BY createdAt DESC")
    fun getAllShops(): Flow<List<ShopProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShop(shop: ShopProfile)

    @Update
    suspend fun updateShop(shop: ShopProfile)
}
