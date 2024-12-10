import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Import viewModelScope
import com.example.dat068_tentamina.utilities.ServerHandler
import kotlinx.coroutines.launch // Import launch
import java.io.File

class ExamInfo : ViewModel() {
    private val apiHelper = ServerHandler()

    fun fetchData(courseCode: String, anonymousCode: String) {
        // Use viewModelScope to launch a coroutine
        viewModelScope.launch {
            val result = apiHelper.getExam(courseCode, anonymousCode)
            result?.let {
                println("JSON Response: $it")
            } ?: println("Failed to fetch data")
        }
    }

    fun sendPdf(pdfFile: File, course: String, username: String) {
        apiHelper.sendPdfToServer(pdfFile, "Math 101", "student_username")
    }
}
