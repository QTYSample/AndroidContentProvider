package com.qty.bookprovider

import android.content.*
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.text.TextUtils
import android.util.Log

class BookProvider : ContentProvider() {

    private lateinit var mOpenHelper: DatabaseHelper

    override fun onCreate(): Boolean {
        Log.d(TAG, "main onCreate called")
        mOpenHelper = DatabaseHelper(context)
        return true
    }

    @Synchronized override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        val qb = SQLiteQueryBuilder()

        when (sUriMatcher.match(uri)) {
            INCOMING_BOOK_COLLECTION_RUI_INDICATOR -> {
                qb.tables = BookProviderMetaData.BookTableMetaData.TABLE_NAME
                qb.projectionMap = sBookProjectionMap
            }
            INCOMING_SINGLE_BOOK_URI_INDICATOR -> {
                qb.tables = BookProviderMetaData.BookTableMetaData.TABLE_NAME
                qb.projectionMap = sBookProjectionMap
                qb.appendWhere(BookProviderMetaData.BookTableMetaData._ID + "=" + uri.pathSegments[1])
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }

        // if no sort order is specified use the default
        val orderBy = if (TextUtils.isEmpty(sortOrder)) {
            BookProviderMetaData.BookTableMetaData.DEFAULT_SORT_ORDER
        } else {
            sortOrder
        }

        // Get the database and run the query
        val c = qb.query(mOpenHelper.readableDatabase, projection, selection, selectionArgs, null, null, orderBy)

        // example of getting a count
        val i = c.count

        // Tell the cursor what uri to watch,
        // so it knows when its source data changes
        c.setNotificationUri(context?.contentResolver, uri)
        return c
    }

    @Synchronized override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != INCOMING_BOOK_COLLECTION_RUI_INDICATOR) {
            throw IllegalArgumentException("Unknown URI $uri")
        }

        val values = if (values != null) {
            ContentValues(values)
        } else {
            ContentValues()
        }

        val now = System.currentTimeMillis() as Long

        // Make sure that the fields are all set
        if (!values.containsKey(BookProviderMetaData.BookTableMetaData.CREATED_DATE)) {
            values.put(BookProviderMetaData.BookTableMetaData.CREATED_DATE, now)
        }

        if (!values.containsKey(BookProviderMetaData.BookTableMetaData.MODIFIED_DATE)) {
            values.put(BookProviderMetaData.BookTableMetaData.MODIFIED_DATE, now)
        }

        if (!values.containsKey(BookProviderMetaData.BookTableMetaData.BOOK_NAME)) {
            throw SQLException("Failed to insert row because Book Name is needed " + uri)
        }

        if (!values.containsKey(BookProviderMetaData.BookTableMetaData.BOOK_ISBN)) {
            values.put(BookProviderMetaData.BookTableMetaData.BOOK_ISBN, "Unknown ISBN")
        }

        if (!values.containsKey(BookProviderMetaData.BookTableMetaData.BOOK_AUTHOR)) {
            values.put(BookProviderMetaData.BookTableMetaData.BOOK_AUTHOR, "Unknown Author")
        }

        val rowId = mOpenHelper.writableDatabase.insert(BookProviderMetaData.BookTableMetaData.TABLE_NAME, BookProviderMetaData.BookTableMetaData.BOOK_NAME, values)
        if (rowId > 0) {
            val insertedBookUri = ContentUris.withAppendedId(BookProviderMetaData.BookTableMetaData.CONTENT_URI, rowId)
            context?.contentResolver?.notifyChange(insertedBookUri, null)
            return insertedBookUri
        }
        throw SQLException("Failed to insert row into $uri")
    }

    @Synchronized override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val count = when (sUriMatcher.match(uri)) {
            INCOMING_BOOK_COLLECTION_RUI_INDICATOR -> mOpenHelper.writableDatabase.delete(BookProviderMetaData.BookTableMetaData.TABLE_NAME, selection, selectionArgs)
            INCOMING_SINGLE_BOOK_URI_INDICATOR -> {
                val rowId = uri.pathSegments[1]
                mOpenHelper.writableDatabase.delete(BookProviderMetaData.BookTableMetaData.TABLE_NAME,
                    BookProviderMetaData.BookTableMetaData._ID + "=" + rowId
                            + (if(!TextUtils.isEmpty(selection))  " AND ( $selection )" else ""), selectionArgs)
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    @Synchronized override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val count = when (sUriMatcher.match(uri)) {
            INCOMING_BOOK_COLLECTION_RUI_INDICATOR -> mOpenHelper.writableDatabase.update(BookProviderMetaData.BookTableMetaData.TABLE_NAME, values, selection, selectionArgs)
            INCOMING_SINGLE_BOOK_URI_INDICATOR -> {
                val rowId = uri.pathSegments[1]
                mOpenHelper.writableDatabase.update(BookProviderMetaData.BookTableMetaData.TABLE_NAME, values,
                    BookProviderMetaData.BookTableMetaData._ID + "=" + rowId
                            + (if (!TextUtils.isEmpty(selection)) " AND ( $selection )" else ""), selectionArgs)
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun getType(uri: Uri): String? = when (sUriMatcher.match(uri)) {
        INCOMING_BOOK_COLLECTION_RUI_INDICATOR -> BookProviderMetaData.BookTableMetaData.CONTENT_TYPE
        INCOMING_SINGLE_BOOK_URI_INDICATOR -> BookProviderMetaData.BookTableMetaData.CONTENT_ITEM_TYPE
        else -> throw IllegalArgumentException("Unknown URI $uri")
    }

    private class DatabaseHelper(
        context: Context?
    ): SQLiteOpenHelper(context, BookProviderMetaData.DATABASE_NAME, null, BookProviderMetaData.DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase?) {
            Log.d(TAG, "inner oncreate called")
            db?.execSQL("CREATE TABLE " + BookProviderMetaData.BookTableMetaData.TABLE_NAME + " (" +
                    BookProviderMetaData.BookTableMetaData._ID + " INTEGER PRIMARY KEY," +
                    BookProviderMetaData.BookTableMetaData.BOOK_NAME + " TEXT," +
                    BookProviderMetaData.BookTableMetaData.BOOK_ISBN + " TEXT," +
                    BookProviderMetaData.BookTableMetaData.BOOK_AUTHOR + " TEXT," +
                    BookProviderMetaData.BookTableMetaData.CREATED_DATE + " INTEGER," +
                    BookProviderMetaData.BookTableMetaData.MODIFIED_DATE + " INTEGER" +
                    ");"
            )
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            Log.d(TAG, "inner onupgrade called")
            Log.w(TAG, "Upgrading database from version "
                    + oldVersion + " to "
                    + newVersion + ", which will destroy all old data")
            db?.execSQL("DROP TABLE IF EXISTS " + BookProviderMetaData.BookTableMetaData.TABLE_NAME)
            onCreate(db)
        }
    }

    private companion object {
        // Logging helper tag. No significance to providers.
        const val TAG = "BookProvider"
        // Projection maps are similar to "as" construct
        // in an sql statement where by you can rename the
        // columns.
        val sBookProjectionMap: HashMap<String, String> = HashMap()
        // Provide a mechanism to identify
        // all the incoming uri patterns.
        val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        const val INCOMING_BOOK_COLLECTION_RUI_INDICATOR = 1
        const val INCOMING_SINGLE_BOOK_URI_INDICATOR = 2


        init {
            with(sBookProjectionMap) {
                put(BookProviderMetaData.BookTableMetaData._ID, BookProviderMetaData.BookTableMetaData._ID)
                put(BookProviderMetaData.BookTableMetaData.BOOK_NAME, BookProviderMetaData.BookTableMetaData.BOOK_NAME)
                put(BookProviderMetaData.BookTableMetaData.BOOK_ISBN, BookProviderMetaData.BookTableMetaData.BOOK_ISBN)
                put(BookProviderMetaData.BookTableMetaData.BOOK_AUTHOR, BookProviderMetaData.BookTableMetaData.BOOK_AUTHOR)
                put(BookProviderMetaData.BookTableMetaData.CREATED_DATE, BookProviderMetaData.BookTableMetaData.CREATED_DATE)
                put(BookProviderMetaData.BookTableMetaData.MODIFIED_DATE, BookProviderMetaData.BookTableMetaData.MODIFIED_DATE)
            }
            with(sUriMatcher) {
                addURI(BookProviderMetaData.AUTHORITY, "books", INCOMING_BOOK_COLLECTION_RUI_INDICATOR)
                addURI(BookProviderMetaData.AUTHORITY, "books/#", INCOMING_SINGLE_BOOK_URI_INDICATOR)
            }
        }
    }
}