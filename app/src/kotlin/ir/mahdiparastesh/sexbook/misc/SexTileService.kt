package ir.mahdiparastesh.sexbook.misc

import android.content.Intent
import android.service.quicksettings.TileService
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
