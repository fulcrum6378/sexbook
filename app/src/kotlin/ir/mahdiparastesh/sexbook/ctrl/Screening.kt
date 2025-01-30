package ir.mahdiparastesh.sexbook.ctrl

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.base.BaseDialog

class Screening : BaseDialog<BaseActivity>() {
    companion object {
        const val BUNDLE_WHICH_LIST = "which_list"
        const val TAG = "screening"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(c).apply {
            setTitle(R.string.identify)
            //setView()
        }.create()
    }
}
