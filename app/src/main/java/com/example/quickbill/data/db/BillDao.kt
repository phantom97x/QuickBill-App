package com.example.quickbill.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quickbill.data.model.Bill
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Query("SELECT * FROM bills WHERE shopPhone = :shopPhone ORDER BY createdAt DESC")
    fun getAllBills(shopPhone: String): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE shopPhone = :shopPhone ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentBills(shopPhone: String, limit: Int = 3): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE shopPhone = :shopPhone AND dateKey = :dateKey ORDER BY createdAt DESC")
    fun getBillsByDate(shopPhone: String, dateKey: String): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE shopPhone = :shopPhone AND monthKey = :monthKey ORDER BY createdAt DESC")
    fun getBillsByMonth(shopPhone: String, monthKey: String): Flow<List<Bill>>

    @Query("SELECT COUNT(*) FROM bills WHERE shopPhone = :shopPhone")
    suspend fun getBillCount(shopPhone: String): Int

    @Query("SELECT * FROM bills WHERE billId = :billId LIMIT 1")
    suspend fun getBillById(billId: String): Bill?

    @Query("SELECT * FROM bills WHERE shopPhone = :shopPhone AND billNo = :billNo LIMIT 1")
    suspend fun getBillByNo(shopPhone: String, billNo: Long): Bill?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill)

    @Delete
    suspend fun deleteBill(bill: Bill)

    @Query("DELETE FROM bills WHERE billId = :billId")
    suspend fun deleteBillById(billId: String)
}
