//@file:Suppress("unused")

package ir.mahdiparastesh.sexbook.view

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
/* FIXME
    09:45:51.650  E  FATAL EXCEPTION: main
    Process: ir.mahdiparastesh.sexbook, PID: 26803
    java.lang.IndexOutOfBoundsException: Inconsistency detected. Invalid view holder adapter positionAnyViewHolder{a6ed5ae position=8 id=-1, oldPos=-1, pLpos:-1 no parent} androidx.recyclerview.widget.RecyclerView{8a120a5 VFED..... .F...... 0,147-1080,2131 #7f0a011a app:id/list aid=1073741854}, adapter:ir.mahdiparastesh.sexbook.list.GuessAdap@2194c18, layout:ir.mahdiparastesh.sexbook.base.SafeLinearLayoutManager@92b5c7a, context:ir.mahdiparastesh.sexbook.Estimation@9315304
    at androidx.recyclerview.widget.RecyclerView$Recycler.validateViewHolderForOffsetPosition(RecyclerView.java:6544)
    at androidx.recyclerview.widget.RecyclerView$Recycler.tryGetViewHolderForPositionByDeadline(RecyclerView.java:6727)
    at androidx.recyclerview.widget.GapWorker.prefetchPositionWithDeadline(GapWorker.java:288)
    at androidx.recyclerview.widget.GapWorker.flushTaskWithDeadline(GapWorker.java:345)
    at androidx.recyclerview.widget.GapWorker.flushTasksWithDeadline(GapWorker.java:361)
    at androidx.recyclerview.widget.GapWorker.prefetch(GapWorker.java:368)
    at androidx.recyclerview.widget.GapWorker.run(GapWorker.java:399)
    at android.os.Handler.handleCallback(Handler.java:938)
    at android.os.Handler.dispatchMessage(Handler.java:99)
    at android.os.Looper.loop(Looper.java:246)
    at android.app.ActivityThread.main(ActivityThread.java:8653)
    at java.lang.reflect.Method.invoke(Native Method)
    at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:602)
    at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1130)
    *
    *
    * This problem is caused by RecyclerView Data modified in different thread.
    * The best way is checking all data access. And a workaround is wrapping LinearLayoutManager.
    */
