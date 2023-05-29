package ir.mahdiparastesh.sexbook.more

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.AppCompatEditText

/**
 * Subclass of AppCompatEditText that solves the problem of soft keyboard and selectable text.
 *
 * @see <a href="https://stackoverflow.com/questions/39896751/soft-keyboard-not-opening-after-setting-textisselectabletrue-in-android">Problem in StackOverflow</a>
 */
class SoftEditText(
    context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int
) : AppCompatEditText(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.editTextStyle)
    constructor(context: Context) : this(context, null)

    override fun isTextSelectable(): Boolean = false
}

/**
 * Subclass of AppCompatAutoCompleteTextView that solves the problem of soft keyboard and selectable text.
 *
 * @see <a href="https://stackoverflow.com/questions/39896751/soft-keyboard-not-opening-after-setting-textisselectabletrue-in-android">Problem in StackOverflow</a>
 */
class SoftAutoCompleteTextView(
    context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int
) : AppCompatAutoCompleteTextView(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) :
            this(context, attrs, R.attr.autoCompleteTextViewStyle)

    constructor(context: Context) : this(context, null)

    override fun isTextSelectable(): Boolean = false
}
