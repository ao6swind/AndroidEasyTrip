package com.easy.trip.utilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

object ImageUtils {
    /**
     * 將Bitmap存成檔案
     */
    fun saveBitmapToFile(context: Context, bitmap: Bitmap, filename: String) {
        val inputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, inputStream)
        val bytes = inputStream.toByteArray()
        val outputStream: FileOutputStream
        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE)
            outputStream.write(bytes)
            outputStream.close()
            Log.d("com.easy.trip.utilities.ImageUtils", "save image finish")
        } catch (e: Exception) {
            Log.e("com.easy.trip.utilities.ImageUtils", "save image failed: ${e.message}")
        }
    }

    /**
     * 將實體檔案轉成Bitmap
     */
    fun loadBitmapFromFile(context: Context, filename: String): Bitmap? {
        val filePath = File(context.filesDir, filename).absolutePath
        return BitmapFactory.decodeFile(filePath)
    }

    /**
     * 刪除檔案
     */
    fun removeFile(context: Context, filename: String): Boolean {
        when(File(context.filesDir, filename).exists()) {
            true -> return File(context.filesDir, filename).delete()
            false -> return false
        }
    }
}