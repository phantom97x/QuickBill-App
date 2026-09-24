package com.example.quickbill.ui.screens

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quickbill.data.model.Bill
import com.example.quickbill.data.model.ShopProfile
import com.example.quickbill.ui.components.QuickBillGradientButton
import com.example.quickbill.ui.components.QuickBillLogo
import com.example.quickbill.util.AppLanguage
import com.example.quickbill.util.AppStrings
import com.example.quickbill.util.PdfInvoiceGenerator
import com.example.quickbill.util.PrintReceiptHelper
import com.example.ui.theme.HeaderGradient
import com.example.ui.theme.HeroAddBillGradient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BillPreviewScreen(
    bill: Bill?,
    shop: ShopProfile?,
    onNewBill: () -> Unit,
    onBack: () -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (bill == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(54.dp)
                    )
                    Text(
                        text = "No bill selected for preview",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                    Text(
                        text = "Create a new bill or select one from History.",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    QuickBillGradientButton(
                        text = "+ Create New Bill",
                        onClick = onNewBill,
                        gradient = HeroAddBillGradient,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        return
    }

    val effectiveShop = shop ?: ShopProfile(
        phone = bill.shopPhone,
        shopName = "QuickBill Store",
        passwordHash = ""
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("bill_preview_screen_container"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Navigation Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_back_from_preview")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = AppStrings.get("bill_preview", language),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                    color = Color(0xFFEFF6FF)
                ) {
                    Text(
                        text = "Bill #${bill.billNo}",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1D4ED8),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Invoice-style Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("invoice_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header with Logo + Shop Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = effectiveShop.shopName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E3A8A)
                            )
                            if (effectiveShop.address.isNotBlank()) {
                                Text(
                                    text = effectiveShop.address,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Text(
                                text = "Ph: ${effectiveShop.phone}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        QuickBillLogo(size = 44.dp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Bill Metadata
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Billed To:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = bill.customerName.ifBlank { "Walk-in Customer" },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            if (bill.customerPhone.isNotBlank()) {
                                Text(
                                    text = "Ph: ${bill.customerPhone}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Bill No: #${bill.billNo}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E3A8A)
                            )
                            Text(
                                text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(bill.createdAt)),
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Item Table Header
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("#", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.width(24.dp))
                            Text("Item Name", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(1f))
                            Text("Qty", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)
                            Text("Price", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.width(50.dp), textAlign = TextAlign.End)
                            Text("Total", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.width(60.dp), textAlign = TextAlign.End)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Item Rows
                    bill.items.forEach { item ->
                        val qtyStr = if (item.qty % 1.0 == 0.0) "${item.qty.toInt()} ${item.unit}" else "${item.qty} ${item.unit}"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${item.itemNo}", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.width(24.dp))
                            Text(item.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A), modifier = Modifier.weight(1f))
                            Text(qtyStr, fontSize = 11.sp, color = Color(0xFF334155), modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)
                            Text("₹${item.price.toInt()}", fontSize = 11.sp, color = Color(0xFF334155), modifier = Modifier.width(50.dp), textAlign = TextAlign.End)
                            Text("₹${item.lineTotal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), modifier = Modifier.width(60.dp), textAlign = TextAlign.End)
                        }
                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.8.dp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Grand Total Box
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFECFDF5),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Grand Total",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = "₹ ${String.format(Locale.US, "%,.2f", bill.total)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF065F46)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Thank you for shopping with us!",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Action Buttons: Print & Download/Share PDF
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Print Button
                QuickBillGradientButton(
                    text = AppStrings.get("print", language),
                    onClick = {
                        PrintReceiptHelper.printBill(context, bill, effectiveShop)
                    },
                    gradient = HeaderGradient,
                    icon = Icons.Default.Print,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_print_bill"
                )

                // Share / Download PDF Button
                QuickBillGradientButton(
                    text = AppStrings.get("download_pdf", language),
                    onClick = {
                        PdfInvoiceGenerator.generateAndSharePdf(context, bill, effectiveShop)
                    },
                    gradient = HeaderGradient,
                    icon = Icons.Default.Share,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_download_pdf"
                )
            }
        }

        // "+ Start Next Bill" Hero Button
        item {
            QuickBillGradientButton(
                text = "+ Start Next Bill",
                onClick = onNewBill,
                gradient = HeroAddBillGradient,
                icon = Icons.Default.Add,
                modifier = Modifier.fillMaxWidth(),
                testTag = "btn_start_next_bill"
            )
        }
    }
}
