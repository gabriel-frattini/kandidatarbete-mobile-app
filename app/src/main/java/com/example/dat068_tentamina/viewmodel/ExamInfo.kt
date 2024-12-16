import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Import viewModelScope
import com.example.dat068_tentamina.utilities.ServerHandler
import com.example.dat068_tentamina.viewmodel.TentaViewModel
import kotlinx.coroutines.launch // Import launch
import org.json.JSONArray
import java.io.File

class ExamInfo() : ViewModel() {
    private val apiHelper = ServerHandler()
    private val questions = mutableListOf<String>()
    private lateinit var tentaViewModel: TentaViewModel
    private var onDataFetched: (() -> Unit)? = null
    var user = ""
    var personalID = ""
    var course = ""

    fun setOnDataFetched(callback: () -> Unit) {
        onDataFetched = callback
    }

    fun fetchData(courseCode: String, anonymousCode: String) {
        viewModelScope.launch {
            try {
                val result = apiHelper.getExam(courseCode, anonymousCode)
                result?.let {
                    Log.d("GET Request", "JSON: $it")
                    course = it.getString("examID")
                    val info = it.getJSONObject("anonymousCode")
                    user = info.getString("anonymousCode")
                    personalID = info.getString("birthYear")
                    if (it.has("Error")) {
                        println("Error in response: ${it.getString("Error")}")
                        return@let
                    }
                    var questionLength = 1
                    // Check if "questions" exists and is an array
                    if (it.has("questions") && it.get("questions") is JSONArray) {
                        val json = it.getJSONArray("questions")
                        questions.clear() // Ensure previous data is removed

                        for (i in 0 until json.length()) {
                            questions.add(json.getString(i))
                        }
                        questionLength = questions.size
                    }
                    tentaViewModel = TentaViewModel().apply {
                        addQuestions(questionLength)
                    }
                    // Notify data fetched
                    onDataFetched?.invoke()
                } ?: Log.d("GET Request", "Failed to fetch exam")
            } catch (e: Exception) {
                println("Error during GET request: ${e.message}")
            }
        }
    }


    fun sendPdf(pdfFile: File, course: String, username: String) {
        apiHelper.sendPdfToServer(pdfFile, "Math 101", "student_username")
    }

    fun getTentaModel() : TentaViewModel{
        return tentaViewModel
    }
}
