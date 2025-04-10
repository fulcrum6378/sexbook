package ir.mahdiparastesh.sexbook.util

import android.util.LongSparseArray
import androidx.core.util.valueIterator

object LongSparseArrayExt {

    inline fun <T> LongSparseArray<T>.filter(predicate: (T) -> Boolean): ArrayList<T> {
        val al = arrayListOf<T>()
        for (item in valueIterator())
            if (predicate(item))
                al.add(item)
        return al
    }

    fun <T> LongSparseArray<T>.toArrayList(): ArrayList<T> {
        val al = arrayListOf<T>()
        for (item in valueIterator()) al.add(item)
        return al
    }

    fun <T> LongSparseArray<T>.iterator(): Iterator<Pair<Long, T>> =
        object : Iterator<Pair<Long, T>> {
            var index = 0

            override fun hasNext(): Boolean =
                index < size()

            override fun next(): Pair<Long, T> {
                val ret = Pair(keyAt(index), valueAt(index))
                index++
                return ret
            }
        }
}
