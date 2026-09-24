package com.example.quickbill.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quickbill.data.db.AppDatabase
import com.example.quickbill.data.model.Bill
import com.example.quickbill.data.model.BillItem
import com.example.quickbill.data.model.Item
import com.example.quickbill.data.model.ShopProfile
import com.example.quickbill.data.repository.QuickBillRepository
import com.example.quickbill.util.AppLanguage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface UiMessage {
    data class Success(val message: String) : UiMessage
    data class Error(val message: String) : UiMessage
}

@OptIn(ExperimentalCoroutinesApi::class)
class QuickBillViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = QuickBillRepository(db)

    private val _currentShop = MutableStateFlow<ShopProfile?>(null)
    val currentShop: StateFlow<ShopProfile?> = _currentShop.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UiMessage>()
    val uiEvents = _uiEvents.asSharedFlow()

    private val _previewBill = MutableStateFlow<Bill?>(null)
    val previewBill: StateFlow<Bill?> = _previewBill.asStateFlow()

    // Sales filter states
    private val todayDateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    private val currentMonthKey = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

    private val _selectedSalesDate = MutableStateFlow(todayDateKey)
    val selectedSalesDate: StateFlow<String> = _selectedSalesDate.asStateFlow()

    private val _selectedSalesMonth = MutableStateFlow(currentMonthKey)
    val selectedSalesMonth: StateFlow<String> = _selectedSalesMonth.asStateFlow()

    // Reactive streams mapped to current shop
    val allItems: StateFlow<List<Item>> = _currentShop
        .flatMapLatest { shop ->
            if (shop != null) repository.getAllItems(shop.phone)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockItems: StateFlow<List<Item>> = _currentShop
        .flatMapLatest { shop ->
            if (shop != null) repository.getLowStockItems(shop.phone)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentBills: StateFlow<List<Bill>> = _currentShop
        .flatMapLatest { shop ->
            if (shop != null) repository.getRecentBills(shop.phone, limit = 3)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBills: StateFlow<List<Bill>> = _currentShop
        .flatMapLatest { shop ->
            if (shop != null) repository.getAllBills(shop.phone)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayBills: StateFlow<List<Bill>> = _currentShop
        .flatMapLatest { shop ->
            if (shop != null) repository.getBillsByDate(shop.phone, todayDateKey)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthBills: StateFlow<List<Bill>> = _currentShop
        .flatMapLatest { shop ->
            if (shop != null) repository.getBillsByMonth(shop.phone, currentMonthKey)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val salesDateBills: StateFlow<List<Bill>> = kotlinx.coroutines.flow.combine(
        _currentShop,
        _selectedSalesDate
    ) { shop, dateKey ->
        Pair(shop, dateKey)
    }.flatMapLatest { (shop, dateKey) ->
        if (shop != null) repository.getBillsByDate(shop.phone, dateKey)
        else kotlinx.coroutines.flow.flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val salesMonthBills: StateFlow<List<Bill>> = kotlinx.coroutines.flow.combine(
        _currentShop,
        _selectedSalesMonth
    ) { shop, monthKey ->
        Pair(shop, monthKey)
    }.flatMapLatest { (shop, monthKey) ->
        if (shop != null) repository.getBillsByMonth(shop.phone, monthKey)
        else kotlinx.coroutines.flow.flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayTotal: StateFlow<Double> = todayBills
        .map { bills -> bills.sumOf { it.total } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthTotal: StateFlow<Double> = monthBills
        .map { bills -> bills.sumOf { it.total } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun setLanguage(language: AppLanguage) {
        _language.value = language
    }

    fun setSalesDate(dateKey: String) {
        _selectedSalesDate.value = dateKey
    }

    fun setSalesMonth(monthKey: String) {
        _selectedSalesMonth.value = monthKey
    }

    fun setPreviewBill(bill: Bill?) {
        _previewBill.value = bill
    }

    fun login(phone: String, pin: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val cleanPhone = phone.trim()
            val shop = repository.getShopByPhone(cleanPhone)
            if (shop != null && shop.passwordHash == pin.trim()) {
                _currentShop.value = shop
                _uiEvents.emit(UiMessage.Success("Welcome back, ${shop.shopName}!"))
                onComplete(true)
            } else {
                _uiEvents.emit(UiMessage.Error("Invalid phone number or PIN"))
                onComplete(false)
            }
        }
    }

    fun signup(
        phone: String,
        pin: String,
        shopName: String,
        onComplete: (Boolean) -> Unit
    ) {
        signup(phone, pin, shopName, "", "", "", onComplete)
    }

    fun signup(
        phone: String,
        pin: String,
        shopName: String,
        ownerName: String,
        address: String,
        gstin: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val cleanPhone = phone.trim()
            val cleanName = shopName.trim()
            if (cleanPhone.length < 10) {
                _uiEvents.emit(UiMessage.Error("Please enter a valid 10-digit phone number"))
                onComplete(false)
                return@launch
            }
            if (cleanName.isBlank()) {
                _uiEvents.emit(UiMessage.Error("Shop name is required"))
                onComplete(false)
                return@launch
            }
            val existing = repository.getShopByPhone(cleanPhone)
            if (existing != null) {
                _uiEvents.emit(UiMessage.Error("Shop with phone $cleanPhone already exists. Please log in."))
                onComplete(false)
                return@launch
            }

            val newShop = ShopProfile(
                phone = cleanPhone,
                shopName = cleanName,
                passwordHash = pin.trim(),
                ownerName = ownerName.trim(),
                address = address.trim(),
                gstin = gstin.trim()
            )
            repository.saveShop(newShop)
            _currentShop.value = newShop
            _uiEvents.emit(UiMessage.Success("Shop created successfully!"))
            onComplete(true)
        }
    }

    fun logout() {
        _currentShop.value = null
    }

    suspend fun getNextBillNo(): Long {
        val shop = _currentShop.value ?: return 1L
        return repository.getNextBillNo(shop.phone)
    }

    suspend fun getNextSuggestedItemNo(): Int {
        val shop = _currentShop.value ?: return 1
        return repository.getNextSuggestedItemNo(shop.phone)
    }

    fun createBill(
        customerName: String,
        customerPhone: String,
        items: List<BillItem>,
        onSuccess: (Bill) -> Unit,
        onError: (String) -> Unit
    ) {
        val shop = _currentShop.value
        if (shop == null) {
            onError("Please log in first")
            return
        }
        if (items.isEmpty()) {
            onError("Please add at least one item to the bill")
            return
        }

        viewModelScope.launch {
            val result = repository.saveBill(
                shopPhone = shop.phone,
                customerName = customerName,
                customerPhone = customerPhone,
                items = items
            )
            result.fold(
                onSuccess = { bill ->
                    _previewBill.value = bill
                    _uiEvents.emit(UiMessage.Success("Bill #${bill.billNo} generated successfully!"))
                    onSuccess(bill)
                },
                onFailure = { error ->
                    val msg = error.message ?: "Failed to generate bill"
                    _uiEvents.emit(UiMessage.Error(msg))
                    onError(msg)
                }
            )
        }
    }

    fun deleteBill(billId: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteBill(billId)
            result.fold(
                onSuccess = {
                    if (_previewBill.value?.billId == billId) {
                        _previewBill.value = null
                    }
                    _uiEvents.emit(UiMessage.Success("Bill deleted and stock restored!"))
                    onDeleted()
                },
                onFailure = { error ->
                    val msg = error.message ?: "Failed to delete bill"
                    _uiEvents.emit(UiMessage.Error(msg))
                }
            )
        }
    }

    fun saveItem(
        itemNo: Int,
        name: String,
        price: Double,
        quantity: Double,
        lowStockLimit: Double = 10.0,
        unit: String = "pcs",
        category: String = "General",
        isEdit: Boolean = false,
        originalItemNo: Int? = null,
        onComplete: (Boolean) -> Unit
    ) {
        val shop = _currentShop.value
        if (shop == null) {
            onComplete(false)
            return
        }

        viewModelScope.launch {
            val result = repository.saveItem(
                shopPhone = shop.phone,
                itemNo = itemNo,
                name = name,
                price = price,
                quantity = quantity,
                lowStockLimit = lowStockLimit,
                unit = unit,
                category = category,
                isEdit = isEdit,
                originalItemNo = originalItemNo
            )
            result.fold(
                onSuccess = {
                    _uiEvents.emit(UiMessage.Success(if (isEdit) "Item updated" else "Item added to inventory"))
                    onComplete(true)
                },
                onFailure = { error ->
                    val msg = error.message ?: "Could not save item"
                    _uiEvents.emit(UiMessage.Error(msg))
                    onComplete(false)
                }
            )
        }
    }

    fun deleteItem(itemNo: Int) {
        val shop = _currentShop.value ?: return
        viewModelScope.launch {
            val result = repository.deleteItem(shop.phone, itemNo)
            result.fold(
                onSuccess = {
                    _uiEvents.emit(UiMessage.Success("Item #$itemNo removed from inventory"))
                },
                onFailure = { error ->
                    _uiEvents.emit(UiMessage.Error(error.message ?: "Failed to delete item"))
                }
            )
        }
    }
}
