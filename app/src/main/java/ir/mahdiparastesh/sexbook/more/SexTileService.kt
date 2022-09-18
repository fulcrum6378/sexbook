package ir.mahdiparastesh.sexbook.more

import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import ir.mahdiparastesh.sexbook.Main

class SexTileService : TileService() { // qsTile
    override fun onClick() {
        startActivity(
            Intent(applicationContext, Main::class.java)
                .setAction(Main.Action.ADD.s)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
