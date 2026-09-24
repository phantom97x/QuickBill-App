package com.example.quickbill.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quickbill.data.model.Bill
import com.example.quickbill.data.model.BillItem
import com.example.quickbill.data.model.Item
import com.example.quickbill.data.model.ShopProfile
import com.example.quickbill.ui.components.QuickBillGradientButton
import com.example.quickbill.util.AppLanguage
import com.example.quickbill.util.AppStrings
import com.example.quickbill.util.PdfInvoiceGenerator
import com.example.quickbill.util.PrintReceiptHelper
import com.example.ui.theme.HeaderGradient
import com.example.ui.theme.SuccessGradient
import java.util.Locale

@Composable
fun BillingScreen(
    allItems: List<Item>,
    currentShop: ShopProfile?,
    getNextBillNo: suspend () -> Long,
    onSaveBill: (String, String, List<BillItem>, (Bill) -> Unit, (String) -> Unit) -> Unit,
    onBillCreated: (Bill) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var nextBillNo by remember { mutableLongStateOf(1L) }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }

    // Item selection state
    var searchQuery by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<Item?>(null) }
    var itemQtyInput by remember { mutableStateOf("1") }
    var selectedUnit by remember { mutableStateOf("pcs") }
    var unitMenuExpanded by remember { mutableStateOf(false) }

    // Bill items list
    val billItems = remember { mutableStateListOf<BillItem>() }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val commonUnits = listOf("pcs", "kg", "gram", "meter", "feet", "inch", "packet", "box", "dozen")

    LaunchedEffect(Unit) {
        nextBillNo = getNextBillNo()
    }

    // Filter suggestions based on searchQuery
    val suggestions by remember(searchQuery, allItems) {
        derivedStateOf {
            val q = searchQuery.trim().lowercase(Locale.ROOT)
            if (q.isBlank()) emptyList()
            else {
                allItems.filter { item ->
                    item.itemNo.toString() == q || item.itemNo.toString().startsWith(q) ||
                            item.nameLower.contains(q)
                }.take(6)
            }
        }
    }

    val grandTotal by remember {
        derivedStateOf { billItems.sumOf { it.lineTotal } }
    }

    fun submitBill(onDoneAction: (Bill) -> Unit) {
        if (billItems.isEmpty()) {
            errorMessage = "Please add at least one item before finalizing bill"
            return
        }
        isSaving = true
        errorMessage = null
        onSaveBill(
            customerName,
            customerPhone,
            billItems.toList(),
            { createdBill ->
                isSaving = false
                onDoneAction(createdBill)
            },
            { error ->
                isSaving = false
                errorMessage = error
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("billing_screen_container"),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = if (billItems.isNotEmpty()) 180.dp else 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Bill Number & Top Action Row
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bill_number_banner"),
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = AppStrings.get("bill_no", language),
                                fontSize = 12.sp,
                                color = Color(0xFF1E40AF),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "#$nextBillNo",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E3A8A)
                            )
                        }

                        if (billItems.isNotEmpty()) {
                            // Quick Top "Done & Preview" button
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        submitBill { bill -> onBillCreated(bill) }
                                    }
                                    .testTag("btn_top_done_preview"),
                                color = Color(0xFF10B981)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Done (${billItems.size})",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2563EB))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "AUTO GENERATED",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Customer Details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Customer Details (${AppStrings.get("optional", language)})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text(AppStrings.get("customer_name", language)) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF64748B))
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_customer_name"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) customerPhone = it },
                            label = { Text(AppStrings.get("customer_phone", language)) },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF64748B))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_customer_phone"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )
                    }
                }
            }

            // Item Picker Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Add Item to Bill",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Live Search by item number or name
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { query ->
                                searchQuery = query
                                // Check if exact item number matched
                                val num = query.trim().toIntOrNull()
                                if (num != null) {
                                    val match = allItems.find { it.itemNo == num }
                                    if (match != null) {
                                        selectedItem = match
                                        selectedUnit = match.unit
                                    }
                                }
                            },
                            placeholder = { Text(AppStrings.get("search_item_placeholder", language), fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF2563EB))
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        selectedItem = null
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_search_item"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )

                        // Live Suggestions Dropdown
                        if (suggestions.isNotEmpty() && selectedItem == null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column {
                                    suggestions.forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedItem = item
                                                    searchQuery = "${item.itemNo} - ${item.name}"
                                                    selectedUnit = item.unit
                                                }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(26.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFFDBEAFE)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "${item.itemNo}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF1D4ED8)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = item.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF1E293B)
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "₹ ${String.format(Locale.US, "%.0f", item.price)}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF16A34A)
                                                )
                                                Text(
                                                    text = "Stock: ${item.quantity.toInt()} ${item.unit}",
                                                    fontSize = 11.sp,
                                                    color = if (item.isLowStock) Color(0xFFDC2626) else Color(0xFF64748B)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Selected Item Preview
                        selectedItem?.let { item ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFF0FDF4),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "#${item.itemNo} • ${item.name}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF166534)
                                        )
                                        Text(
                                            text = "Rate: ₹ ${String.format(Locale.US, "%.2f", item.price)} / ${item.unit}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF15803D)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (item.quantity <= 0) Color(0xFFFEE2E2) else Color(0xFFDCFCE7))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${AppStrings.get("available_qty", language)}: ${item.quantity.toInt()}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (item.quantity <= 0) Color(0xFFDC2626) else Color(0xFF166534)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quantity + Unit Dropdown + Add Item Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = itemQtyInput,
                                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) itemQtyInput = it },
                                label = { Text(AppStrings.get("quantity", language)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_item_quantity"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2563EB),
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                )
                            )

                            // Unit Selector Dropdown
                            Box(modifier = Modifier.weight(0.9f)) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { unitMenuExpanded = true }
                                        .testTag("unit_dropdown_selector"),
                                    color = Color.White
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = selectedUnit,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }

                                DropdownMenu(
                                    expanded = unitMenuExpanded,
                                    onDismissRequest = { unitMenuExpanded = false }
                                ) {
                                    commonUnits.forEach { unit ->
                                        DropdownMenuItem(
                                            text = { Text(unit) },
                                            onClick = {
                                                selectedUnit = unit
                                                unitMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Add Item Button
                            QuickBillGradientButton(
                                text = AppStrings.get("add_item", language),
                                onClick = {
                                    val item = selectedItem
                                    val qty = itemQtyInput.toDoubleOrNull()
                                    if (item == null) {
                                        errorMessage = "Please select an item first"
                                        return@QuickBillGradientButton
                                    }
                                    if (qty == null || qty <= 0) {
                                        errorMessage = "Quantity must be greater than 0"
                                        return@QuickBillGradientButton
                                    }
                                    if (qty > item.quantity) {
                                        errorMessage = "Only ${item.quantity.toInt()} available in stock"
                                        return@QuickBillGradientButton
                                    }

                                    val existingIndex = billItems.indexOfFirst { it.itemNo == item.itemNo }
                                    if (existingIndex >= 0) {
                                        val currentQty = billItems[existingIndex].qty
                                        val newQty = currentQty + qty
                                        if (newQty > item.quantity) {
                                            errorMessage = "Cannot exceed available stock of ${item.quantity.toInt()}"
                                            return@QuickBillGradientButton
                                        }
                                        billItems[existingIndex] = billItems[existingIndex].copy(
                                            qty = newQty,
                                            lineTotal = newQty * item.price
                                        )
                                    } else {
                                        billItems.add(
                                            BillItem(
                                                itemNo = item.itemNo,
                                                name = item.name,
                                                qty = qty,
                                                unit = selectedUnit,
                                                price = item.price,
                                                lineTotal = qty * item.price
                                            )
                                        )
                                    }

                                    selectedItem = null
                                    searchQuery = ""
                                    itemQtyInput = "1"
                                    errorMessage = null
                                },
                                gradient = HeaderGradient,
                                icon = Icons.Default.Add,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(54.dp),
                                testTag = "btn_add_item_to_bill"
                            )
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = Color(0xFFDC2626),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Bill Items Table
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Items in Bill (${billItems.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    if (billItems.isNotEmpty()) {
                        Text(
                            text = "Clear All",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFDC2626),
                            modifier = Modifier
                                .clickable { billItems.clear() }
                                .padding(4.dp)
                        )
                    }
                }
            }

            if (billItems.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White
                    ) {
                        Box(
                            modifier = Modifier.padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No items added yet. Type an item number above (e.g. 1).",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(billItems) { index, item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bill_item_row_$index"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFEFF6FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${item.itemNo}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF1D4ED8)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = item.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "${if (item.qty % 1.0 == 0.0) item.qty.toInt() else item.qty} ${item.unit} × ₹${item.price.toInt()}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₹ ${String.format(Locale.US, "%.0f", item.lineTotal)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF16A34A)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { billItems.removeAt(index) },
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFEE2E2))
                                        .testTag("btn_remove_item_$index")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // DOCKED / FLOATING BOTTOM ACTION BAR: ALWAYS VISIBLE ON SCREEN!
        AnimatedVisibility(
            visible = billItems.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        spotColor = Color(0x40000000)
                    ),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Total & Item Summary Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Grand Total (${billItems.size} items)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "₹ ${String.format(Locale.US, "%,.2f", grandTotal)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF16A34A)
                            )
                        }

                        // Quick Print & Share PDF buttons right here on Billing screen!
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Quick Print
                            IconButton(
                                onClick = {
                                    submitBill { bill ->
                                        onBillCreated(bill)
                                        currentShop?.let { PrintReceiptHelper.printBill(context, bill, it) }
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEFF6FF))
                                    .testTag("btn_docked_quick_print")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Print,
                                    contentDescription = "Print",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Quick PDF Share
                            IconButton(
                                onClick = {
                                    submitBill { bill ->
                                        onBillCreated(bill)
                                        currentShop?.let { PdfInvoiceGenerator.generateAndSharePdf(context, bill, it) }
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEFF6FF))
                                    .testTag("btn_docked_quick_pdf")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share PDF",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Primary Big Button: "Done - View & Print Bill"
                    QuickBillGradientButton(
                        text = "Done & Preview Bill →",
                        onClick = {
                            submitBill { bill -> onBillCreated(bill) }
                        },
                        gradient = SuccessGradient,
                        icon = Icons.Default.Check,
                        loading = isSaving,
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        testTag = "btn_docked_done_preview"
                    )
                }
            }
        }
    }
}
