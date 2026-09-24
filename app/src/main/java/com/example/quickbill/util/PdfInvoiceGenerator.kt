package com.example.quickbill.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.quickbill.data.model.Bill
import com.example.quickbill.data.model.ShopProfile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfInvoiceGenerator {

    fun generateAndSharePdf(context: Context, bill: Bill, shop: ShopProfile) {
        val file = generatePdfFile(context, bill, shop)
        if (file != null && file.exists()) {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Bill #${bill.billNo} - ${shop.shopName}")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Invoice #${bill.billNo} from ${shop.shopName}. Total: ${shop.currencySymbol} ${String.format(Locale.US, "%.2f", bill.total)}"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Bill via"))
        }
    }

    fun generatePdfFile(context: Context, bill: Bill, shop: ShopProfile): File? {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // Standard A4 width in points
        val pageHeight = 842 // Standard A4 height in points
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        val titlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.rgb(30, 58, 138) // Deep Blue
        }
        val subPaint = Paint().apply {
            isAntiAlias = true
            textSize = 12f
            color = Color.rgb(75, 85, 99)
        }
        val boldPaint = Paint().apply {
            isAntiAlias = true
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.rgb(17, 24, 39)
        }
        val textPaint = Paint().apply {
            isAntiAlias = true
            textSize = 11f
            color = Color.rgb(31, 41, 55)
        }
        val headerBgPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(238, 242, 255) // Indigo 50
        }
        val linePaint = Paint().apply {
            isAntiAlias = true
            strokeWidth = 1f
            color = Color.rgb(229, 231, 235) // Gray 200
        }

        var y = 50f
        val margin = 40f
        val contentWidth = pageWidth - (margin * 2)

        // Header Banner
        paint.color = Color.rgb(30, 58, 138)
        canvas.drawRoundRect(margin, y, margin + contentWidth, y + 60f, 10f, 10f, paint)

        val headerTextPaint = Paint().apply {
            isAntiAlias = true
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.WHITE
        }
        canvas.drawText("QuickBill", margin + 20f, y + 36f, headerTextPaint)

        val headerSubPaint = Paint().apply {
            isAntiAlias = true
            textSize = 11f
            color = Color.rgb(199, 210, 254)
        }
        canvas.drawText("INVOICE / CASH MEMO", margin + contentWidth - 160f, y + 36f, headerSubPaint)

        y += 80f

        // Shop Info & Bill Metadata
        canvas.drawText(shop.shopName, margin, y, titlePaint)
        y += 18f
        if (shop.address.isNotBlank()) {
            canvas.drawText(shop.address, margin, y, subPaint)
            y += 16f
        }
        canvas.drawText("Phone: ${shop.phone}", margin, y, subPaint)

        val rightX = margin + contentWidth - 180f
        var rightY = y - (if (shop.address.isNotBlank()) 34f else 18f)
        canvas.drawText("Bill No: #${bill.billNo}", rightX, rightY, boldPaint)
        rightY += 16f
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(bill.createdAt))
        canvas.drawText("Date: $dateStr", rightX, rightY, subPaint)

        y += 26f
        canvas.drawLine(margin, y, margin + contentWidth, y, linePaint)
        y += 20f

        // Customer Info
        if (bill.customerName.isNotBlank() || bill.customerPhone.isNotBlank()) {
            canvas.drawText("Billed To:", margin, y, boldPaint)
            y += 16f
            if (bill.customerName.isNotBlank()) {
                canvas.drawText("Customer: ${bill.customerName}", margin, y, textPaint)
                y += 16f
            }
            if (bill.customerPhone.isNotBlank()) {
                canvas.drawText("Phone: ${bill.customerPhone}", margin, y, subPaint)
                y += 16f
            }
            y += 10f
        }

        // Table Header
        val colNo = margin + 10f
        val colName = margin + 60f
        val colQty = margin + 280f
        val colPrice = margin + 370f
        val colTotal = margin + 450f

        canvas.drawRoundRect(margin, y, margin + contentWidth, y + 26f, 6f, 6f, headerBgPaint)
        val thY = y + 18f
        canvas.drawText("#", colNo, thY, boldPaint)
        canvas.drawText("Item Name", colName, thY, boldPaint)
        canvas.drawText("Qty & Unit", colQty, thY, boldPaint)
        canvas.drawText("Price (₹)", colPrice, thY, boldPaint)
        canvas.drawText("Total (₹)", colTotal, thY, boldPaint)
        y += 32f

        // Table Rows
        for (item in bill.items) {
            val qtyStr = if (item.qty % 1.0 == 0.0) "${item.qty.toInt()} ${item.unit}" else "${item.qty} ${item.unit}"
            val priceStr = String.format(Locale.US, "%.2f", item.price)
            val lineTotalStr = String.format(Locale.US, "%.2f", item.lineTotal)

            canvas.drawText("${item.itemNo}", colNo, y + 14f, textPaint)
            // Truncate name if too long
            val displayName = if (item.name.length > 32) item.name.take(30) + ".." else item.name
            canvas.drawText(displayName, colName, y + 14f, textPaint)
            canvas.drawText(qtyStr, colQty, y + 14f, textPaint)
            canvas.drawText(priceStr, colPrice, y + 14f, textPaint)
            canvas.drawText(lineTotalStr, colTotal, y + 14f, boldPaint)

            y += 24f
            canvas.drawLine(margin, y, margin + contentWidth, y, linePaint)
            y += 6f
        }

        y += 14f
        // Grand Total Box
        val totalBoxWidth = 220f
        val totalBoxX = margin + contentWidth - totalBoxWidth
        paint.color = Color.rgb(243, 244, 246)
        canvas.drawRoundRect(totalBoxX, y, margin + contentWidth, y + 50f, 8f, 8f, paint)

        val totalLabelPaint = Paint().apply {
            isAntiAlias = true
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.rgb(31, 41, 55)
        }
        val totalValuePaint = Paint().apply {
            isAntiAlias = true
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.rgb(22, 101, 52) // Green 800
        }
        canvas.drawText("GRAND TOTAL:", totalBoxX + 16f, y + 32f, totalLabelPaint)
        canvas.drawText(
            "${shop.currencySymbol} ${String.format(Locale.US, "%.2f", bill.total)}",
            totalBoxX + 120f,
            y + 32f,
            totalValuePaint
        )

        y += 80f
        // Footer Message
        val footerPaint = Paint().apply {
            isAntiAlias = true
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            color = Color.rgb(107, 114, 128)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Thank you for your business! Visit again.", pageWidth / 2f, y, footerPaint)

        pdfDocument.finishPage(page)

        return try {
            val dir = File(context.cacheDir, "bills")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "Bill_${bill.billNo}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}
