package com.qty.bookprovider

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log

open class ProviderTester(
    private val mContext: Context,
    private val mReportTo: IReportBack
) {

    open fun addBook() {
        Log.d(TAG, "Adding a book")
        val cv = ContentValues()
        with(cv) {
            put(BookProviderMetaData.BookTableMetaData.BOOK_NAME, "book1")
            put(BookProviderMetaData.BookTableMetaData.BOOK_ISBN, "isbn-1")
            put(BookProviderMetaData.BookTableMetaData.BOOK_AUTHOR, "author-1")
        }

        val uri = BookProviderMetaData.BookTableMetaData.CONTENT_URI
        Log.d(TAG, "book insert uri: $uri")
        val insertedUri = mContext.contentResolver.insert(uri, cv)
        Log.d(TAG, "inserted uri: $insertedUri")
        reportString("Inserted Uri: $insertedUri")
    }

    open fun updateBook() {
        Log.d(TAG, "Update a book")
        val c = mContext.contentResolver.query(BookProviderMetaData.BookTableMetaData.CONTENT_URI,
            null,
            null,
            null,
            null)
        val count = c?.count ?: 0
        if (count > 0) {
            c?.moveToFirst()
            val updateId = c!!.getInt(c!!.getColumnIndex(BookProviderMetaData.BookTableMetaData._ID))
            val cv = ContentValues()
            with(cv) {
                put(BookProviderMetaData.BookTableMetaData.BOOK_NAME, "book2")
                put(BookProviderMetaData.BookTableMetaData.BOOK_ISBN, "isbn-2")
                put(BookProviderMetaData.BookTableMetaData.BOOK_AUTHOR, "author-2")
            }
            mContext.contentResolver.update(BookProviderMetaData.BookTableMetaData.CONTENT_URI, cv,
                "${BookProviderMetaData.BookTableMetaData._ID} = ?", arrayOf(updateId.toString()))
        } else {
            reportString("No data to update.")
            Log.d(TAG, "ContentProvider no data.")
        }
        c?.close()
    }

    open fun removeBook() {
        val i = getCount()
        val uri = BookProviderMetaData.BookTableMetaData.CONTENT_URI
        val delUri = Uri.withAppendedPath(uri, i.toString())
        reportString("Del Uri: $delUri")
        mContext.contentResolver.delete(delUri, null, null)

        reportString("Newcount: ${getCount()}")
    }

    open fun showBooks() {
        val uri = BookProviderMetaData.BookTableMetaData.CONTENT_URI
        val c = mContext.contentResolver.query(uri,
            null,
            null,
            null,
            null
        )
        if (c != null) {
            val iname = c!!.getColumnIndex(BookProviderMetaData.BookTableMetaData.BOOK_NAME)
            val iisbn = c!!.getColumnIndex(BookProviderMetaData.BookTableMetaData.BOOK_ISBN)
            val iauthor = c!!.getColumnIndex(BookProviderMetaData.BookTableMetaData.BOOK_AUTHOR)

            // Report your indexes
            reportString("name, isbn, author: $iname, $iisbn, $iauthor")

            // walk through the rows based on indexes
            while (c!!.moveToNext()) {
                // Gather values
                val id = c.getString(1)
                val name = c.getString(iname)
                val isbn = c.getString(iisbn)
                val auther = c.getString(iauthor)

                // Report or log the row
                val cbuf = StringBuffer(id)
                with(cbuf) {
                    append(",").append(name)
                    append(",").append(isbn)
                    append(",").append(auther)
                }
                reportString(cbuf.toString())
            }
        }

        // Report how many rows have been read
        val numberOfRecords = c?.count ?: 0
        reportString("Num of Records: $numberOfRecords")

        // Close the cursor
        // ideally this should be done in
        // a finally block.
        c?.close()
    }

    private fun getCount(): Int {
        val uri = BookProviderMetaData.BookTableMetaData.CONTENT_URI
        val c = mContext.contentResolver.query(uri,
            null,   // projection
            null,  // selection string
            null,   // selection args array of strings
            null,   // sort order
        )
        val count = c?.count ?: 0
        c?.close()
        return count
    }

    private fun report(stringId: Int) {
        mReportTo.reportBack(TAG, mContext.getString(stringId))
    }

    private fun reportString(s: String) {
        mReportTo.reportBack(TAG, s)
    }

    private fun reportString(s: String, stringId: Int) {
        mReportTo.reportBack(TAG, s)
        report(stringId)
    }

    companion object {
        const val TAG = "ProviderTester"
    }
}