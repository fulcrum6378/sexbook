package org.ifaco.mbcounter.data

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import org.ifaco.mbcounter.Fun.Companion.c
import org.ifaco.mbcounter.Main
import org.ifaco.mbcounter.R
import java.io.*

class Exporter {
    companion object {
        val mime = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) "application/octet-stream"
        else "application/json"

        fun export(that: AppCompatActivity, onani: ArrayList<Report>?): Boolean {
            if (onani == null) {
                Toast.makeText(c, R.string.noRecords, Toast.LENGTH_LONG).show(); return true; }
            that.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult
                val bExp = try {
                    c.contentResolver.openFileDescriptor(it.data!!.data!!, "w")?.use { des ->
                        FileOutputStream(des.fileDescriptor).use { fos ->
                            fos.write(Gson().toJson(onani).toByteArray())
                        }
                    }
                    true
                } catch (ignored: Exception) {
                    false
                }
                Toast.makeText(
                    c, if (bExp) R.string.exportDone else R.string.exportUndone, Toast.LENGTH_LONG
                ).show()
            }.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = mime
                putExtra(Intent.EXTRA_TITLE, "exported.json")
            })
            return true
        }

        fun import(that: AppCompatActivity): Boolean {
            that.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult
                var data: String? = null
                try {
                    c.contentResolver.openFileDescriptor(it.data!!.data!!, "r")?.use { des ->
                        val sb = StringBuffer()
                        FileInputStream(des.fileDescriptor).apply {
                            var i: Int
                            while (read().also { r -> i = r } != -1) sb.append(i.toChar())
                            close()
                        }
                        data = sb.toString()
                    }
                    data!!
                } catch (e: Exception) {
                    Toast.makeText(c, R.string.importOpenError, Toast.LENGTH_LONG).show()
                    return@registerForActivityResult
                }
                try {
                    Gson().fromJson(data, Array<Report>::class.java)
                } catch (e: Exception) {
                    Toast.makeText(c, R.string.importReadError, Toast.LENGTH_LONG).show()
                    return@registerForActivityResult
                }
                Work(c, Main.handler, Work.REPLACE_ALL, data!!.toList()).start()
            }.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = mime
            })
            return true
        }
    }
}
