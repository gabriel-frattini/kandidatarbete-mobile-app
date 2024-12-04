package com.example.dat068_tentamina.utilities

import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

class ServerHandler {
    companion object {
        private const val SCHEME = "http"
        private const val HOST = "10.0.2.2"
        private const val PORT = 3000
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

        fun getExam(course: String, anonymousCode: String, callback: (JsonObject?) -> Unit) {
            // Construct the URL
            val url = HttpUrl.Builder()
                .scheme(SCHEME)
                .host(HOST)
                .port(PORT)
                .addPathSegment("getExam")
                .addQueryParameter("course", course)
                .addQueryParameter("anonymousCode", anonymousCode)
                .build()

            // Build the GET request
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            // Execute the request asynchronously
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("GetExam", "Failed to fetch exam", e)
                    callback(null) // Return null in case of failure
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        response.body?.string()?.let { responseBody ->
                            try {
                                // Parse the JSON response
                                val json = JsonParser.parseString(responseBody).asJsonObject
                                callback(json) // Return the parsed JSON
                            } catch (e: Exception) {
                                Log.e("GetExam", "Failed to parse JSON", e)
                                callback(null)
                            }
                        } ?: run {
                            callback(null) // No response body
                        }
                    } else {
                        Log.e("GetExam", "Server returned an error: ${response.message}")
                        callback(null)
                    }
                }
            })
        }
    }
}
