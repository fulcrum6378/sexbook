package ir.mahdiparastesh.sexbook

import android.os.Bundle
import ir.mahdiparastesh.sexbook.databinding.EstimationBinding
import ir.mahdiparastesh.sexbook.more.BaseActivity

class Estimation : BaseActivity() {
    private lateinit var b: EstimationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = EstimationBinding.inflate(layoutInflater)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.etTitle)
    }
}
