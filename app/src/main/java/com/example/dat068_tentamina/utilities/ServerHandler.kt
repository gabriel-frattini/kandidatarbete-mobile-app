package com.example.dat068_tentamina.utilities

import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException

class ServerHandler {
    private val SCHEME = "http"
    private val HOST = "10.0.1.45"
    private val PORT = 3000
    private val client = OkHttpClient()

    fun sendPdfToServer(pdfFile: File, course: String, username: String) {
        // Create the multipart body
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("course", course)
            .addFormDataPart("username", username)
            .addFormDataPart(
                "file",
                pdfFile.name,
                pdfFile.asRequestBody("application/pdf".toMediaType())
            )
            .build()

        // Construct the URL
        val url = HttpUrl.Builder()
            .scheme(SCHEME)
            .host(HOST)
            .port(PORT)
            .addPathSegment("submit")
            .build()

        // Create the HTTP request
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        // Enqueue the request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("SubmitExam", "Failed to send PDF", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("SubmitExam", "PDF submitted successfully")
                } else {
                    Log.e("SubmitExam", "Server returned an error: ${response.message}")
                }
            }
        })
    }

    suspend fun getExam(courseCode: String, anonymousCode: String): JSONObject?  {
        // Construct the URL with query parameters
        val url = HttpUrl.Builder()
            .scheme("http")
            .host(HOST)
            .port(PORT)
            .addPathSegment("getExam") // Replace with your endpoint
            .addQueryParameter("courseCode", courseCode)
            .addQueryParameter("anonymousCode", anonymousCode)
            .build()

        // Build the request
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        // Execute the request
        return withContext(Dispatchers.IO) { // Perform the network request on the IO dispatcher
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val jsonString = response.body?.string() // Get the response body as a string
                    jsonString?.let { JSONObject(it) } // Parse the string into a JSONObject
                } else {
                    null // Handle non-successful HTTP responses
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null // Handle exceptions
            }
        }
    }
}
