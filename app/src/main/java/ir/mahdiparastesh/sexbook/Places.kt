package ir.mahdiparastesh.sexbook

import android.os.Bundle
import ir.mahdiparastesh.sexbook.databinding.PlacesBinding
import ir.mahdiparastesh.sexbook.more.BaseActivity

class Places : BaseActivity() {
    private lateinit var b: PlacesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = PlacesBinding.inflate(layoutInflater)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.plTitle)
    }
}
