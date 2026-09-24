package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.quickbill.data.db.AppDatabase
import com.example.quickbill.data.model.BillItem
import com.example.quickbill.data.model.Item
import com.example.quickbill.data.repository.QuickBillRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: QuickBillRepository
    private val testShopPhone = "9876543210"

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = QuickBillRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun verifyAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("QuickBill", appName)
    }

    @Test
    fun testAddItemAndCheckUniqueItemNumber() = runBlocking {
        // 1. Add Item #1
        val item1Result = repository.saveItem(
            shopPhone = testShopPhone,
            itemNo = 1,
            name = "1 inch PVC Pipe",
            price = 120.0,
            quantity = 50.0,
            lowStockLimit = 10.0,
            unit = "meter"
        )
        assertTrue(item1Result.isSuccess)

        // 2. Attempt duplicate Item #1 -> must fail
        val duplicateResult = repository.saveItem(
            shopPhone = testShopPhone,
            itemNo = 1,
            name = "Different Pipe",
            price = 150.0,
            quantity = 20.0
        )
        assertTrue(duplicateResult.isFailure)

        // 3. Attempt duplicate Item Name (case-insensitive) -> must fail
        val duplicateNameResult = repository.saveItem(
            shopPhone = testShopPhone,
            itemNo = 2,
            name = "  1 inch pvc pipe  ",
            price = 130.0,
            quantity = 20.0
        )
        assertTrue(duplicateNameResult.isFailure)
    }

    @Test
    fun testCreateBillAssignsSequenceAndDeductsStock() = runBlocking {
        // Setup initial inventory
        repository.saveItem(
            shopPhone = testShopPhone,
            itemNo = 1,
            name = "1 inch PVC Pipe",
            price = 100.0,
            quantity = 20.0
        )

        val billItems = listOf(
            BillItem(
                itemNo = 1,
                name = "1 inch PVC Pipe",
                qty = 5.0,
                unit = "meter",
                price = 100.0,
                lineTotal = 500.0
            )
        )

        // Create first bill
        val billResult = repository.saveBill(
            shopPhone = testShopPhone,
            customerName = "Raju Farmer",
            customerPhone = "9988776655",
            items = billItems
        )

        assertTrue(billResult.isSuccess)
        val bill = billResult.getOrThrow()
        assertEquals(1L, bill.billNo)
        assertEquals(500.0, bill.total, 0.01)

        // Verify stock was decremented from 20 to 15
        val updatedItem = repository.getItemByNo(testShopPhone, 1)
        assertNotNull(updatedItem)
        assertEquals(15.0, updatedItem!!.quantity, 0.01)

        // Delete bill and verify stock is restored from 15 to 20
        val deleteResult = repository.deleteBill(bill.billId)
        assertTrue(deleteResult.isSuccess)

        val restoredItem = repository.getItemByNo(testShopPhone, 1)
        assertNotNull(restoredItem)
        assertEquals(20.0, restoredItem!!.quantity, 0.01)
    }

    @Test
    fun testMultipleShopsHaveIndependentItem1() = runBlocking {
        val shopA = "1111111111"
        val shopB = "2222222222"

        val resA = repository.saveItem(shopA, 1, "Pipe A", 100.0, 10.0)
        assertTrue(resA.isSuccess)

        val resB = repository.saveItem(shopB, 1, "Pipe B", 200.0, 20.0)
        assertTrue(resB.isSuccess)

        val itemA = repository.getItemByNo(shopA, 1)
        val itemB = repository.getItemByNo(shopB, 1)

        assertEquals("Pipe A", itemA?.name)
        assertEquals("Pipe B", itemB?.name)
    }
}
