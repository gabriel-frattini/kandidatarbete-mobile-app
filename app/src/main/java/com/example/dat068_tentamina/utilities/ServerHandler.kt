package com.example.dat068_tentamina.utilities

import android.telecom.Call
import android.util.Log
import androidx.tracing.perfetto.handshake.protocol.Response
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody

import java.io.File
import java.io.IOException


class ServerHandler {
    companion object {
        fun sendPdfToServer(pdfFile: File, course: String, username: String) {
            val client = OkHttpClient()

            // Create the multipart body
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("course", course)
                .addFormDataPart("username", username)
                .addFormDataPart("file", pdfFile.name, pdfFile.asRequestBody("application/pdf".toMediaType()))
                .build()

            // Create the HTTP request
            val request = Request.Builder()
                .url("http://10.0.2.2:3000/submit")
                .post(requestBody)
                .build()

            // Enqueue the request
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    Log.e("SubmitExam", "Failed to send PDF", e)
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    if (response.isSuccessful) {
                        Log.d("SubmitExam", "PDF submitted successfully")
                    } else {
                        Log.e("SubmitExam", "Server returned an error: ${response.message}")
                    }
                }
            })
        }
    }
}
