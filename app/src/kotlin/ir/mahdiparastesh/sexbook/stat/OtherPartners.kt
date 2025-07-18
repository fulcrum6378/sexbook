package ir.mahdiparastesh.sexbook.stat

import android.app.Dialog
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.base.BaseDialog
import ir.mahdiparastesh.sexbook.list.OtherPartnersAdap

class OtherPartners : BaseDialog<Singular>() {

    data class Item(val name: String, var times: Int) {
        override fun hashCode(): Int = name.lowercase().hashCode()
        override fun equals(other: Any?): Boolean =
            if (other is Item) name == other.name else false
    }

    companion object : BaseDialogCompanion() {
        private const val TAG = "other_partners"
        private const val BUNDLE_CRUSH_KEY = "crush_key"

        fun create(c: BaseActivity, crushKey: String) {
            if (isDuplicate()) return
            OtherPartners().apply {
                arguments = Bundle().apply { putString(BUNDLE_CRUSH_KEY, crushKey) }
                show(c.supportFragmentManager, TAG)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = true
        return MaterialAlertDialogBuilder(c).apply {
            setTitle(R.string.otherPartners)
            setView(RecyclerView(c).apply {
                layoutManager = LinearLayoutManager(c)
                if (adapter == null) adapter = OtherPartnersAdap(c)
            })
        }.create()
    }
}
