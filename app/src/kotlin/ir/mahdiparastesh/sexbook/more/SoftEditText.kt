@file:SuppressLint("AppCompatCustomView")

package ir.mahdiparastesh.sexbook.more

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.annotation.AttrRes

/**
 * Subclass of EditText that solves the problem of soft keyboard and selectable text.
 *
 * @see <a href="https://stackoverflow.com/questions/39896751/soft-keyboard-not-opening-after-setting-textisselectabletrue-in-android">Problem in StackOverflow</a>
 */
class SoftEditText(
    context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int
) : EditText(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) :
            this(context, attrs, android.R.attr.editTextStyle)

    constructor(context: Context) : this(context, null)

    override fun isTextSelectable(): Boolean = false
}

/**
 * Subclass of AutoCompleteTextView that solves the problem of soft keyboard and selectable text.
 *
 * @see <a href="https://stackoverflow.com/questions/39896751/soft-keyboard-not-opening-after-setting-textisselectabletrue-in-android">Problem in StackOverflow</a>
 */
class SoftAutoCompleteTextView(
    context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int
) : AutoCompleteTextView(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) :
            this(context, attrs, android.R.attr.autoCompleteTextViewStyle)

    constructor(context: Context) : this(context, null)

    override fun isTextSelectable(): Boolean = false
}
