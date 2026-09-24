package com.example.quickbill.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quickbill.data.model.Item
import com.example.quickbill.ui.components.QuickBillGradientButton
import com.example.quickbill.util.AppLanguage
import com.example.quickbill.util.AppStrings
import com.example.ui.theme.HeaderGradient
import com.example.ui.theme.HeroAddBillGradient
import com.example.ui.theme.SuccessGradient
import java.util.Locale

@Composable
fun StockScreen(
    items: List<Item>,
    onSaveItem: (Int, String, Double, Double, Double, String, String, Boolean, Int?, (Boolean) -> Unit) -> Unit,
    onDeleteItem: (Int) -> Unit,
    getNextSuggestedItemNo: suspend () -> Int,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<Item?>(null) }
    var itemToDelete by remember { mutableStateOf<Item?>(null) }

    val filteredItems by remember(searchQuery, items) {
        derivedStateOf {
            val q = searchQuery.trim().lowercase(Locale.ROOT)
            if (q.isBlank()) items
            else items.filter { it.itemNo.toString().contains(q) || it.nameLower.contains(q) }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("stock_screen_container"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Stats & Search Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = AppStrings.get("stock", language) + " (${items.size} items)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        val lowCount = items.count { it.isLowStock }
                        if (lowCount > 0) {
                            Text(
                                text = "⚠️ $lowCount items in low stock",
                                fontSize = 12.sp,
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    QuickBillGradientButton(
                        text = "+ Add Item",
                        onClick = {
                            itemToEdit = null
                            showAddEditDialog = true
                        },
                        gradient = HeroAddBillGradient,
                        icon = Icons.Default.Add,
                        modifier = Modifier.height(44.dp),
                        testTag = "btn_add_item_top"
                    )
                }
            }

            // Search Box
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by Item No or Name...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF2563EB))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_stock"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )
            }

            // Table Header Bar
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("#", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), modifier = Modifier.width(30.dp))
                        Text(AppStrings.get("item_name", language), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), modifier = Modifier.weight(1f))
                        Text(AppStrings.get("price", language), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), modifier = Modifier.width(60.dp))
                        Text(AppStrings.get("available_qty", language), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), modifier = Modifier.width(65.dp))
                        Text("Action", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), modifier = Modifier.width(55.dp))
                    }
                }
            }

            // Item Rows
            if (filteredItems.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White
                    ) {
                        Box(
                            modifier = Modifier.padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No items match '$searchQuery'" else "Stock is empty. Add your first item!",
                                color = Color(0xFF64748B),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredItems, key = { it.itemNo }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stock_item_card_${item.itemNo}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isLowStock) Color(0xFFFFF1F2) else Color.White
                        ),
                        border = if (item.isLowStock) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3)) else null,
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Item No Badge
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (item.isLowStock) Color(0xFFFFE4E6) else Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${item.itemNo}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (item.isLowStock) Color(0xFFE11D48) else Color(0xFF1D4ED8)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Name
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0F172A)
                                    )
                                    if (item.isLowStock) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Low Stock",
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${item.unit} • min ${item.lowStockLimit.toInt()}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            // Price
                            Text(
                                text = "₹${item.price.toInt()}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A),
                                modifier = Modifier.width(60.dp)
                            )

                            // Available Qty
                            Text(
                                text = "${item.quantity.toInt()}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (item.isLowStock) Color(0xFFDC2626) else Color(0xFF0F172A),
                                modifier = Modifier.width(65.dp)
                            )

                            // Actions: Edit and Delete
                            Row(
                                modifier = Modifier.width(55.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        itemToEdit = item
                                        showAddEditDialog = true
                                    },
                                    modifier = Modifier
                                        .size(26.dp)
                                        .testTag("btn_edit_item_${item.itemNo}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { itemToDelete = item },
                                    modifier = Modifier
                                        .size(26.dp)
                                        .testTag("btn_delete_item_${item.itemNo}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
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

        // Add/Edit Item Dialog Modal
        if (showAddEditDialog) {
            AddEditItemDialog(
                item = itemToEdit,
                onDismiss = { showAddEditDialog = false },
                onSave = { itemNo, name, price, qty, limit, unit, onComplete ->
                    onSaveItem(
                        itemNo,
                        name,
                        price,
                        qty,
                        limit,
                        unit,
                        "General",
                        itemToEdit != null,
                        itemToEdit?.itemNo,
                        { success ->
                            onComplete(success)
                            if (success) showAddEditDialog = false
                        }
                    )
                },
                getNextSuggestedItemNo = getNextSuggestedItemNo,
                language = language
            )
        }

        // Delete Item Confirmation Dialog
        itemToDelete?.let { item ->
            AlertDialog(
                onDismissRequest = { itemToDelete = null },
                title = { Text("Delete Item #${item.itemNo}?") },
                text = { Text("Are you sure you want to remove '${item.name}' from your stock?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteItem(item.itemNo)
                            itemToDelete = null
                        }
                    ) {
                        Text("Delete", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun AddEditItemDialog(
    item: Item?,
    onDismiss: () -> Unit,
    onSave: (Int, String, Double, Double, Double, String, (Boolean) -> Unit) -> Unit,
    getNextSuggestedItemNo: suspend () -> Int,
    language: AppLanguage
) {
    val isEdit = item != null
    var itemNoStr by remember { mutableStateOf(item?.itemNo?.toString() ?: "") }
    var name by remember { mutableStateOf(item?.name ?: "") }
    var priceStr by remember { mutableStateOf(item?.price?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var qtyStr by remember { mutableStateOf(item?.quantity?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var limitStr by remember { mutableStateOf(item?.lowStockLimit?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "10") }
    var unit by remember { mutableStateOf(item?.unit ?: "pcs") }
    var unitMenuExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val commonUnits = listOf("pcs", "kg", "gram", "meter", "feet", "inch", "packet", "box", "dozen")

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!isEdit && itemNoStr.isBlank()) {
            itemNoStr = getNextSuggestedItemNo().toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) AppStrings.get("edit_item", language) else AppStrings.get("add_new_item", language),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Item Number
                OutlinedTextField(
                    value = itemNoStr,
                    onValueChange = { if (it.all { c -> c.isDigit() }) itemNoStr = it },
                    label = { Text(AppStrings.get("item_no", language) + " *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_dialog_item_no")
                )

                // Item Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(AppStrings.get("item_name", language) + " *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_dialog_item_name")
                )

                // Price & Quantity in a Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) priceStr = it },
                        label = { Text(AppStrings.get("price", language) + " (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_dialog_price")
                    )

                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) qtyStr = it },
                        label = { Text(AppStrings.get("quantity", language)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_dialog_qty")
                    )
                }

                // Unit & Low Stock Limit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { unitMenuExpanded = true },
                            color = Color.White
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Unit: $unit", fontSize = 13.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = unitMenuExpanded,
                            onDismissRequest = { unitMenuExpanded = false }
                        ) {
                            commonUnits.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u) },
                                    onClick = {
                                        unit = u
                                        unitMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = limitStr,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) limitStr = it },
                        label = { Text("Low Stock Alert") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            QuickBillGradientButton(
                text = AppStrings.get("save", language),
                onClick = {
                    val no = itemNoStr.toIntOrNull()
                    val p = priceStr.toDoubleOrNull()
                    val q = qtyStr.toDoubleOrNull()
                    val lim = limitStr.toDoubleOrNull() ?: 10.0

                    if (no == null || no <= 0) {
                        errorMessage = "Item number must be a valid positive integer"
                        return@QuickBillGradientButton
                    }
                    if (name.isBlank()) {
                        errorMessage = "Item name cannot be empty"
                        return@QuickBillGradientButton
                    }
                    if (p == null || p < 0) {
                        errorMessage = "Price must be a valid positive number"
                        return@QuickBillGradientButton
                    }
                    if (q == null || q < 0) {
                        errorMessage = "Quantity cannot be negative"
                        return@QuickBillGradientButton
                    }

                    isSaving = true
                    errorMessage = null
                    onSave(no, name, p, q, lim, unit) { success ->
                        isSaving = false
                        if (!success) {
                            errorMessage = "Item number or name already exists!"
                        }
                    }
                },
                gradient = SuccessGradient,
                loading = isSaving,
                enabled = !isSaving,
                testTag = "btn_save_item_dialog"
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.get("cancel", language), color = Color(0xFF64748B))
            }
        }
    )
}
