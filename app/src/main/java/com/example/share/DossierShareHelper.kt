package com.example.share

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.TheoryDossier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object DossierShareHelper {

    fun buildSocialSummaryText(dossier: TheoryDossier): String {
        return buildString {
            if (dossier.isWeeklySpotlight) {
                appendLine("CONSPIRACY OF THE WEEK | ${dossier.caseCode}")
            } else {
                appendLine("CIPHERFEED DOSSIER | ${dossier.caseCode}")
            }
            appendLine("${dossier.title} (${dossier.chronologicalDateLabel})")
            appendLine("Category: ${dossier.category} · ${dossier.tractionStatus}")
            appendLine()
            appendLine(dossier.summary)
            if (dossier.recentUpdates.isNotBlank()) {
                appendLine()
                appendLine("Latest Update: ${dossier.recentUpdates}")
            }
            dossier.formattedGps?.let { gps ->
                appendLine()
                val place = dossier.locationName?.let { " ($it)" } ?: ""
                appendLine("GPS Site: $gps$place")
            }
            if (dossier.dangersAndLaws.isNotEmpty()) {
                appendLine()
                appendLine("Laws & Safety Notice: ${dossier.dangersAndLaws.first()}")
            }
            if (dossier.sources.isNotEmpty()) {
                appendLine()
                appendLine("Sources:")
                dossier.sources.take(3).forEach { source ->
                    appendLine("• ${source.domain}: ${source.url}")
                }
            }
            appendLine()
            append("#CipherFeed #ChronologicalArchive")
        }.trim()
    }

    fun copySummaryToClipboard(context: Context, dossier: TheoryDossier) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(
            dossier.title,
            buildSocialSummaryText(dossier)
        )
        clipboard.setPrimaryClip(clip)
    }

    fun shareTextSummary(context: Context, dossier: TheoryDossier) {
        val text = buildSocialSummaryText(dossier)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "${dossier.title} (${dossier.caseCode})")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Dossier Summary").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    suspend fun exportAndShareCardImage(context: Context, dossier: TheoryDossier) {
        val uri = withContext(Dispatchers.Default) {
            val bitmap = renderDossierCardBitmap(context, dossier)
            try {
                saveBitmapToCacheUri(context, bitmap, dossier.id)
            } finally {
                bitmap.recycle()
            }
        }
        val caption = buildSocialSummaryText(dossier)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "${dossier.title} (${dossier.caseCode})")
            putExtra(Intent.EXTRA_TEXT, caption)
            clipData = ClipData.newUri(context.contentResolver, "Dossier Card", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Share Conspiracy Dossier Card").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun saveBitmapToCacheUri(context: Context, bitmap: Bitmap, id: String): Uri {
        val dir = File(context.cacheDir, "shared_cards").apply { mkdirs() }
        val safeName = id.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "dossier" }
        val file = File(dir, "cipherfeed_$safeName.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun renderDossierCardBitmap(context: Context, dossier: TheoryDossier): Bitmap {
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgColor = Color.parseColor("#0D1117")
        val cardColor = Color.parseColor("#1B2330")
        val elevatedColor = Color.parseColor("#232D3D")
        val borderColor = Color.parseColor("#303B4D")
        val amberColor = Color.parseColor("#F59E0B")
        val amberSoft = Color.parseColor("#FBBF24")
        val tealColor = Color.parseColor("#2DD4BF")
        val tealContainer = Color.parseColor("#0E2F2C")
        val crimsonColor = Color.parseColor("#EF4444")
        val crimsonContainer = Color.parseColor("#3B1518")
        val crimsonSoft = Color.parseColor("#FCA5A5")
        val creamColor = Color.parseColor("#F3EFE0")
        val secondaryColor = Color.parseColor("#C5C0B0")
        val mutedColor = Color.parseColor("#8B949E")

        val displayFont = ResourcesCompat.getFont(context, R.font.playfair_display) ?: Typeface.SERIF
        val bodyFont = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans) ?: Typeface.SANS_SERIF
        val monoFont = ResourcesCompat.getFont(context, R.font.jetbrains_mono) ?: Typeface.MONOSPACE

        // Background + subtle coordinate grid
        canvas.drawColor(bgColor)
        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#161D28")
            strokeWidth = 2f
        }
        var gx = 0f
        while (gx < width) {
            canvas.drawLine(gx, 0f, gx, height.toFloat(), gridPaint)
            gx += 90f
        }
        var gy = 0f
        while (gy < height) {
            canvas.drawLine(0f, gy, width.toFloat(), gy, gridPaint)
            gy += 90f
        }

        // Main Card Frame
        val pad = 56f
        val cardRect = RectF(pad, pad, width - pad, height - pad)
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = cardColor }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (dossier.isWeeklySpotlight) amberColor else borderColor
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(cardRect, 34f, 34f, cardPaint)
        canvas.drawRoundRect(cardRect, 34f, 34f, strokePaint)

        // Left crimson-amber spine accent
        val spinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = crimsonColor }
        canvas.drawRoundRect(RectF(pad + 26f, pad + 36f, pad + 34f, height - pad - 36f), 4f, 4f, spinePaint)

        val contentLeft = pad + 62f
        val contentWidth = (width - pad - 44f - contentLeft).toInt()
        var cursorY = pad + 48f

        // Header line: Brand + Case Code
        val monoPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(monoFont, Typeface.BOLD)
            textSize = 24f
            color = amberColor
        }
        val headerLabel = if (dossier.isWeeklySpotlight) {
            "CONSPIRACY OF THE WEEK  ·  ${dossier.caseCode}"
        } else {
            "CIPHERFEED ARCHIVE  ·  ${dossier.caseCode}"
        }
        canvas.drawText(headerLabel, contentLeft, cursorY + 24f, monoPaint)
        cursorY += 54f

        // Chronological Stamp + Category
        val stampPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(monoFont, Typeface.NORMAL)
            textSize = 22f
            color = tealColor
        }
        canvas.drawText(
            "${dossier.chronologicalDateLabel}  ·  ${dossier.category.uppercase()}  ·  ${dossier.tractionStatus.uppercase()}",
            contentLeft,
            cursorY + 20f,
            stampPaint
        )
        cursorY += 52f

        // Title
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(displayFont, Typeface.BOLD)
            textSize = 52f
            color = creamColor
        }
        cursorY = drawWrappedText(canvas, dossier.title, titlePaint, contentLeft, cursorY, contentWidth, 3)
        cursorY += 28f

        // Summary
        val summaryPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = bodyFont
            textSize = 30f
            color = secondaryColor
        }
        cursorY = drawWrappedText(canvas, dossier.summary, summaryPaint, contentLeft, cursorY, contentWidth, 5)
        cursorY += 28f

        // Latest Update Block
        if (dossier.recentUpdates.isNotBlank() && cursorY < 890f) {
            val updateLabelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create(monoFont, Typeface.BOLD)
                textSize = 21f
                color = amberSoft
            }
            canvas.drawText("LATEST WEB UPDATE", contentLeft, cursorY + 20f, updateLabelPaint)
            cursorY += 32f

            val updatePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = bodyFont
                textSize = 25f
                color = creamColor
            }
            cursorY = drawWrappedText(canvas, dossier.recentUpdates, updatePaint, contentLeft, cursorY, contentWidth, 3)
            cursorY += 26f
        }

        // GPS Coordinates Box
        dossier.formattedGps?.let { gps ->
            if (cursorY < 1030f) {
                val gpsBox = RectF(contentLeft, cursorY, contentLeft + contentWidth, cursorY + 96f)
                val gpsBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = tealContainer }
                val gpsBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = tealColor
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                }
                canvas.drawRoundRect(gpsBox, 18f, 18f, gpsBg)
                canvas.drawRoundRect(gpsBox, 18f, 18f, gpsBorder)

                val gpsTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = Typeface.create(monoFont, Typeface.BOLD)
                    textSize = 24f
                    color = tealColor
                }
                val gpsLocPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = bodyFont
                    textSize = 22f
                    color = creamColor
                }
                canvas.drawText("GPS: $gps", contentLeft + 20f, cursorY + 38f, gpsTitlePaint)
                val loc = dossier.locationName ?: "Pinned Coordinate Site"
                canvas.drawText(loc.take(46), contentLeft + 20f, cursorY + 74f, gpsLocPaint)
                cursorY += 116f
            }
        }

        // Laws & Dangers Box
        if (dossier.dangersAndLaws.isNotEmpty() && cursorY < 1140f) {
            val lawBox = RectF(contentLeft, cursorY, contentLeft + contentWidth, cursorY + 96f)
            val lawBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = crimsonContainer }
            val lawBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = crimsonColor
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawRoundRect(lawBox, 18f, 18f, lawBg)
            canvas.drawRoundRect(lawBox, 18f, 18f, lawBorder)

            val lawLabelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create(monoFont, Typeface.BOLD)
                textSize = 20f
                color = crimsonSoft
            }
            canvas.drawText("LAWS & SAFETY NOTICE", contentLeft + 20f, cursorY + 32f, lawLabelPaint)

            val lawTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = bodyFont
                textSize = 22f
                color = creamColor
            }
            drawWrappedText(
                canvas,
                dossier.dangersAndLaws.first(),
                lawTextPaint,
                contentLeft + 20f,
                cursorY + 42f,
                contentWidth - 40,
                2
            )
        }

        // Footer Attribution Bar
        val footerTop = height - pad - 88f
        val footerRect = RectF(contentLeft, footerTop, contentLeft + contentWidth, height - pad - 24f)
        val footerBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = elevatedColor }
        canvas.drawRoundRect(footerRect, 14f, 14f, footerBg)

        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = monoFont
            textSize = 20f
            color = mutedColor
        }
        val sourceSummary = if (dossier.sources.isNotEmpty()) {
            "SOURCES: ${dossier.sources.take(3).joinToString(" · ") { it.domain }}"
        } else {
            "CIPHERFEED · DECLASSIFIED WEB CHRONICLE"
        }
        canvas.drawText(sourceSummary.take(56), contentLeft + 18f, footerTop + 40f, footerPaint)

        return bitmap
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        paint: TextPaint,
        x: Float,
        y: Float,
        width: Int,
        maxLines: Int
    ): Float {
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width.coerceAtLeast(100))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.15f)
            .setMaxLines(maxLines)
            .setEllipsize(android.text.TextUtils.TruncateAt.END)
            .build()

        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
        return y + layout.height
    }
}
