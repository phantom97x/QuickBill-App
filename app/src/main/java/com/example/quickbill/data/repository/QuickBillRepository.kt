package com.example.quickbill.data.repository

import androidx.room.withTransaction
import com.example.quickbill.data.db.AppDatabase
import com.example.quickbill.data.model.Bill
import com.example.quickbill.data.model.BillItem
import com.example.quickbill.data.model.Item
import com.example.quickbill.data.model.MetaCounter
import com.example.quickbill.data.model.ShopProfile
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class QuickBillRepository(private val db: AppDatabase) {
    private val itemDao = db.itemDao()
    private val billDao = db.billDao()
    private val shopDao = db.shopDao()
    private val metaDao = db.metaDao()

    fun getAllItems(shopPhone: String): Flow<List<Item>> = itemDao.getAllItems(shopPhone)

    fun getLowStockItems(shopPhone: String): Flow<List<Item>> = itemDao.getLowStockItems(shopPhone)

    fun getAllBills(shopPhone: String): Flow<List<Bill>> = billDao.getAllBills(shopPhone)

    fun getRecentBills(shopPhone: String, limit: Int = 3): Flow<List<Bill>> =
        billDao.getRecentBills(shopPhone, limit)

    fun getBillsByDate(shopPhone: String, dateKey: String): Flow<List<Bill>> =
        billDao.getBillsByDate(shopPhone, dateKey)

    fun getBillsByMonth(shopPhone: String, monthKey: String): Flow<List<Bill>> =
        billDao.getBillsByMonth(shopPhone, monthKey)

    suspend fun getBillById(billId: String): Bill? = billDao.getBillById(billId)

    suspend fun getItemByNo(shopPhone: String, itemNo: Int): Item? =
        itemDao.getItemByNo(shopPhone, itemNo)

    suspend fun getNextSuggestedItemNo(shopPhone: String): Int {
        val max = itemDao.getMaxItemNo(shopPhone) ?: 0
        return max + 1
    }

    suspend fun getNextBillNo(shopPhone: String): Long {
        val counter = metaDao.getCounter("billSeq_$shopPhone")
        return (counter?.counterValue ?: 0L) + 1L
    }

    suspend fun saveBill(
        shopPhone: String,
        customerName: String,
        customerPhone: String,
        items: List<BillItem>
    ): Result<Bill> {
        if (items.isEmpty()) {
            return Result.failure(IllegalArgumentException("Bill must contain at least one item"))
        }

        return try {
            val bill = db.withTransaction {
                // 1. Verify stock availability for all items
                for (item in items) {
                    val dbItem = itemDao.getItemByNo(shopPhone, item.itemNo)
                        ?: throw IllegalStateException("Item #${item.itemNo} not found in inventory")
                    if (item.qty > dbItem.quantity) {
                        throw IllegalStateException(
                            "Cannot sell ${item.qty} ${item.unit}. Only ${dbItem.quantity} ${dbItem.unit} available for ${dbItem.name}!"
                        )
                    }
                }

                // 2. Compute sequence atomically
                val counterId = "billSeq_$shopPhone"
                val existing = metaDao.getCounter(counterId)
                val nextSeq = (existing?.counterValue ?: 0L) + 1L
                metaDao.setCounter(MetaCounter(id = counterId, counterValue = nextSeq))

                val now = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val monthFormat = SimpleDateFormat("yyyy-MM", Locale.US)
                val dateKey = dateFormat.format(Date(now))
                val monthKey = monthFormat.format(Date(now))

                val total = items.sumOf { it.lineTotal }
                val billId = UUID.randomUUID().toString()

                val newBill = Bill(
                    billId = billId,
                    shopPhone = shopPhone,
                    billNo = nextSeq,
                    customerName = customerName.trim(),
                    customerPhone = customerPhone.trim(),
                    items = items,
                    total = total,
                    createdAt = now,
                    dateKey = dateKey,
                    monthKey = monthKey
                )

                // 3. Deduct stock for all items
                for (item in items) {
                    itemDao.deductStock(shopPhone, item.itemNo, item.qty)
                }

                // 4. Save bill
                billDao.insertBill(newBill)

                newBill
            }
            Result.success(bill)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBill(billId: String): Result<Unit> {
        return try {
            db.withTransaction {
                val bill = billDao.getBillById(billId)
                    ?: throw IllegalStateException("Bill not found")

                // Restore stock for all items in the deleted bill
                for (item in bill.items) {
                    itemDao.restoreStock(bill.shopPhone, item.itemNo, item.qty)
                }

                // Delete the bill
                billDao.deleteBill(bill)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveItem(
        shopPhone: String,
        itemNo: Int,
        name: String,
        price: Double,
        quantity: Double,
        lowStockLimit: Double = 10.0,
        unit: String = "pcs",
        category: String = "General",
        isEdit: Boolean = false,
        originalItemNo: Int? = null
    ): Result<Item> {
        val trimmedName = name.trim()
        val nameLower = trimmedName.lowercase(Locale.ROOT)

        if (itemNo <= 0) {
            return Result.failure(IllegalArgumentException("Item number must be greater than 0"))
        }
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Item name cannot be empty"))
        }
        if (price < 0) {
            return Result.failure(IllegalArgumentException("Price must be 0 or greater"))
        }
        if (quantity < 0) {
            return Result.failure(IllegalArgumentException("Quantity cannot be negative"))
        }

        return try {
            val item = db.withTransaction {
                // Check duplicate Item No
                val existingByNo = itemDao.getItemByNo(shopPhone, itemNo)
                if (existingByNo != null) {
                    if (!isEdit || (isEdit && originalItemNo != itemNo)) {
                        throw IllegalStateException("Item number $itemNo already exists. Choose a different number.")
                    }
                }

                // Check duplicate Item Name
                val existingByName = itemDao.getItemByNameLower(shopPhone, nameLower)
                if (existingByName != null) {
                    if (!isEdit || (isEdit && existingByName.itemNo != (originalItemNo ?: itemNo))) {
                        throw IllegalStateException("Item name '$trimmedName' already exists. Use a unique name.")
                    }
                }

                val newItem = Item(
                    itemNo = itemNo,
                    shopPhone = shopPhone,
                    name = trimmedName,
                    nameLower = nameLower,
                    price = price,
                    quantity = quantity,
                    lowStockLimit = lowStockLimit,
                    unit = unit.trim().ifEmpty { "pcs" },
                    category = category,
                    updatedAt = System.currentTimeMillis()
                )

                if (isEdit && originalItemNo != null && originalItemNo != itemNo) {
                    // Item number changed: remove old row and insert new row
                    itemDao.deleteByNo(shopPhone, originalItemNo)
                    itemDao.insertItem(newItem)
                } else if (isEdit) {
                    itemDao.updateItem(newItem)
                } else {
                    itemDao.insertItem(newItem)
                }

                newItem
            }
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteItem(shopPhone: String, itemNo: Int): Result<Unit> {
        return try {
            itemDao.deleteByNo(shopPhone, itemNo)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getShopByPhone(phone: String): ShopProfile? = shopDao.getShopByPhone(phone)

    suspend fun saveShop(shop: ShopProfile) {
        shopDao.insertShop(shop)
    }
}
