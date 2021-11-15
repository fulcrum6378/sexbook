package ir.mahdiparastesh.sexbook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ir.mahdiparastesh.sexbook.databinding.PageLoveBinding

class PageLove(val that: Main) : Fragment() {
    private lateinit var b: PageLoveBinding
    private lateinit var m: Model

    override fun onCreateView(inf: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        b = PageLoveBinding.inflate(layoutInflater, parent, false)
        m = ViewModelProvider(this, Model.Factory()).get("Model", Model::class.java)

        return b.root
    }
}
