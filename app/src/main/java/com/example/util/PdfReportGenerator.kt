package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.model.Account
import com.example.data.model.Transaction
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595 // Standard A4 width in points
    private const val PAGE_HEIGHT = 842 // Standard A4 height in points
    private const val MARGIN = 36f
    private const val USABLE_WIDTH = PAGE_WIDTH - (MARGIN * 2)

    data class ReportFilterInfo(
        val userName: String,
        val accountFilter: String,
        val typeFilter: String,
        val categoryFilter: String = "All",
        val searchQuery: String = "",
        val dateRangeText: String = "All Time"
    )

    fun generatePdf(
        context: Context,
        transactions: List<Transaction>,
        accounts: List<Account>,
        filterInfo: ReportFilterInfo,
        outputStream: OutputStream
    ) {
        val pdfDoc = PdfDocument()
        val accountMap = accounts.associateBy { it.id }

        // Paints
        val primaryPaint = Paint().apply {
            color = Color.parseColor("#07182B") // Midnight Navy
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = Color.parseColor("#07182B")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val thPaint = Paint().apply {
            color = Color.WHITE
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val creditPaint = Paint().apply {
            color = Color.parseColor("#059669") // Emerald
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val expensePaint = Paint().apply {
            color = Color.parseColor("#DC2626") // Red
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val transferPaint = Paint().apply {
            color = Color.parseColor("#2563EB") // Blue
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
            isAntiAlias = true
        }

        val rowBgPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            isAntiAlias = true
        }

        // Calculate Summary
        var totalCreditPaisa = 0L
        var totalExpensePaisa = 0L
        for (tx in transactions) {
            if (tx.type == "CREDIT") totalCreditPaisa += tx.amountPaisa
            else if (tx.type == "EXPENSE") totalExpensePaisa += tx.amountPaisa
        }
        val netCashFlowPaisa = totalCreditPaisa - totalExpensePaisa

        // Table Column Widths (total USABLE_WIDTH = 523)
        val colDateWidth = 72f
        val colAccountWidth = 85f
        val colDescWidth = 206f
        val colTypeWidth = 60f
        val colAmountWidth = 100f

        val colXDate = MARGIN
        val colXAccount = colXDate + colDateWidth
        val colXDesc = colXAccount + colAccountWidth
        val colXType = colXDesc + colDescWidth
        val colXAmount = colXType + colTypeWidth

        // Helper to draw header
        fun drawReportHeader(canvas: Canvas, pageNumber: Int) {
            // Accent bar at top
            canvas.drawRect(MARGIN, 24f, PAGE_WIDTH - MARGIN, 28f, Paint().apply {
                color = Color.parseColor("#10B981")
            })

            // Branding & Title
            canvas.drawText("Pockitify", MARGIN, 46f, titlePaint)
            canvas.drawText("Track every move your money makes • Offline Finance Manager", MARGIN, 58f, subtitlePaint)

            // Right-aligned report title & user
            val titleText = "Transaction History Report"
            val titleWidth = titlePaint.measureText(titleText)
            canvas.drawText(titleText, PAGE_WIDTH - MARGIN - titleWidth, 46f, titlePaint)

            val userText = "Prepared for: ${filterInfo.userName.ifBlank { "User" }}"
            val userWidth = subtitlePaint.measureText(userText)
            canvas.drawText(userText, PAGE_WIDTH - MARGIN - userWidth, 58f, subtitlePaint)

            canvas.drawLine(MARGIN, 66f, PAGE_WIDTH - MARGIN, 66f, linePaint)

            // Metadata Row
            val genDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            var metaY = 79f

            val accountLabel = "Account: ${filterInfo.accountFilter}"
            val typeLabel = "Type: ${filterInfo.typeFilter}"
            val catLabel = "Category: ${filterInfo.categoryFilter}"
            val dateLabel = "Date Range: ${filterInfo.dateRangeText}"
            val genLabel = "Generated: $genDate"

            canvas.drawText(accountLabel, MARGIN, metaY, subtitlePaint)
            canvas.drawText(typeLabel, MARGIN + 130f, metaY, subtitlePaint)
            canvas.drawText(catLabel, MARGIN + 230f, metaY, subtitlePaint)
            val genWidth = subtitlePaint.measureText(genLabel)
            canvas.drawText(genLabel, PAGE_WIDTH - MARGIN - genWidth, metaY, subtitlePaint)

            if (filterInfo.searchQuery.isNotBlank()) {
                metaY += 12f
                canvas.drawText("Search query: \"${filterInfo.searchQuery}\"", MARGIN, metaY, subtitlePaint)
            }
        }

        // Helper to draw Table Header
        fun drawTableHeader(canvas: Canvas, topY: Float) {
            val thHeight = 22f
            canvas.drawRect(MARGIN, topY, PAGE_WIDTH - MARGIN, topY + thHeight, Paint().apply {
                color = Color.parseColor("#101C35")
            })

            val textY = topY + 15f
            canvas.drawText("Date", colXDate + 6f, textY, thPaint)
            canvas.drawText("Account", colXAccount + 6f, textY, thPaint)
            canvas.drawText("Category / Description", colXDesc + 6f, textY, thPaint)
            canvas.drawText("Type", colXType + 6f, textY, thPaint)

            val amountHeaderText = "Amount (PKR)"
            val amtW = thPaint.measureText(amountHeaderText)
            canvas.drawText(amountHeaderText, colXAmount + colAmountWidth - amtW - 6f, textY, thPaint)
        }

        fun drawFooter(canvas: Canvas, pageNumber: Int) {
            val footerY = PAGE_HEIGHT - 24f
            canvas.drawLine(MARGIN, footerY - 10f, PAGE_WIDTH - MARGIN, footerY - 10f, linePaint)
            val footerText = "Pockitify Personal Finance Report • 100% Offline & Private"
            canvas.drawText(footerText, MARGIN, footerY, subtitlePaint)

            val pageStr = "Page $pageNumber"
            val pageStrW = subtitlePaint.measureText(pageStr)
            canvas.drawText(pageStr, PAGE_WIDTH - MARGIN - pageStrW, footerY, subtitlePaint)
        }

        var pageNum = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
        var page = pdfDoc.startPage(pageInfo)
        var canvas = page.canvas

        drawReportHeader(canvas, pageNum)

        var currentY = 104f
        drawTableHeader(canvas, currentY)
        currentY += 22f

        val rowHeight = 22f

        if (transactions.isEmpty()) {
            canvas.drawText(
                "No transactions found matching the applied filters.",
                MARGIN + 12f,
                currentY + 24f,
                textPaint
            )
            currentY += 40f
        } else {
            for ((index, tx) in transactions.withIndex()) {
                // Check if page overflow
                if (currentY + rowHeight + 110f > PAGE_HEIGHT - 40f) {
                    drawFooter(canvas, pageNum)
                    pdfDoc.finishPage(page)

                    pageNum++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
                    page = pdfDoc.startPage(pageInfo)
                    canvas = page.canvas

                    drawReportHeader(canvas, pageNum)
                    currentY = 104f
                    drawTableHeader(canvas, currentY)
                    currentY += 22f
                }

                // Alternate row background
                if (index % 2 == 1) {
                    canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + rowHeight, rowBgPaint)
                }

                val rowTextY = currentY + 14f

                // 1. Date
                val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(tx.timestamp))
                canvas.drawText(dateStr, colXDate + 6f, rowTextY, textPaint)

                // 2. Account
                val accStr = when (tx.type) {
                    "CREDIT" -> tx.toAccountId?.uppercase() ?: "Account"
                    "EXPENSE" -> tx.fromAccountId?.uppercase() ?: "Account"
                    else -> "${tx.fromAccountId?.uppercase()} -> ${tx.toAccountId?.uppercase()}"
                }
                canvas.drawText(accStr, colXAccount + 6f, rowTextY, textPaint)

                // 3. Category & Description
                val descStr = if (tx.note.isNotBlank()) {
                    "${tx.category} (${tx.note})"
                } else {
                    tx.category
                }
                val truncatedDesc = if (descStr.length > 36) descStr.take(33) + "..." else descStr
                canvas.drawText(truncatedDesc, colXDesc + 6f, rowTextY, textPaint)

                // 4. Type
                val typeLabel = when (tx.type) {
                    "CREDIT" -> "Credit"
                    "EXPENSE" -> "Expense"
                    else -> "Transfer"
                }
                canvas.drawText(typeLabel, colXType + 6f, rowTextY, textPaint)

                // 5. Amount
                val (amtText, paint) = when (tx.type) {
                    "CREDIT" -> Pair("+Rs. ${CurrencyFormatter.formatPaisa(tx.amountPaisa, includePrefix = false)}", creditPaint)
                    "EXPENSE" -> Pair("-Rs. ${CurrencyFormatter.formatPaisa(tx.amountPaisa, includePrefix = false)}", expensePaint)
                    else -> Pair("Rs. ${CurrencyFormatter.formatPaisa(tx.amountPaisa, includePrefix = false)}", transferPaint)
                }
                val amtWidth = paint.measureText(amtText)
                canvas.drawText(amtText, colXAmount + colAmountWidth - amtWidth - 6f, rowTextY, paint)

                // Underline
                canvas.drawLine(MARGIN, currentY + rowHeight, PAGE_WIDTH - MARGIN, currentY + rowHeight, linePaint)
                currentY += rowHeight
            }
        }

        // Summary Box
        if (currentY + 110f > PAGE_HEIGHT - 40f) {
            drawFooter(canvas, pageNum)
            pdfDoc.finishPage(page)

            pageNum++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
            page = pdfDoc.startPage(pageInfo)
            canvas = page.canvas
            drawReportHeader(canvas, pageNum)
            currentY = 104f
        }

        currentY += 16f

        // Draw Summary Box
        val summaryBoxHeight = 84f
        val summaryPaint = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            isAntiAlias = true
        }
        canvas.drawRoundRect(
            MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + summaryBoxHeight, 8f, 8f, summaryPaint
        )
        canvas.drawRoundRect(
            MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + summaryBoxHeight, 8f, 8f, Paint().apply {
                color = Color.parseColor("#CBD5E1")
                style = Paint.Style.STROKE
                strokeWidth = 1f
                isAntiAlias = true
            }
        )

        val sumTitlePaint = Paint().apply {
            color = Color.parseColor("#07182B")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("REPORT SUMMARY (${transactions.size} Transactions)", MARGIN + 12f, currentY + 18f, sumTitlePaint)

        val sumY1 = currentY + 38f
        val sumY2 = currentY + 56f
        val sumY3 = currentY + 74f

        canvas.drawText("Total Credits:", MARGIN + 12f, sumY1, textPaint)
        canvas.drawText("+Rs. ${CurrencyFormatter.formatPaisa(totalCreditPaisa, includePrefix = false)}", MARGIN + 90f, sumY1, creditPaint)

        canvas.drawText("Total Expenses:", MARGIN + 210f, sumY1, textPaint)
        canvas.drawText("-Rs. ${CurrencyFormatter.formatPaisa(totalExpensePaisa, includePrefix = false)}", MARGIN + 295f, sumY1, expensePaint)

        val netPrefix = if (netCashFlowPaisa >= 0) "+Rs. " else "-Rs. "
        val netAmtPaisa = kotlin.math.abs(netCashFlowPaisa)
        val netPaint = if (netCashFlowPaisa >= 0) creditPaint else expensePaint

        canvas.drawText("Net Cash Flow:", MARGIN + 12f, sumY2, sumTitlePaint)
        canvas.drawText("$netPrefix${CurrencyFormatter.formatPaisa(netAmtPaisa, includePrefix = false)}", MARGIN + 90f, sumY2, netPaint)

        val transferCount = transactions.count { it.type == "TRANSFER" }
        canvas.drawText("Transfers (Not counted in cash flow): $transferCount", MARGIN + 210f, sumY2, subtitlePaint)

        canvas.drawText("Note: This summary reflects only the transactions matching the active filters applied at time of export.", MARGIN + 12f, sumY3, subtitlePaint)

        drawFooter(canvas, pageNum)
        pdfDoc.finishPage(page)

        pdfDoc.writeTo(outputStream)
        pdfDoc.close()
    }

    /**
     * Saves a temporary copy to app cache for immediate viewing/sharing
     */
    fun saveTempPdfFile(
        context: Context,
        transactions: List<Transaction>,
        accounts: List<Account>,
        filterInfo: ReportFilterInfo
    ): File {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val accountTag = filterInfo.accountFilter.replace(" ", "_").replace("(", "").replace(")", "")
        val dateTag = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val fileName = "Pockitify_Transactions_${accountTag}_$dateTag.pdf"
        val file = File(reportsDir, fileName)

        FileOutputStream(file).use { out ->
            generatePdf(context, transactions, accounts, filterInfo, out)
        }
        return file
    }
}
