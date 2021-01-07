package org.ifaco.mbcounter.data

import com.google.gson.Gson
import org.ifaco.mbcounter.Main
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Exporter {
    companion object {
        fun export(path: String): Boolean {
            val file = File(path, "exported.json")
            if (file.exists()) file.delete()
            try {
                FileOutputStream(file, false).apply {
                    write(Gson().toJson(Main.allMasturbation).toByteArray())
                    close()
                }
            } catch (ignored: IOException) {
            }
            return file.exists()
        }
    }
}