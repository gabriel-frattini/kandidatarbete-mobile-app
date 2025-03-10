import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import com.example.dat068_tentamina.model.CanvasObject
import com.example.dat068_tentamina.model.Line
import com.example.dat068_tentamina.model.TextBox
import java.io.File

class PdfConverter {
    companion object {
        private var reusableBitmap: Bitmap? = null

        fun createPdfFromAnswers(
            answers: MutableMap<Int, List<CanvasObject>>,
            pageWidth: Int,
            pageHeight: Int,
            context: Context
        ): File {
            val pdfDocument = PdfDocument()
            var questionNumber = 1
            var pageNumber = 1

            // Create a reusable bitmap
            reusableBitmap = Bitmap.createBitmap(pageWidth, pageHeight, Bitmap.Config.ARGB_8888)

            answers.forEach { (_, drawingObjects) ->
                var scrollOffset = 0f
                val totalHeight = getTotalHeight(drawingObjects, context, pageWidth)

                while (scrollOffset < totalHeight) {
                    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    val page = pdfDocument.startPage(pageInfo)
                    val canvas = page.canvas

                    // Render the portion of CanvasObjects visible within the current page's viewport
                    val bitmap = createBitmapFromCanvasObject(
                        drawingObjects = drawingObjects,
                        width = pageWidth,
                        height = pageHeight,
                        context = context,
                        questionNum = questionNumber,
                        pageNum = pageNumber,
                        scrollOffset = scrollOffset
                    )

                    // Draw a snapshot of the bitmap onto the PDF page
                    canvas.drawBitmap(Bitmap.createBitmap(bitmap), 0f, 0f, null)
                    pdfDocument.finishPage(page)

                    // Move to the next page and update the scroll offset
                    pageNumber++
                    scrollOffset += pageHeight.toFloat()
                }

                questionNumber++
            }

            // Save the PDF file
            val file = File(context.cacheDir, "exam_submission.pdf")
            pdfDocument.writeTo(file.outputStream())
            pdfDocument.close()

            // Release the reusable bitmap
            reusableBitmap?.recycle()
            reusableBitmap = null

            return file
        }

        fun createBitmapFromCanvasObject(
            drawingObjects: List<CanvasObject>,
            width: Int,
            height: Int,
            context: Context,
            questionNum: Int,
            pageNum: Int,
            scrollOffset: Float
        ): Bitmap {
            val bitmap = reusableBitmap ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)

            // Clear the bitmap before drawing
            canvas.drawColor(android.graphics.Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)

            // Reserve space for metadata
            val metaHeightPx = 50f

            // Draw metadata
            val metadataPaint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 40f
                isAntiAlias = true
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText(
                "Question: $questionNum", 20f,
                (metaHeightPx / 1.5).toFloat(), metadataPaint
            )
            canvas.drawText(
                "Page: $pageNum", width - 200f,
                (metaHeightPx / 1.5).toFloat(), metadataPaint
            )

            // Adjust drawing objects for metadata and scroll
            val density = context.resources.displayMetrics.density
            val viewportTop = scrollOffset
            val viewportBottom = scrollOffset + height

            drawingObjects.forEach { obj ->
                when (obj) {
                    is Line -> {
                        val adjustedStart = obj.start.copy(y = obj.start.y + metaHeightPx - scrollOffset)
                        val adjustedEnd = obj.end.copy(y = obj.end.y + metaHeightPx - scrollOffset)

                        if (isInViewport(obj.start.y, obj.end.y, viewportTop, viewportBottom)) {
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
                    }

                    is TextBox -> {
                        val adjustedX = obj.position.x
                        val adjustedY = obj.position.y + metaHeightPx - scrollOffset

                        if (isInViewport(obj.position.y, obj.position.y, viewportTop, viewportBottom)) {
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
                            val lines = obj.text.split("\n")
                            lines.forEachIndexed { index, line ->
                                canvas.drawText(line, adjustedX, adjustedY + baselineAdjustment + index * obj.fontSize.value * density, textPaint)
                            }
                        }
                    }
                }
            }

            return bitmap
        }

        private fun isInViewport(yStart: Float, yEnd: Float, viewportTop: Float, viewportBottom: Float): Boolean {
            return yStart < viewportBottom && yEnd > viewportTop
        }

        private fun getTotalHeight(drawingObjects: List<CanvasObject>, context: Context, pageWidth: Int): Float {
            // Estimate total height needed for all CanvasObjects
            val density = context.resources.displayMetrics.density
            return drawingObjects.maxOfOrNull {
                when (it) {
                    is Line -> maxOf(it.start.y, it.end.y)
                    is TextBox -> it.position.y + it.fontSize.value * density
                    else -> 0f
                }
            } ?: 0f
        }
    }
}
