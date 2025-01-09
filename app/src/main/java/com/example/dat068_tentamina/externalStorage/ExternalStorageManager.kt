package com.example.dat068_tentamina.externalStorage

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class ExternalStorageManager {
    //The name of the backup file. This can be changed here easily and it should still work.
    private val backUpFileName = "ExamBackUp.txt"

    // Returns the the filedirectory of the SD card on the tablet.
    private fun getExternalStorageVolumes(context: Context): File {
        val externalStorageVolumes = ContextCompat.getExternalFilesDirs(context, null)
        return externalStorageVolumes[1] // the SD card was placed on 1
    }
    // Checks if the SD card is mounted and we are able to use it
    fun isSDCardAvailable(context: Context): Boolean {
        val externalDirs = ContextCompat.getExternalFilesDirs(context, null)
        // Check if there's more than one directory and the second one is not null
        return externalDirs.size > 1 && externalDirs[1] != null
    }
    // creates a file on the SD card if SD card is mounted, will return false if it was unable to create file
    private fun createFileOnSDCard(context: Context, fileName: String?): Boolean {
        if (fileName.isNullOrEmpty() || isSDCardAvailable(context)==false) return false

        val sdCardDir = getExternalStorageVolumes(context)

        val file = File(sdCardDir, fileName)
        return try {
            if (!file.exists()) {
                file.createNewFile()
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
    // getFile from SD card
    private fun getFile(context: Context, fileName: String?): File? {
        val sdCardDir = getExternalStorageVolumes(context)
        return fileName?.let {
            File(sdCardDir, it)
        }
    }
    // writes data to file
    private fun write(file: File?, data: String?)
    {
        try{
            val fileWriter = FileWriter(file)
            fileWriter.append(data)
            fileWriter.flush()
            fileWriter.close()
        }catch (e : IOException){
            e.printStackTrace()
        }
    }
    // reads data from file
    private fun read(file: File?): StringBuilder{
        var line: String?
        val stringBuilder = StringBuilder()
        try{
            val fileReader = FileReader(file)
            val bufferedReader = BufferedReader(fileReader)
            while(bufferedReader.readLine().also { line = it } != null)
            {
                stringBuilder.append(line)
            }
        }catch (e : IOException)
        {
            e.printStackTrace()
        }
        return stringBuilder
    }
    // Writes to BackUp on SDcard if it is mounted
    fun writeToSDCardBackUp(context: Context, data: JSONObject) {
        val sdCardFileCreated = createFileOnSDCard(context, backUpFileName)
        if (sdCardFileCreated) {
            val sdCardFile = getFile(context, backUpFileName)
            write(sdCardFile, data.toString())
        } else {
            Log.d("ExamInfo", "Failed to create backup on SD card")
        }
    }
    // Reads from backup SDcard and returns JSONOBJECT if it finds one
    fun readFromBackUp(context: Context): JSONObject?{
        val file = getFile(context, backUpFileName)
        if(sdCardBackUpExists(context))
        {
            val stringBuilder = read(file)
            return JSONObject(stringBuilder.toString())
        }
        return null
    }
    //Checks if there is a backup on SDcard, if the SD card is mounted
    fun sdCardBackUpExists(context: Context): Boolean {
        if (isSDCardAvailable(context))
        {
            val sdCardDir = getExternalStorageVolumes(context)
            val file = File(sdCardDir, backUpFileName)
            return file.exists()
        }
        return false
    }
}