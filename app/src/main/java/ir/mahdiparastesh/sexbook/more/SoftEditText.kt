package ir.mahdiparastesh.sexbook.more

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.AppCompatEditText

class SoftEditText(
    c: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int
) : AppCompatEditText(c, attrs, defStyleAttr) {
    constructor(c: Context, attrs: AttributeSet?) : this(c, attrs, R.attr.editTextStyle)
    constructor(c: Context) : this(c, null, R.attr.editTextStyle)

    override fun isTextSelectable(): Boolean = false
}

class SoftAutoCompleteTextView(
    c: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int
) : AppCompatAutoCompleteTextView(c, attrs, defStyleAttr) {
    constructor(c: Context, attrs: AttributeSet?) :
            this(c, attrs, R.attr.autoCompleteTextViewStyle)

    constructor(c: Context) : this(c, null, R.attr.autoCompleteTextViewStyle)

    override fun isTextSelectable(): Boolean = false
}
