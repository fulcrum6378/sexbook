package ir.mahdiparastesh.sexbook.more

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.AppCompatEditText

class SoftEditText(
    context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int
) : AppCompatEditText(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.editTextStyle)
    constructor(context: Context) : this(context, null)

    override fun isTextSelectable(): Boolean = false
}

class SoftAutoCompleteTextView(
    context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int
) : AppCompatAutoCompleteTextView(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) :
            this(context, attrs, R.attr.autoCompleteTextViewStyle)

    constructor(context: Context) : this(context, null)

    override fun isTextSelectable(): Boolean = false
}
