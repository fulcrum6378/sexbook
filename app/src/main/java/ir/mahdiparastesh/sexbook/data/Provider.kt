package ir.mahdiparastesh.sexbook.data

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.sqlite.db.SupportSQLiteQueryBuilder

class Provider : ContentProvider() {
    private lateinit var c: Context
    private lateinit var db: Database
    private lateinit var dao: Dao

    override fun onCreate(): Boolean {
        c = context ?: return false
        db = Database.build(c)
        dao = db.dao()
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor = db.query(
        SupportSQLiteQueryBuilder.builder(table(uri))
            .columns(projection)
            .selection(selection, selectionArgs)
            .orderBy(sortOrder)
            .create()
    )

    override fun getType(uri: Uri): String? = table(uri)

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (values == null) return null
        db.openHelper.writableDatabase.insert(table(uri), SQLiteDatabase.CONFLICT_ABORT, values)
        return uri
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int =
        db.openHelper.writableDatabase.delete(table(uri), selection, selectionArgs)

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = db.openHelper.writableDatabase.update(
        table(uri),
        SQLiteDatabase.CONFLICT_ABORT,
        values ?: ContentValues(),
        selection,
        selectionArgs
    )

    private fun table(uri: Uri) = uri.pathSegments[0]

    override fun shutdown() {
        db.close()
        super.shutdown()
    }
}
