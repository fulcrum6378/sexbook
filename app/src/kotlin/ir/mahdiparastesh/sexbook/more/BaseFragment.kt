package ir.mahdiparastesh.sexbook.more

import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {
    val c: BaseActivity by lazy { activity as BaseActivity }
}
