package ir.mahdiparastesh.sexbook.view

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.base.BaseDialog

class HelpDialog : BaseDialog<BaseActivity>() {

    companion object : BaseDialogCompanion() {
        private const val TAG = "help"
        private const val BUNDLE_MESSAGE_RES = "message_res"

        fun create(c: BaseActivity, @StringRes messageRes: Int) {
            if (isDuplicate()) return
            HelpDialog().apply {
                arguments = Bundle().apply { putInt(BUNDLE_MESSAGE_RES, messageRes) }
                show(c.supportFragmentManager, TAG)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(c).apply {
            setTitle(R.string.help)
            setMessage(requireArguments().getInt(BUNDLE_MESSAGE_RES))
            setCancelable(true)
        }.show()
    }
}
