package ir.mahdiparastesh.sexbook.data

import android.content.Context
import android.os.Handler
import ir.mahdiparastesh.sexbook.Estimation
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.PageLove
import ir.mahdiparastesh.sexbook.PageSex
import ir.mahdiparastesh.sexbook.Places

class Work(
    val c: Context,
    private val action: Int,
    private val values: List<Any?>? = null,
    val handler: Handler? = when {
        action < 10 -> if (PageSex.handler.value != null) PageSex.handler.value!! else Main.handler
        action < 20 -> if (PageLove.handler.value != null) PageLove.handler.value!! else Main.handler
        action < 30 -> Places.handler ?: Main.handler
        action < 40 -> Estimation.handler ?: Main.handler
        else -> Main.handler
    }
) : Thread() {
    companion object {
        // Report
        const val SCROLL = 6

        // Crush
        const val C_VIEW_ONE = 10
        const val C_VIEW_ALL = 11
        const val C_INSERT_ONE = 12
        const val C_UPDATE_ONE = 14
        const val C_DELETE_ONE = 15
        const val C_VIEW_ALL_PEOPLE = 16

        // Place
        const val P_VIEW_ONE = 20
        const val P_INSERT_ONE = 22
        const val P_UPDATE_ONE = 24
        const val P_DELETE_ONE = 25

        // Guess
        const val G_VIEW_ONE = 30
        const val G_INSERT_ONE = 32
        const val G_UPDATE_ONE = 34
        const val G_DELETE_ONE = 35

        // PURPOSES
        const val ADD_NEW_ITEM = 0

        // Other
        const val TIMEOUT = 5000L
        const val SPECIAL_ADD = 100
        const val CRUSH_ALTERED = 101
        // const val ADMOB_LOADED = 10X
    }

    override fun run() {
        val db = Database.Builder(c).build()
        val dao = db.dao()
        when (action) {
            // Crush
            C_VIEW_ONE -> if (!values.isNullOrEmpty()) handler?.obtainMessage(
                action,
                if (values.size > 1) values[1] as Int else 0,
                if (values.size > 2) values[2] as Int else 0,
                dao.cGet(values[0] as String)
            )?.sendToTarget()

            C_VIEW_ALL -> handler?.obtainMessage(action, dao.cGetAll())?.sendToTarget()
            C_VIEW_ALL_PEOPLE -> handler?.obtainMessage(action, dao.cGetPeople())?.sendToTarget()

            C_INSERT_ONE -> {
                var result: Long = -1
                if (!values.isNullOrEmpty()) result = dao.cInsert(values[0] as Crush)
                handler?.obtainMessage(action, result)?.sendToTarget()
                Main.handler!!.obtainMessage(CRUSH_ALTERED).sendToTarget()
            }

            C_UPDATE_ONE -> if (!values.isNullOrEmpty()) {
                dao.cUpdate(values[0] as Crush)
                handler?.obtainMessage(
                    action, /*if (values.size > 1) values[1] as Int else*/ 0,
                    if (values.size > 2) values[2] as Int else 0, values[0]
                )?.sendToTarget()
                Main.handler!!.obtainMessage(CRUSH_ALTERED).sendToTarget()
            }

            C_DELETE_ONE -> if (!values.isNullOrEmpty()) {
                dao.cDelete(values[0] as Crush)
                handler?.obtainMessage(
                    action, /*if (values.size > 1) values[1] as Int else*/ 0,
                    if (values.size > 2) values[2] as Int else 0, values[0]
                )?.sendToTarget()
                Main.handler!!.obtainMessage(CRUSH_ALTERED).sendToTarget()
            }


            // Place
            P_VIEW_ONE -> if (!values.isNullOrEmpty()) handler?.obtainMessage(
                action,
                if (values.size > 1) values[1] as Int else 0,
                if (values.size > 2) values[2] as Int else 0,
                dao.pGet(values[0] as Long)
            )?.sendToTarget()

            P_INSERT_ONE -> {
                var result: Long = -1
                if (!values.isNullOrEmpty()) result = dao.pInsert(values[0] as Place)
                handler?.obtainMessage(action, result)?.sendToTarget()
            }

            P_UPDATE_ONE -> if (!values.isNullOrEmpty()) {
                dao.pUpdate(values[0] as Place)
                handler?.obtainMessage(
                    action, if (values.size > 1) values[1] as Int else 0,
                    if (values.size > 2) values[2] as Int else 0, null
                )?.sendToTarget()
            }

            P_DELETE_ONE -> if (!values.isNullOrEmpty()) {
                val old = values[0] as Place
                dao.pDelete(old)
                for (mig in dao.rGetByPlace(old.id))
                    dao.rUpdate(mig.apply { plac = values[1] as Long })
                handler?.obtainMessage(
                    action, if (values.size > 2) values[2] as Int else 0,
                    if (values.size > 3) values[3] as Int else 0, null
                )?.sendToTarget()
            }


            // Guess
            G_VIEW_ONE -> if (!values.isNullOrEmpty()) handler?.obtainMessage(
                action,
                if (values.size > 1) values[1] as Int else 0,
                if (values.size > 2) values[2] as Int else 0,
                dao.gGet(values[0] as Long)
            )?.sendToTarget()

            G_INSERT_ONE -> {
                var result: Long = -1
                if (!values.isNullOrEmpty()) result = dao.gInsert(values[0] as Guess)
                handler?.obtainMessage(action, result)?.sendToTarget()
            }

            G_UPDATE_ONE -> if (!values.isNullOrEmpty()) {
                dao.gUpdate(values[0] as Guess)
                handler?.obtainMessage(
                    action, if (values.size > 1) values[1] as Int else 0,
                    if (values.size > 2) values[2] as Int else 0, null
                )?.sendToTarget()
            }

            G_DELETE_ONE -> if (!values.isNullOrEmpty()) {
                dao.gDelete(values[0] as Guess)
                handler?.obtainMessage(
                    action, if (values.size > 1) values[1] as Int else 0,
                    if (values.size > 2) values[2] as Int else 0, null
                )?.sendToTarget()
            }
        }
        db.close()
    }
}
