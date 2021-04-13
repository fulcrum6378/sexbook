package org.ifaco.mbcounter.data

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import org.ifaco.mbcounter.Fun.Companion.c
import org.ifaco.mbcounter.Main
import org.ifaco.mbcounter.R
import java.io.*

class Exporter {
    companion object {
        const val reqFolder = 450
        const val reqFile = 750
        val mime = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) "application/octet-stream"
        else "application/json"

        fun whereToExport(that: AppCompatActivity) {
            that.startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = mime
                putExtra(Intent.EXTRA_TITLE, "exported.json")
            }, reqFolder)
        }

        fun export(where: Uri): Boolean = try {
            c.contentResolver.openFileDescriptor(where, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { fos ->
                    fos.write(Gson().toJson(Main.allMasturbation).toByteArray())
                }
            }
            true
        } catch (ignored: Exception) {
            false
        }

        fun importFromWhere(that: AppCompatActivity): Boolean {
            that.startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = mime
            }, reqFile)
            return true
        }

        fun import(where: Uri): Array<Report>? {
            var data: String? = null
            try {
                c.contentResolver.openFileDescriptor(where, "r")?.use { des ->
                    val sb = StringBuffer()
                    FileInputStream(des.fileDescriptor).apply {
                        var i: Int
                        while (read().also { i = it } != -1) sb.append(i.toChar())
                        close()
                    }
                    data = sb.toString()
                }
                data!!
            } catch (e: Exception) {
                Toast.makeText(c, R.string.importOpenError, Toast.LENGTH_LONG).show()
                return null
            }
            return try {
                Gson().fromJson(data, Array<Report>::class.java)
            } catch (e: Exception) {
                Toast.makeText(c, R.string.importReadError, Toast.LENGTH_LONG).show()
                null
            }
        }
    }
}
