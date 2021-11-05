package ir.mahdiparastesh.sexbook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import ir.mahdiparastesh.sexbook.databinding.SettingsBinding

class Settings : AppCompatActivity() {
    private lateinit var b: SettingsBinding
    private lateinit var m: Model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = SettingsBinding.inflate(layoutInflater)
        m = ViewModelProvider(this, Model.Factory()).get("Model", Model::class.java)
        setContentView(b.root)
        Fun.init(this)
    }
}
