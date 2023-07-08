package ir.mahdiparastesh.sexbook.more

import androidx.fragment.app.DialogFragment

abstract class BaseDialog : DialogFragment() {
    protected val c: BaseActivity by lazy { activity as BaseActivity }
}
