package ir.mahdiparastesh.sexbook.ctrl

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.sexbook.BuildConfig
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseDialog
import ir.mahdiparastesh.sexbook.databinding.DataControlsBinding
import ir.mahdiparastesh.sexbook.page.Main
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DataControls : BaseDialog<Main>() {

    var dropbox: Dropbox? = null

    companion object : BaseDialogCompanion() {
        private const val TAG = "data_controls"

        fun create(c: Main) {
            if (isDuplicate()) return
            DataControls().show(c.supportFragmentManager, TAG)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bd = DataControlsBinding.inflate(c.layoutInflater)
        bd.dataExport.setOnClickListener { c.exporter.launchExport() }
        bd.dataImport.setOnClickListener { c.exporter.launchImport() }
        bd.dataSend.setOnClickListener { c.exporter.send() }

        @Suppress("SimplifyBooleanWithConstants", "KotlinConstantConditions")
        if (BuildConfig.BUILD_TYPE != "mahdi") {
            bd.dropboxLogin.isVisible = false
            bd.dropboxSync.isVisible = false
        } else
            bd.updateDropbox()

        return MaterialAlertDialogBuilder(c).apply {
            setTitle(R.string.dataControls)
            setView(bd.root)
            setCancelable(true)
        }.show()
    }

    override fun onResume() {
        super.onResume()
        dropbox?.onResume()
    }

    /** Checks whether the user has logged in to Dropbox and update the UI accordingly. */
    @SuppressLint("SetTextI18n")
    private fun DataControlsBinding.updateDropbox() {
        if (dropbox == null) dropbox = Dropbox(c.c, c.exporter)
        val auth = dropbox!!.isAuthenticated()

        dropboxSync.isVisible = auth
        dropboxLogin.text =
            getString(if (!auth) R.string.dropboxSignIn else R.string.dropboxSignOut)
        dropboxLogin.setOnClickListener(
            if (!auth) View.OnClickListener {
                dropbox!!.login(c) { updateDropbox() }
            } else View.OnClickListener {
                MaterialAlertDialogBuilder(c).apply {
                    setTitle(R.string.dropboxSignOut)
                    setMessage(R.string.dropboxLogoutSure)
                    setPositiveButton(R.string.yes) { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            dropbox!!.logout()
                            withContext(Dispatchers.Main) { updateDropbox() }
                        }
                    }
                    setNegativeButton(R.string.no, null)
                }.show()
            }
        )
        dropboxSync.setOnClickListener {
            if (!dropbox!!.isAuthenticated()) return@setOnClickListener
            dropbox!!.backup(true)
        }
    }
}
