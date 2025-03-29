package ir.mahdiparastesh.sexbook.view

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/** Helper class for implementing [RecyclerView.ViewHolder]. */
open class AnyViewHolder<B>(val b: B) : RecyclerView.ViewHolder(b.root) where B : ViewBinding

/**
 * An interface to be implemented on some custom listeners of [RecyclerView] items.
 * Note: TextWatchers cannot be retrieved from an EditText unless you extend EditText.
 */
interface RecyclerViewItemEvent<Obj> {
    var o: Obj?
}
