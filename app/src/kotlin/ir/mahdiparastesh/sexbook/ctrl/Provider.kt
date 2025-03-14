package ir.mahdiparastesh.sexbook.ctrl

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import ir.mahdiparastesh.sexbook.Sexbook

/**
 * An API ([ContentProvider]) for other apps to read and/or write data in [Sexbook].
 */
class Provider : ContentProvider() {
    private lateinit var c: Sexbook

    override fun onCreate(): Boolean {
        c = context as Sexbook? ?: return false
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor = c.db.query(
        SupportSQLiteQueryBuilder.builder(table(uri))
            .columns(projection)
            .selection(selection, selectionArgs)
            .orderBy(sortOrder)
            .create()
    )

    override fun getType(uri: Uri): String? = table(uri)

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (values == null) return null
        c.db.openHelper.writableDatabase.insert(table(uri), SQLiteDatabase.CONFLICT_ABORT, values)
        return uri
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int =
        c.db.openHelper.writableDatabase.delete(table(uri), selection, selectionArgs)

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = c.db.openHelper.writableDatabase.update(
        table(uri),
        SQLiteDatabase.CONFLICT_ABORT,
        values ?: ContentValues(),
        selection,
        selectionArgs
    )

    private fun table(uri: Uri) = uri.pathSegments[0]

    override fun shutdown() {
        c.db.close()
        super.shutdown()
    }
}
