package com.example.dat068_tentamina.externalStorage

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.dat068_tentamina.ui.externalStorage
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class ExternalStorageManager {
    // checks if we can read and write to externalStorage, we should be able to do so as the tablet should handle this.
    val isExternalStorageWritable : Boolean get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    fun getExternalStoragePath(context: Context): File {
        val externalStorageVolumes = ContextCompat.getExternalFilesDirs(context, null)
        return externalStorageVolumes[0]
    }
    //create file to store
    fun createFile(context: Context, fileName: String?) :Boolean
    {

        val appSpecificExternalDir = fileName?.let { File(context.getExternalFilesDir(fileName),it) }
        return appSpecificExternalDir!=null

    }
    fun getFile(context: Context, fileName: String?): File? {
        return fileName?.let { File(context.getExternalFilesDir(fileName),it) }
    }
    fun write(file: File?, data: String?)
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
    fun createNewFile(context: Context){
        for(i in 0..100)
        {
            val createdFile = createFile(context,i.toString())
            if(createdFile)
            {
                Toast.makeText(context,"FileCreated", Toast.LENGTH_SHORT).show()
                val file : File? = getFile(context,i.toString())

                write(file,"Hejsan detta är min data jag sparat!")
            }
            else{
                Toast.makeText(context,"Failed in creating files",Toast.LENGTH_LONG).show()
            }
            /*
        val createdFile = createFile(context,"ExamBackup")
        if(createdFile)
        {
            Toast.makeText(context,"FileCreated", Toast.LENGTH_SHORT).show()
            val file : File? = getFile(context,"ExamBackUp")

            write(file,"Hejsan detta är min data jag sparat!")
            Toast.makeText(context,"Data written to file",Toast.LENGTH_SHORT).show()

            val stringBuilder = read(file)
            Toast.makeText(context, stringBuilder.toString(),Toast.LENGTH_LONG).show()

        }
        else{
            Toast.makeText(context,"FailedInCreatingFile", Toast.LENGTH_SHORT).show()
        }

             */
        }
    }
}