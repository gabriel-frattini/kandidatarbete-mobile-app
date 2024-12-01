import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import com.example.dat068_tentamina.model.CanvasObject
import com.example.dat068_tentamina.model.Line
import java.io.File



class PdfConverter {
    companion object {
        fun createPdfFromAnswers(
            answers: MutableMap<Int, List<CanvasObject>>,
            pageWidth: Int,
            pageHeight: Int,
            context: Context // Accept Context here
        ): File {
            val pdfDocument = PdfDocument()
            var yOffset = 0
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()

            answers.forEach { (_, drawingObjects) ->
                val bitmap = createBitmapFromCanvasObject(drawingObjects, pageWidth, pageHeight)
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                canvas.drawBitmap(bitmap, 0f, yOffset.toFloat(), null)
                yOffset += pageHeight // Adjust for spacing

                pdfDocument.finishPage(page)
            }

            // Use context.cacheDir here
            val file = File(context.cacheDir, "exam_submission.pdf")
            pdfDocument.writeTo(file.outputStream())
            pdfDocument.close()

            return file
        }


        fun createBitmapFromCanvasObject(
            drawingObjects: List<CanvasObject>,
            width: Int,
            height: Int
        ): Bitmap {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK       // Set the line color
                strokeWidth = 5f                          // Set the stroke width in pixels
                style = android.graphics.Paint.Style.STROKE // Set the paint style (STROKE, FILL, or FILL_AND_STROKE)
                strokeCap = android.graphics.Paint.Cap.ROUND // Set the cap style for line ends
            }
            drawingObjects.forEach { obj ->
                when (obj) {
                    is Line -> {
                        // Configure paint for this line
                        paint.color = obj.color.toArgb()       // Convert Jetpack Compose Color to Android Color
                        paint.strokeWidth = obj.strokeWidth.value // Convert Dp to pixels
                        paint.strokeCap = when (obj.cap) {
                            StrokeCap.Round -> android.graphics.Paint.Cap.ROUND
                            StrokeCap.Square -> android.graphics.Paint.Cap.SQUARE
                            else -> android.graphics.Paint.Cap.BUTT
                        }
                        canvas.drawLine(obj.start.x, obj.start.y, obj.end.x, obj.end.y, paint)
                    }


                    // Handle other CanvasObject types like Circle, Rectangle, etc.
                }
            }
            return bitmap
        }
    }

}
