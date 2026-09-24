package com.example.quickbill.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.quickbill.data.model.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE shopPhone = :shopPhone ORDER BY itemNo ASC")
    fun getAllItems(shopPhone: String): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE shopPhone = :shopPhone AND quantity <= lowStockLimit ORDER BY quantity ASC")
    fun getLowStockItems(shopPhone: String): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE shopPhone = :shopPhone AND itemNo = :itemNo LIMIT 1")
    suspend fun getItemByNo(shopPhone: String, itemNo: Int): Item?

    @Query("SELECT * FROM items WHERE shopPhone = :shopPhone AND nameLower = :nameLower LIMIT 1")
    suspend fun getItemByNameLower(shopPhone: String, nameLower: String): Item?

    @Query("SELECT MAX(itemNo) FROM items WHERE shopPhone = :shopPhone")
    suspend fun getMaxItemNo(shopPhone: String): Int?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertItem(item: Item)

    @Update
    suspend fun updateItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    @Query("DELETE FROM items WHERE shopPhone = :shopPhone AND itemNo = :itemNo")
    suspend fun deleteByNo(shopPhone: String, itemNo: Int)

    @Query("UPDATE items SET quantity = quantity - :soldQty WHERE shopPhone = :shopPhone AND itemNo = :itemNo")
    suspend fun deductStock(shopPhone: String, itemNo: Int, soldQty: Double)

    @Query("UPDATE items SET quantity = quantity + :restoredQty WHERE shopPhone = :shopPhone AND itemNo = :itemNo")
    suspend fun restoreStock(shopPhone: String, itemNo: Int, restoredQty: Double)
}
