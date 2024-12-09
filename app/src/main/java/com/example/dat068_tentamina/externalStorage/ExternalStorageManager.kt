package com.example.dat068_tentamina.externalStorage

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class ExternalStorageManager {
    // checks if we can read and write to externalStorage, we should be able to do so as the tablet should handle this.
    val isExternalStorageWritable : Boolean get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    val backUpFileName = "ExamBackUp.txt"

    fun getExternalStorageVolumes(context: Context): File {
        val externalStorageVolumes = ContextCompat.getExternalFilesDirs(context, null)
        return externalStorageVolumes[0]
    }
    //create file to store, if able to store it will return true otherwise false
    private fun createFile(context: Context, fileName: String?) :Boolean
    {
        //val appSpecificExternalDir = fileName?.let { File(context.getExternalFilesDir(fileName),it) }
        //return appSpecificExternalDir!=null
        if (fileName.isNullOrEmpty()) return false

        val appSpecificExternalDir = context.getExternalFilesDir(null) ?: return false

        val file = File(appSpecificExternalDir, fileName)
        return try {
            // Attempt to create the file
            if (!file.exists()) {
                file.createNewFile() // Create the file on disk
            }
            true // Return true if file was successfully created or already exists
        } catch (e: IOException) {
            e.printStackTrace()
            false // Return false if an error occurred
        }
    }
    // this one works.
    fun getFile(context: Context, fileName: String?): File? {
        return fileName?.let {
            File(context.getExternalFilesDir(null), it)
        }
    }
    // this one works
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
    //this one works
    fun read(file: File?): StringBuilder{
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
    // this one works
    fun writeToBackUp(context: Context, data: JSONObject){

        val createdFile = createFile(context,backUpFileName)
        if(createdFile)
        {
            Toast.makeText(context,"BackUp Created", Toast.LENGTH_SHORT).show()
            val file : File? = getFile(context,backUpFileName)

            write(file,data.toString())
            Toast.makeText(context,"BackUp Written To File",Toast.LENGTH_SHORT).show()

            val stringBuilder = read(file)
            Toast.makeText(context, stringBuilder.toString(),Toast.LENGTH_LONG).show()

        }
        else{
            Toast.makeText(context,"Failed in making external backup", Toast.LENGTH_SHORT).show()
        }
    }
//works
    fun readFromBackUp(context: Context): JSONObject?{
        val file = getFile(context, backUpFileName)
        if(backUpExists(context))
        {
            val stringBuilder = read(file)
            return JSONObject(stringBuilder.toString())
        }
        return null
    }
    // works
    fun backUpExists(context: Context): Boolean {
        val appSpecificExternalDir = context.getExternalFilesDir(null) ?: return false
        val file = File(appSpecificExternalDir,backUpFileName)
        return file.exists()
    }


}