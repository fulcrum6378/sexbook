package org.ifaco.mbcounter.dirchooser

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import org.ifaco.mbcounter.R
import org.ifaco.mbcounter.dirchooser.DirectoryChooserFragment.OnFragmentInteractionListener

class DirectoryChooserActivity : AppCompatActivity(), OnFragmentInteractionListener {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
        setContentView(R.layout.directory_chooser_activity)
        val config: DirectoryChooserConfig = intent.getParcelableExtra(EXTRA_CONFIG)
            ?: throw IllegalArgumentException(
                "You must provide EXTRA_CONFIG when starting the DirectoryChooserActivity."
            )
        if (savedInstanceState == null) {
            val fragment = DirectoryChooserFragment.newInstance(config)
            supportFragmentManager.beginTransaction().add(R.id.main, fragment).commit()
        }
    }

    fun setupActionBar() {
        @SuppressLint("AppCompatMethod") val actionBar = actionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == android.R.id.home) {
            setResult(RESULT_CANCELED)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSelectDirectory(path: String) {
        val intent = Intent()
        intent.putExtra(RESULT_SELECTED_DIR, path)
        setResult(RESULT_CODE_DIR_SELECTED, intent)
        finish()
    }

    override fun onCancelChooser() {
        setResult(RESULT_CANCELED)
        finish()
    }

    companion object {
        const val EXTRA_CONFIG = "config"
        const val RESULT_SELECTED_DIR = "selected_dir"
        const val RESULT_CODE_DIR_SELECTED = 1
    }
}
