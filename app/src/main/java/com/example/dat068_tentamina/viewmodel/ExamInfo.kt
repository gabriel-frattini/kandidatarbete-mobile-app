import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Import viewModelScope
import com.example.dat068_tentamina.utilities.ServerHandler
import com.example.dat068_tentamina.viewmodel.TentaViewModel
import kotlinx.coroutines.launch // Import launch
import org.json.JSONArray
import java.io.File
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.dp
import com.example.dat068_tentamina.externalStorage.ExternalStorageManager
import com.example.dat068_tentamina.model.CanvasObject
import com.example.dat068_tentamina.model.TextBox
import com.example.dat068_tentamina.model.Line
import com.example.dat068_tentamina.model.serializable.Answer
import com.example.dat068_tentamina.model.serializable.SerializableTextbox
import com.example.dat068_tentamina.model.serializable.SerializableLine
import com.example.dat068_tentamina.utilities.CanvasObjectSerializationUtils.toColor
import com.example.dat068_tentamina.utilities.CanvasObjectSerializationUtils.toOffset
import com.example.dat068_tentamina.utilities.CanvasObjectSerializationUtils.toSerializable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.json.JSONObject


class ExamInfo() : ViewModel() {
    private val apiHelper = ServerHandler()
    private var questions = mutableListOf<String>()
    private val externalStorageManager = ExternalStorageManager()
    private val _recoveryMode = MutableStateFlow(false)
    private lateinit var tentaViewModel: TentaViewModel
    private var onDataFetched: (() -> Unit)? = null
    var user = ""
    var personalID = ""
    var course = ""
    var storageObject = JSONObject()
    val job = Job()
    val scope = CoroutineScope(Dispatchers.Default + job)
    val recoveryMode: StateFlow<Boolean> get() = _recoveryMode


    fun enableRecoveryMode() {
        _recoveryMode.value = true
    }

    fun disableRecoveryMode() {
        _recoveryMode.value = false
    }


    fun createSerialization(): List<Answer> {
        return tentaViewModel.getAnswers().map { (questionId, canvasObjects) ->
            Answer(
                questionId = questionId,
                canvasObjects = canvasObjects.map { it.toSerializable() }
            )
        }
    }

    private fun getStorageObjectFromExternal(context : Context): JSONObject? {
        //fel hantering behövs, vad om det ej finns en fil
        return externalStorageManager.readFromBackUp(context)
    }

    // check if there is a recoverable exam on external storage for said examID and anonymousCode
    fun continueAlreadyStartedExam(textMeasurer: TextMeasurer, context : Context): Boolean {
        Log.d("Backup", "Attempting to continue an already started exam")

        val storedObject = getStorageObjectFromExternal(context)
        if (storedObject == null) {
            Log.d("Backup", "No stored object found")
            return false
        }

        Log.d("Backup", "Stored object retrieved: $storedObject")

        val answersJsonArray = storedObject.optJSONArray("answers")?.toString()
        if (answersJsonArray == null) {
            Log.d("Backup", "No answers JSON array found in stored object")
            return false
        }

        Log.d("Backup", "Answers JSON array extracted: $answersJsonArray")

        return try {
            val deserializedAnswers: List<Answer> = Json.decodeFromString(answersJsonArray)
            Log.d("Backup", "Deserialized answers: $deserializedAnswers")

            // Step 1: Determine the range of questions dynamically
            val allQuestionIds = deserializedAnswers.map { it.questionId }
            val maxQuestionId = allQuestionIds.maxOrNull() ?: 0
            val questionRange = 1..maxQuestionId

            // Step 2: Initialize all questions dynamically as empty lists
            tentaViewModel.questions.clear()
            questionRange.forEach { questionId ->
                tentaViewModel.questions[questionId] = emptyList()
            }
            Log.d("Backup", "Initialized questions dynamically: ${tentaViewModel.questions}")

            // Step 3: Populate questions with recovered objects
            deserializedAnswers.forEach { answer ->
                Log.d("Backup", "Processing answer: $answer")

                val canvasObjects: List<CanvasObject> = answer.canvasObjects.map { serializedObject ->
                    Log.d("Backup", "Processing canvas object: $serializedObject")
                    when (serializedObject) {
                        is SerializableLine -> Line(
                            start = serializedObject.start.toOffset(),
                            end = serializedObject.end.toOffset(),
                            color = serializedObject.color.toColor() ,
                            strokeWidth = serializedObject.strokeWidth.dp
                        )

                        is SerializableTextbox -> {
                            Log.d("Backup", "SerializableTextbox detected: $serializedObject")
                            TextBox(
                                position = serializedObject.position.toOffset(),
                                textLayout = textMeasurer.measure(AnnotatedString(serializedObject.text)),
                                text = serializedObject.text
                            )
                        }
                        else -> {
                            Log.e("Backup", "Unknown SerializableCanvasObject type: $serializedObject")
                            throw IllegalArgumentException("Unknown SerializableCanvasObject type")
                        }
                    }
                }

                tentaViewModel.questions[answer.questionId] = canvasObjects
                Log.d("Backup", "Objects for question ${answer.questionId} added successfully")
            }

            Log.d("Backup", "Recovery process completed. Questions: ${tentaViewModel.questions}")
            true // Successfully recovered
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Backup", "Failed to deserialize exam data: ${e.message}")
            false
        }
    }

    fun startBackUp(context : Context ){
        externalStorageManager.writeToSDCardBackUp(context,storageObject)
        startPerodicallyUpdatingExternalStorage(scope, context)
    }

    private fun startPerodicallyUpdatingExternalStorage(scope: CoroutineScope, context : Context) {
        scope.launch {
            while (isActive) { // Ensures the coroutine can be canceled
                updateStorageObject(context)
                delay(30 * 1000L) // 30 second delay
                Log.d("ExamInfo", "{${tentaViewModel.getAnswers()}}")
            }
        }
    }

    private fun updateStorageObject(context : Context) {
        try {
            // Get the serialized answers as a list of Answer objects
            val serializedAnswers = createSerialization()

            // Convert the serialized answers to JSON format
            val answersJsonString = Json.encodeToString(ListSerializer(Answer.serializer()), serializedAnswers)

            // Update the storageObject with the serialized data
            storageObject = JSONObject().apply {
                put("examID", course)
                put("anonymousCode", user)
                put("answers", JSONArray(answersJsonString)) // Convert the JSON string into a JSONArray
            }

            // Write the updated storageObject to external storage
            externalStorageManager.writeToSDCardBackUp(context, storageObject)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ExamError", "Failed to update backup: ${e.message}")
        }
    }

    fun verifyBackupCredentials(aCode: String, exId: String, context : Context): Boolean {
        Log.d("Backup", "Verifying backup credentials for anonymousCode: $aCode and examID: $exId")

        // Step 1: Check if a backup file exists
        if (!externalStorageManager.sdCardBackUpExists(context)) {
            Log.d("Backup", "No backup file exists")
            return false
        }

        // Step 2: Retrieve the stored backup object
        val storedObject = getStorageObjectFromExternal(context) ?: return false
        Log.d("Backup", "Retrieved stored object: $storedObject")

        // Step 3: Extract and verify credentials
        val storedExamID: String = storedObject.optString("examID", "")
        val storedAnonymousCode: String = storedObject.optString("anonymousCode", "")

        Log.d("Backup", "Stored examID: $storedExamID, Stored anonymousCode: $storedAnonymousCode")

        val credentialsMatch = storedExamID == exId && storedAnonymousCode == aCode
        if (credentialsMatch) {
            Log.d("Backup", "Credentials match!")
        } else {
            Log.d("Backup", "Credentials do not match")
        }

        return credentialsMatch
    }

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
                    personalID = info.getString("birthID")
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


    fun sendPdf(pdfFile: File) {
        apiHelper.sendPdfToServer(pdfFile, course, user)
    }

    fun getTentaModel() : TentaViewModel{
        return tentaViewModel
    }

    fun clearInfo() {
        questions = mutableListOf<String>()
        tentaViewModel = TentaViewModel()
        user = ""
        personalID = ""
        course = ""
    }
}

