import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.util.Log
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
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
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                // Create a bitmap at the same scale as the PDF page
                val bitmap = createBitmapFromCanvasObject(
                    drawingObjects = drawingObjects,
                    width = pageWidth,
                    height = pageHeight,
                    context = context,
                    questionNum = questionNumber,
                    pageNum = pageNumber,
                    scrollOffset = 0f // No scroll offset for PDF rendering
                )
                // Draw the bitmap on the PDF canvas
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                pdfDocument.finishPage(page)

                questionNumber++
                pageNumber++
            }

            // Save the PDF file
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
            pageNum: Int,
            scrollOffset: Float // Pass the verticalScrollState's value here
        ): Bitmap {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)

            // Reserve space for metadata
            val metaHeightPx = 50f

            // Draw metadata
            val metadataPaint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 40f
                isAntiAlias = true
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText("Question: $questionNum", 20f, metaHeightPx / 2, metadataPaint)
            canvas.drawText("Page: $pageNum", width - 200f, metaHeightPx / 2, metadataPaint)

            // Adjust drawing objects for metadata and scroll
            val density = context.resources.displayMetrics.density
            drawingObjects.forEach { obj ->
                when (obj) {
                    is Line -> {
                        // Adjust Line coordinates for metadata and scroll offset
                        val adjustedStart = obj.start.copy(y = obj.start.y + metaHeightPx - scrollOffset)
                        val adjustedEnd = obj.end.copy(y = obj.end.y + metaHeightPx - scrollOffset)

                        val paint = Paint().apply {
                            color = obj.color.toArgb()
                            strokeWidth = obj.strokeWidth.value * density
                            strokeCap = when (obj.cap) {
                                StrokeCap.Round -> Paint.Cap.ROUND
                                StrokeCap.Square -> Paint.Cap.SQUARE
                                else -> Paint.Cap.BUTT
                            }
                        }
                        canvas.drawLine(adjustedStart.x, adjustedStart.y, adjustedEnd.x, adjustedEnd.y, paint)
                    }

                    is TextBox -> {
                        // Adjust TextBox position for metadata and scroll offset
                        val adjustedX = obj.position.x
                        val adjustedY = obj.position.y + metaHeightPx - scrollOffset

                        val textPaint = Paint().apply {
                            color = obj.color.toArgb()
                            textSize = obj.fontSize.value * density
                            isAntiAlias = true
                            typeface = Typeface.DEFAULT
                            textAlign = Paint.Align.LEFT
                        }

                        // Correct baseline adjustment using FontMetrics
                        val metrics = textPaint.fontMetrics
                        val baselineAdjustment = -metrics.top

                        canvas.drawText(obj.text, adjustedX, adjustedY + baselineAdjustment, textPaint)
                    }
                }
            }

            return bitmap
        }


    }

}
