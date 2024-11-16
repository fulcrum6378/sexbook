@file:SuppressLint("AppCompatCustomView")

package ir.mahdiparastesh.sexbook.misc

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.annotation.AttrRes

/**
 * Subclass of EditText that solves the problem of soft keyboard and selectable text.
 *
 * @see <a href="https://stackoverflow.com/questions/39896751/soft-keyboard-not-opening-after-setting-textisselectabletrue-in-android">Problem in StackOverflow</a>
 */
open class SoftEditText(
    context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int
) : EditText(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) :
            this(context, attrs, R.attr.editTextStyle)

    constructor(context: Context) : this(context, null)

    override fun isTextSelectable(): Boolean = false
}

/**
 * Subclass of SoftEditText that on accepts only a single TextWatcher for good.
 * So that its contents can be safely echoed in the database.
 */
class LiveEditText(
    context: Context, attrs: AttributeSet?
) : SoftEditText(context, attrs), LiveTextField {
    constructor(context: Context) : this(context, null)

    override var mListener: TextWatcher? = null

    override fun setTextWatcher(listener: TextWatcher?) {
        if (mListener != null) removeTextChangedListener(mListener)
        if (listener == null) return
        mListener = listener
        addTextChangedListener(listener)
    }
}

/**
 * Subclass of AutoCompleteTextView that solves the problem of soft keyboard and selectable text.
 *
 * @see <a href="https://stackoverflow.com/questions/39896751/soft-keyboard-not-opening-after-setting-textisselectabletrue-in-android">Problem in StackOverflow</a>
 */
open class SoftAutoCompleteTextView(
    context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int
) : AutoCompleteTextView(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) :
            this(context, attrs, R.attr.autoCompleteTextViewStyle)

    constructor(context: Context) : this(context, null)

    override fun isTextSelectable(): Boolean = false
}

/**
 * Subclass of SoftAutoCompleteTextView that on accepts only a single TextWatcher for good.
 * So that its contents can be safely echoed in the database.
 */
class LiveAutoCompleteTextView(
    context: Context, attrs: AttributeSet?
) : SoftAutoCompleteTextView(context, attrs), LiveTextField {
    constructor(context: Context) : this(context, null)

    override var mListener: TextWatcher? = null
    override fun setTextWatcher(listener: TextWatcher?) {
        if (mListener != null) removeTextChangedListener(mListener)
        if (listener == null) return
        mListener = listener
        addTextChangedListener(listener)
    }
}

interface LiveTextField {
    var mListener: TextWatcher?
    fun setTextWatcher(listener: TextWatcher?)
}
