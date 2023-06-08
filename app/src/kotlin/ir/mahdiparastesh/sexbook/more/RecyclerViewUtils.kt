package ir.mahdiparastesh.sexbook.more

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/** Helper class for implementing RecyclerView.ViewHolder. */
open class AnyViewHolder<B>(val b: B) : RecyclerView.ViewHolder(b.root) where B : ViewBinding

/** An interface to be implemented on some custom listeners of RecyclerView items. */
interface RecyclerViewItemEvent {
    var i: Int?
}
