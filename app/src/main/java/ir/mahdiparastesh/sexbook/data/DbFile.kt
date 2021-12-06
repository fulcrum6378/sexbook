package ir.mahdiparastesh.sexbook.data

import android.annotation.SuppressLint
import ir.mahdiparastesh.sexbook.Main
import java.io.File

@SuppressLint("SdCardPath")
class DbFile(which: Triple) : File(
    "/data/data/" + Main::class.java.`package`!!.name + "/databases/" + DATABASE + which.s
) {
    companion object {
        const val DATABASE = "sexbook.db"
    }

    enum class Triple(val s: String) {
        MAIN(""), SHARED_MEMORY("-shm"), WRITE_AHEAD_LOG("-wal")
    }
}
