package ir.mahdiparastesh.sexbook.stat

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseDialog

class CrushesStat : BaseDialog() {
    companion object {
        const val TAG = "crushes_stat"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val pager = ViewPager2(c)
        pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 1
            override fun createFragment(i: Int): Fragment = when (i) {
                else -> TODO()
            }
        }

        isCancelable = true
        return MaterialAlertDialogBuilder(c).apply {
            setTitle(R.string.crushesStat)
            setView(pager)
        }.create()
    }
}