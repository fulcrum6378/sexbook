package ir.mahdiparastesh.sexbook.base

import androidx.fragment.app.Fragment
import ir.mahdiparastesh.sexbook.base.BaseActivity

abstract class BaseFragment : Fragment() {
    val c: BaseActivity by lazy { activity as BaseActivity }
}
