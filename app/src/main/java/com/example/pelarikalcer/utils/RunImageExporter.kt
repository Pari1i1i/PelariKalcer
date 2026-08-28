package com.example.pelarikalcer.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.pelarikalcer.data.local.entity.UserEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object RunImageExporter {

    /**
     * Renders a custom high-resolution Run Summary image card with:
     * - Dark Navy Background & Styled Grid Lines
     * - Route overlay stats (Distance, Pace, Duration, Calories)
     * - Subtle semi-transparent Watermark "PelariKalcer 🏃"
     */
    fun createRunSummaryBitmap(
        distanceKm: Double,
        durationSeconds: Int,
        paceMinPerKm: Double,
        caloriesBurned: Int,
        userName: String = "PelariKalcer Runner"
    ): Bitmap {
        val width = 1080
        val height = 1080
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Background gradient (Deep Navy to Dark Slate)
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(
                    Color.parseColor("#0A0F1E"),
                    Color.parseColor("#0F172A"),
                    Color.parseColor("#0A1628")
                ),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Decorative glowing circular elements
        val glowPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#00FF66")
            alpha = 25
            maskFilter = BlurMaskFilter(120f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawCircle(width * 0.5f, height * 0.35f, 320f, glowPaint)

        // Decorative grid lines
        val gridPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            strokeWidth = 3f
            isAntiAlias = true
        }
        for (i in 1..8) {
            val pos = (height / 9f) * i
            canvas.drawLine(0f, pos, width.toFloat(), pos, gridPaint)
        }

        // 3. Header title & User Name
        val titlePaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.15f
        }
        canvas.drawText("SESI LARI KALCER", width / 2f, 130f, titlePaint)

        val userPaint = Paint().apply {
            color = Color.WHITE
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("🏃 $userName", width / 2f, 190f, userPaint)

        // 4. Large Distance Display (Centerpiece)
        val distValPaint = Paint().apply {
            color = Color.parseColor("#00FF66")
            textSize = 180f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        val formattedDist = String.format("%.2f", distanceKm)
        canvas.drawText(formattedDist, width / 2f, 430f, distValPaint)

        val distLabelPaint = Paint().apply {
            color = Color.parseColor("#00FF66")
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.2f
        }
        canvas.drawText("KILOMETER", width / 2f, 500f, distLabelPaint)

        // 5. Stat Box Card (Pace, Duration, Calories)
        val cardRect = RectF(90f, 570f, width - 90f, 870f)
        val cardPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            isAntiAlias = true
        }
        canvas.drawRoundRect(cardRect, 32f, 32f, cardPaint)

        val cardBorderPaint = Paint().apply {
            color = Color.parseColor("#3300FF66")
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        canvas.drawRoundRect(cardRect, 32f, 32f, cardBorderPaint)

        // Metrics inside card: 3 columns (Waktu, Pace, Kalori)
        val hours = durationSeconds / 3600
        val mins = (durationSeconds % 3600) / 60
        val secs = durationSeconds % 60
        val timeStr = if (hours > 0) String.format("%d:%02d:%02d", hours, mins, secs) else String.format("%02d:%02d", mins, secs)
        val paceStr = if (paceMinPerKm > 0 && paceMinPerKm < 99) String.format("%d'%02d\"", paceMinPerKm.toInt(), ((paceMinPerKm % 1) * 60).toInt()) else "--"
        val calStr = "$caloriesBurned kcal"

        val labelPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.1f
        }

        val valPaint = Paint().apply {
            color = Color.WHITE
            textSize = 44f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        // Col 1: Waktu
        val col1X = width * 0.25f
        canvas.drawText(timeStr, col1X, 680f, valPaint)
        canvas.drawText("WAKTU", col1X, 740f, labelPaint)

        // Vertical divider line 1
        canvas.drawLine(width * 0.42f, 630f, width * 0.42f, 790f, gridPaint)

        // Col 2: Pace
        val col2X = width * 0.5f
        val pacePaint = Paint(valPaint).apply { color = Color.parseColor("#00FF66") }
        canvas.drawText(paceStr, col2X, 680f, pacePaint)
        canvas.drawText("PACE", col2X, 740f, labelPaint)

        // Vertical divider line 2
        canvas.drawLine(width * 0.58f, 630f, width * 0.58f, 790f, gridPaint)

        // Col 3: Kalori
        val col3X = width * 0.75f
        canvas.drawText(calStr, col3X, 680f, valPaint)
        canvas.drawText("KALORI", col3X, 740f, labelPaint)

        // 6. Watermark Logo at Bottom-Right & Bottom-Left
        val watermarkPaint = Paint().apply {
            color = Color.WHITE
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            alpha = 140
            isAntiAlias = true
        }
        canvas.drawText("⚡ PelariKalcer", width - 360f, height - 70f, watermarkPaint)

        val appTagPaint = Paint(watermarkPaint).apply {
            textSize = 26f
            color = Color.parseColor("#94A3B8")
        }
        canvas.drawText("pelarikalcer.app", 90f, height - 70f, appTagPaint)

        return bitmap
    }

    /**
     * Saves bitmap to MediaStore Gallery (Pictures/PelariKalcer)
     */
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Uri? {
        val filename = "PelariKalcer_${System.currentTimeMillis()}.png"
        var fos: OutputStream? = null
        var imageUri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PelariKalcer")
                }
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    fos = resolver.openOutputStream(imageUri)
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/PelariKalcer"
                val file = File(imagesDir)
                if (!file.exists()) file.mkdirs()
                val imageFile = File(imagesDir, filename)
                fos = FileOutputStream(imageFile)
                imageUri = Uri.fromFile(imageFile)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                Toast.makeText(context, "Berhasil disimpan ke Galeri! 🖼️", Toast.LENGTH_SHORT).show()
            }
            return imageUri
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal menyimpan ke galeri: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        return null
    }

    /**
     * Shares image bitmap directly via Android Native Share Sheet
     */
    fun shareBitmap(context: Context, bitmap: Bitmap) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "run_summary_${System.currentTimeMillis()}.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, contentUri)
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Hasil Lari Kalcer"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membagikan gambar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
