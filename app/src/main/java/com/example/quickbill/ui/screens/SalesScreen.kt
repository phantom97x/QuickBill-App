package com.example.quickbill.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quickbill.data.model.Bill
import com.example.quickbill.util.AppLanguage
import com.example.quickbill.util.AppStrings
import com.example.ui.theme.DailySaleGradient
import com.example.ui.theme.MonthlySaleGradient
import java.util.Calendar
import java.util.Locale

data class ItemSalesData(
    val itemNo: Int,
    val itemName: String,
    val totalQty: Double,
    val totalAmount: Double,
    val unit: String
)

enum class SalesViewTab {
    DAILY, MONTHLY, ALL_TIME
}

@Composable
fun SalesScreen(
    dailyBills: List<Bill>,
    monthlyBills: List<Bill>,
    allBills: List<Bill> = emptyList(),
    selectedDateKey: String,
    selectedMonthKey: String,
    onDateSelected: (String) -> Unit,
    onMonthSelected: (String) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isAmountMode by remember { mutableStateOf(true) }
    var activeTab by remember { mutableStateOf(SalesViewTab.DAILY) }

    // Aggregate helper
    fun aggregateItems(bills: List<Bill>): List<ItemSalesData> {
        val map = mutableMapOf<Int, ItemSalesData>()
        for (bill in bills) {
            for (item in bill.items) {
                val prev = map[item.itemNo]
                if (prev == null) {
                    map[item.itemNo] = ItemSalesData(
                        itemNo = item.itemNo,
                        itemName = item.name,
                        totalQty = item.qty,
                        totalAmount = item.lineTotal,
                        unit = item.unit
                    )
                } else {
                    map[item.itemNo] = prev.copy(
                        totalQty = prev.totalQty + item.qty,
                        totalAmount = prev.totalAmount + item.lineTotal
                    )
                }
            }
        }
        return map.values.sortedByDescending { it.totalAmount }
    }

    val dailyItemSales by remember(dailyBills) { derivedStateOf { aggregateItems(dailyBills) } }
    val monthlyItemSales by remember(monthlyBills) { derivedStateOf { aggregateItems(monthlyBills) } }
    val allTimeItemSales by remember(allBills) { derivedStateOf { aggregateItems(allBills) } }

    val dailyTotal = remember(dailyBills) { dailyBills.sumOf { it.total } }
    val monthlyTotal = remember(monthlyBills) { monthlyBills.sumOf { it.total } }
    val allTimeTotal = remember(allBills) { allBills.sumOf { it.total } }

    // Active dataset based on selected tab
    val currentItems = when (activeTab) {
        SalesViewTab.DAILY -> dailyItemSales
        SalesViewTab.MONTHLY -> monthlyItemSales
        SalesViewTab.ALL_TIME -> allTimeItemSales
    }
    val currentBillsCount = when (activeTab) {
        SalesViewTab.DAILY -> dailyBills.size
        SalesViewTab.MONTHLY -> monthlyBills.size
        SalesViewTab.ALL_TIME -> allBills.size
    }
    val currentTotalAmount = when (activeTab) {
        SalesViewTab.DAILY -> dailyTotal
        SalesViewTab.MONTHLY -> monthlyTotal
        SalesViewTab.ALL_TIME -> allTimeTotal
    }
    val currentGradient = when (activeTab) {
        SalesViewTab.DAILY -> DailySaleGradient
        SalesViewTab.MONTHLY -> MonthlySaleGradient
        SalesViewTab.ALL_TIME -> Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF06B6D4)))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("sales_screen_container"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Timeframe Switcher Tabs: Daily / Monthly / All-Time
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tabs = listOf(
                        SalesViewTab.DAILY to AppStrings.get("sales_daily", language),
                        SalesViewTab.MONTHLY to AppStrings.get("sales_monthly", language),
                        SalesViewTab.ALL_TIME to "All-Time"
                    )
                    tabs.forEach { (tab, label) ->
                        val isSelected = activeTab == tab
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { activeTab = tab }
                                .testTag("btn_sales_tab_${tab.name.lowercase()}"),
                            color = if (isSelected) Color(0xFF1E3A8A) else Color(0xFFF8FAFC)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else Color(0xFF64748B),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Metrics & Controls Bar (Amount vs Qty toggle + Date Picker if daily)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Date/Month selector pill
                    if (activeTab == SalesViewTab.DAILY) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    val parts = selectedDateKey.split("-")
                                    val y = parts.getOrNull(0)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
                                    val m = (parts.getOrNull(1)?.toIntOrNull() ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)) - 1
                                    val d = parts.getOrNull(2)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

                                    DatePickerDialog(context, { _, year, month, dayOfMonth ->
                                        val formatted = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                        onDateSelected(formatted)
                                    }, y, m, d).show()
                                }
                                .testTag("btn_select_sales_date"),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Pick Date",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = selectedDateKey,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2563EB)
                                )
                            }
                        }
                    } else if (activeTab == SalesViewTab.MONTHLY) {
                        Surface(
                            modifier = Modifier.clip(RoundedCornerShape(10.dp)),
                            color = Color(0xFFFAF5FF)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = Color(0xFF9333EA),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = selectedMonthKey,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9333EA)
                                )
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "All Bills Combined",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7)
                            )
                        }
                    }

                    // Right: Amount ₹ vs Quantity Toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(2.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isAmountMode = true }
                                .testTag("btn_sales_mode_amount"),
                            color = if (isAmountMode) Color(0xFF2563EB) else Color.Transparent
                        ) {
                            Text(
                                text = "₹ Amount",
                                color = if (isAmountMode) Color.White else Color(0xFF64748B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isAmountMode = false }
                                .testTag("btn_sales_mode_qty"),
                            color = if (!isAmountMode) Color(0xFF2563EB) else Color.Transparent
                        ) {
                            Text(
                                text = "Quantity",
                                color = if (!isAmountMode) Color.White else Color(0xFF64748B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Main Chart Card: Always Displays the Bar Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_main_sales_chart"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = when (activeTab) {
                                    SalesViewTab.DAILY -> "Daily Item Sales Chart"
                                    SalesViewTab.MONTHLY -> "Monthly Item Sales Chart"
                                    SalesViewTab.ALL_TIME -> "All-Time Item Sales Chart"
                                },
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Revenue: ₹ ${String.format(Locale.US, "%,.0f", currentTotalAmount)} • $currentBillsCount bills",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A)
                            )
                        }

                        // Chart icon badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.QueryStats,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // The Guaranteed Responsive Compose Bar Chart
                    ComposeSalesBarChart(
                        items = currentItems,
                        isAmount = isAmountMode,
                        barGradient = currentGradient,
                        emptyFallbackItems = if (currentItems.isEmpty()) allTimeItemSales else emptyList(),
                        onSwitchToAllTime = { activeTab = SalesViewTab.ALL_TIME }
                    )

                    if (currentItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Itemized Breakdown Table
                        ItemSalesBreakdownTable(items = currentItems)
                    }
                }
            }
        }

        // 4. Secondary Quick Reference if on Daily tab and All-Time has data
        if (activeTab == SalesViewTab.DAILY && allTimeItemSales.isNotEmpty() && dailyItemSales.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_overall_preview"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Store Total Performance",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "All-time: ₹ ${String.format(Locale.US, "%,.0f", allTimeTotal)} across ${allBills.size} bills",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        ComposeSalesBarChart(
                            items = allTimeItemSales,
                            isAmount = isAmountMode,
                            barGradient = Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF06B6D4)))
                        )
                    }
                }
            }
        }
    }
}

/**
 * Pure Jetpack Compose Bar Chart:
 * - Fixed height bar track container to eliminate layout overflow / clipping
 * - Clear Y-axis scale guidelines
 * - Rupee/Quantity tags above bars
 * - #Item badge and item name below bars
 * - Horizontal scrolling for smooth responsiveness
 */
@Composable
fun ComposeSalesBarChart(
    items: List<ItemSalesData>,
    isAmount: Boolean,
    barGradient: Brush,
    modifier: Modifier = Modifier,
    emptyFallbackItems: List<ItemSalesData> = emptyList(),
    onSwitchToAllTime: (() -> Unit)? = null
) {
    if (items.isEmpty()) {
        // When no bills for this exact date/period
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC), RoundedCornerShape(16.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QueryStats,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = "No sales recorded for this date yet",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Bills created on this date will appear as live sales bars.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
                if (emptyFallbackItems.isNotEmpty() && onSwitchToAllTime != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSwitchToAllTime() },
                        color = Color(0xFF2563EB)
                    ) {
                        Text(
                            text = "View All-Time Sales Chart →",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
        return
    }

    val displayItems = remember(items) { items.take(15) }
    val maxValue = remember(displayItems, isAmount) {
        val max = displayItems.maxOfOrNull { if (isAmount) it.totalAmount else it.totalQty } ?: 100.0
        if (max <= 0.0) 100.0 else max
    }

    val scrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxWidth()) {
        // Y-axis guide labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isAmount) "Max: ₹${maxValue.toInt()}" else "Max: ${maxValue.toInt()} pcs",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
            Text(
                text = if (isAmount) "Mid: ₹${(maxValue / 2).toInt()}" else "Mid: ${(maxValue / 2).toInt()} pcs",
                fontSize = 10.sp,
                color = Color(0xFFCBD5E1)
            )
            Text(
                text = "0",
                fontSize = 10.sp,
                color = Color(0xFFCBD5E1)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Horizontal Grid Line (Top)
        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

        Spacer(modifier = Modifier.height(8.dp))

        // Scrollable Bar Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            displayItems.forEach { item ->
                val rawValue = if (isAmount) item.totalAmount else item.totalQty
                val targetFraction = (rawValue / maxValue).toFloat().coerceIn(0.10f, 1.0f)
                val animatedFraction by animateFloatAsState(
                    targetValue = targetFraction,
                    animationSpec = tween(durationMillis = 500),
                    label = "bar_${item.itemNo}"
                )

                // Single Bar Column with strictly bounded heights
                Column(
                    modifier = Modifier.width(62.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Value Tag above bar (e.g. ₹240 or 15)
                    Text(
                        text = if (isAmount) "₹${rawValue.toInt()}" else "${if (rawValue % 1.0 == 0.0) rawValue.toInt() else rawValue}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Bounded Bar Track (120dp height)
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(120.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        val barHeightDp = (120f * animatedFraction).dp.coerceIn(10.dp, 120.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(barHeightDp)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(barGradient)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Item Number Badge
                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                        color = Color(0xFFE2E8F0)
                    ) {
                        Text(
                            text = "#${item.itemNo}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Item Name Label
                    Text(
                        text = item.itemName,
                        fontSize = 10.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        // Baseline Grid Line
        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.5.dp)
    }
}

/**
 * Itemized Sales Breakdown Table below the chart
 */
@Composable
fun ItemSalesBreakdownTable(items: List<ItemSalesData>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Itemized Breakdown",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Table Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFF8FAFC),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.width(26.dp))
                Text("Item Name", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.weight(1f))
                Text("Qty Sold", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.width(70.dp), textAlign = TextAlign.Center)
                Text("Revenue", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.width(70.dp), textAlign = TextAlign.End)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Table Rows
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${item.itemNo}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), modifier = Modifier.width(26.dp))
                Text(
                    text = item.itemName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${if (item.totalQty % 1.0 == 0.0) item.totalQty.toInt() else item.totalQty} ${item.unit}",
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    modifier = Modifier.width(70.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "₹ ${String.format(Locale.US, "%,.0f", item.totalAmount)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16A34A),
                    modifier = Modifier.width(70.dp),
                    textAlign = TextAlign.End
                )
            }
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.8.dp)
        }
    }
}
