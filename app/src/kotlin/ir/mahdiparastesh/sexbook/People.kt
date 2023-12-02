package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.google.android.material.badge.BadgeDrawable
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.PeopleBinding
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.Lister

class People : BaseActivity(), Lister {
    private lateinit var b: PeopleBinding

    override var countBadge: BadgeDrawable? = null

    companion object {
        var handler: Handler? = null
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = PeopleBinding.inflate(layoutInflater)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.people)

        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                }
            }
        }

        Work(c, Work.C_VIEW_ALL).start()
    }

    override fun onDestroy() {
        handler = null
        super.onDestroy()
    }
}
