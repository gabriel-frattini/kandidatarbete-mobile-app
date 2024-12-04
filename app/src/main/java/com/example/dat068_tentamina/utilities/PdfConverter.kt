import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.util.Log
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import com.example.dat068_tentamina.MainActivity
import com.example.dat068_tentamina.model.CanvasObject
import com.example.dat068_tentamina.model.Line
import com.example.dat068_tentamina.model.TextBox
import java.io.File



class PdfConverter {
    companion object {
        fun createPdfFromAnswers(
            answers: MutableMap<Int, List<CanvasObject>>,
            pageWidth: Int,
            pageHeight: Int,
            context: Context
        ): File {
            val pdfDocument = PdfDocument()
            var questionNumber = 1
            var pageNumber = 1
            answers.forEach { (_, drawingObjects) ->
                // Create a new page
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                // Create a bitmap for the page
                val bitmap = createBitmapFromCanvasObject(drawingObjects, pageWidth, pageHeight, context, questionNumber, pageNumber)
                canvas.drawBitmap(bitmap, 0f, 0f, null)

                pdfDocument.finishPage(page)
                questionNumber++
                pageNumber++
            }

            // Save the PDF to the cache directory
            val file = File(context.cacheDir, "exam_submission.pdf")
            pdfDocument.writeTo(file.outputStream())
            pdfDocument.close()

            return file
        }

        fun createBitmapFromCanvasObject(
            drawingObjects: List<CanvasObject>,
            width: Int,
            height: Int,
            context: Context,
            questionNum: Int,
            pageNum: Int
        ): Bitmap {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            val paint = android.graphics.Paint()

            // Reserve space for metadata (e.g., 100px at the top)
            val metaHeightPx = 100f

            // Draw Metadata
            val metadataPaint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 40f // Metadata font size
                isAntiAlias = true
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText("Question: $questionNum", 20f, metaHeightPx / 2, metadataPaint)
            canvas.drawText("Page: $pageNum", width - 200f, metaHeightPx / 2, metadataPaint)

            // Adjust drawing objects' offsets to account for metadata
            val density = context.resources.displayMetrics.density
            drawingObjects.forEach { obj ->
                when (obj) {
                    is Line -> {
                        val adjustedStart = obj.start.copy(y = obj.start.y + metaHeightPx)
                        val adjustedEnd = obj.end.copy(y = obj.end.y + metaHeightPx)

                        paint.color = obj.color.toArgb()
                        paint.strokeWidth = obj.strokeWidth.value * density
                        paint.strokeCap = when (obj.cap) {
                            StrokeCap.Round -> Paint.Cap.ROUND
                            StrokeCap.Square -> Paint.Cap.SQUARE
                            else -> Paint.Cap.BUTT
                        }
                        canvas.drawLine(adjustedStart.x, adjustedStart.y, adjustedEnd.x, adjustedEnd.y, paint)
                    }
                    is TextBox -> {
                        val scaledDensity = context.resources.displayMetrics.scaledDensity
                        val posX = obj.position.x * density
                        val posY = obj.position.y * density + metaHeightPx // Adjust position for metadata

                        val textSizeInPx = obj.fontSize.value * scaledDensity

                        val textPaint = Paint().apply {
                            color = obj.color.toArgb()
                            textSize = textSizeInPx
                            isAntiAlias = true
                            typeface = Typeface.DEFAULT
                            textAlign = Paint.Align.LEFT
                        }

                        val metrics = textPaint.fontMetrics
                        val baselineAdjustment = metrics.ascent
                        canvas.drawText(obj.text, posX, posY - baselineAdjustment, textPaint)
                    }
                }
            }
            return bitmap
        }

    }

}
