package com.example.dat068_tentamina.externalStorage


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
//import com.example.dat068_tentamina.ui.externalStorage

class ExternalStorage {
    fun checkPermission():Boolean{
        return Environment.isExternalStorageManager()

    }

    private fun requestPermission(context: Context)
    {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
    }
    fun writeToExternal(context: Context){
        if(checkPermission())
        {
            // TODO: Write to file
        }
        else{
            requestPermission(context)
        }
    }
    fun readFromExternal(context: Context){
        if(checkPermission())
        {
            // TODO: Write from SD
        }
        else{
            requestPermission(context)
        }
    }
}