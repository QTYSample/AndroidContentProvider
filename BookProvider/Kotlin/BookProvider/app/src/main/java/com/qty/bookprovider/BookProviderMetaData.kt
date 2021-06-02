package com.qty.bookprovider

import android.net.Uri
import android.provider.BaseColumns

class BookProviderMetaData private constructor() {

    // inner class describing columns and their types
    class BookTableMetaData private constructor() {

        companion object {
            const val TABLE_NAME = "books"

            // uri and mime type definitions
            val CONTENT_URI = Uri.parse("content://$AUTHORITY/books")
            const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.androidbook.book"
            const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd/androidbook.book"

            const val DEFAULT_SORT_ORDER = "modified DESC"

            // Additional Columns start here.
            //Integer
            const val _ID = BaseColumns._ID
            // string type
            const val BOOK_NAME = "name"
            // string type
            const val BOOK_ISBN = "isbn"
            // string type
            const val BOOK_AUTHOR = "author"
            // Integer from System.currentTimeMillis()
            const val CREATED_DATE = "created"
            // Integer from System.currentTimeMillis()
            const val MODIFIED_DATE = "modified"
        }

    }

    companion object {
        const val AUTHORITY = "com.qty.provider.BookProvider"

        const val DATABASE_NAME = "book.db"
        const val DATABASE_VERSION = 1
        const val BOOKS_TABLE_NAME = "books"
    }
}