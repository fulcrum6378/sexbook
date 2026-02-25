package ir.mahdiparastesh.sexbook.ctrl

import android.app.Dialog
import android.os.Bundle
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.sexbook.BuildConfig
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseDialog
import ir.mahdiparastesh.sexbook.databinding.DataControlsBinding
import ir.mahdiparastesh.sexbook.page.Main

class DataControls : BaseDialog<Main>() {

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
        if (BuildConfig.BUILD_TYPE != "mahdi")
            bd.dropbox.isVisible = false
        else
            bd.dropbox.setOnClickListener { }

        return MaterialAlertDialogBuilder(c).apply {
            setTitle(R.string.dataControls)
            setView(bd.root)
            setCancelable(true)
        }.show()
    }
}
