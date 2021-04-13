package org.ifaco.mbcounter.data

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import org.ifaco.mbcounter.Fun.Companion.c
import org.ifaco.mbcounter.Main
import org.ifaco.mbcounter.R
import java.io.*
import java.lang.Exception

class Exporter {
    companion object {
        const val reqFolder = 450
        const val reqFile = 750
        val MIME = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) "application/octet-stream"
        else "application/json"

        fun whereToExport(that: AppCompatActivity) {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = MIME
                putExtra(Intent.EXTRA_TITLE, "exported.json")
            }
            that.startActivityForResult(intent, reqFolder)
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
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = MIME
            }
            that.startActivityForResult(intent, reqFile)
            return true
        }

        fun import(where: Uri): Array<Report>? {
            var r: JsonReader? = null
            try {
                c.contentResolver.openFileDescriptor(where, "r")?.use {
                    r = JsonReader(InputStreamReader(FileInputStream(it.fileDescriptor)))
                }
            } catch (e: Exception) {
                Toast.makeText(
                    c, c.resources.getString(R.string.importOpenError), Toast.LENGTH_LONG
                ).show()
            }
            return try {
                Gson().fromJson<Array<Report>>(r, Array<Report>::class.java)
            } catch (e: Exception) {
                Toast.makeText(
                    c, c.resources.getString(R.string.importReadError), Toast.LENGTH_LONG
                ).show()
                null
            }
        }
    }
}
