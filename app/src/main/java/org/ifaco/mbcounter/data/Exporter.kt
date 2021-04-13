package org.ifaco.mbcounter.data

import android.net.Uri
import org.ifaco.mbcounter.more.FileUtils
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import org.ifaco.mbcounter.Fun.Companion.c
import org.ifaco.mbcounter.Main
import org.ifaco.mbcounter.R
import java.io.*
import java.lang.Exception

class Exporter {
    companion object {
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

        fun import(uri: Uri): Array<Report>? {
            var r: JsonReader? = null
            try {
                r = JsonReader(InputStreamReader(FileInputStream(File(FileUtils.getPath(c, uri)))))
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