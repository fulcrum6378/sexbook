@file:Suppress("unused")

package ir.mahdiparastesh.sexbook.more

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/** Subclass of LinearLayoutManager that avoids some kinds of app crashes. */
open class SafeLinearLayoutManager : LinearLayoutManager {
    constructor(
        context: Context, @RecyclerView.Orientation orientation: Int, reverseLayout: Boolean
    ) : super(context, orientation, reverseLayout)

    constructor(
        context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context) : super(context)

    override fun onLayoutChildren(rv: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(rv, state)
        } catch (_: IndexOutOfBoundsException) {
        }
    }

    override fun onFocusSearchFailed(
        focused: View, focusDirection: Int,
        recycler: RecyclerView.Recycler, state: RecyclerView.State
    ): View? = try {
        super.onFocusSearchFailed(focused, focusDirection, recycler, state)
    } catch (e: IndexOutOfBoundsException) {
        null
    }
}
