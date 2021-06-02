package com.qty.bookprovider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class ProviderTester {

    private static final String TAG = "Provider Tester";
    private final Context mContext;
    private final IReportBack mReportTo;

    ProviderTester(Context ctx, IReportBack target) {
        mContext = ctx;
        mReportTo = target;
    }

    public void addBook() {
        Log.d(TAG, "Adding a book");
        ContentValues cv = new ContentValues();
        cv.put(BookProviderMetaData.BookTableMetaData.BOOK_NAME, "book1");
        cv.put(BookProviderMetaData.BookTableMetaData.BOOK_ISBN, "isbn-1");
        cv.put(BookProviderMetaData.BookTableMetaData.BOOK_AUTHOR, "author-1");

        ContentResolver cr = this.mContext.getContentResolver();
        Uri uri = BookProviderMetaData.BookTableMetaData.CONTENT_URI;
        Log.d(TAG, "book insert uri:" + uri);
        Uri insertedUri = cr.insert(uri, cv);
        Log.d(TAG, "inserted uri:" + insertedUri);
        this.reportString("Inserted Uri:" + insertedUri);
    }

    public void updateBook() {
        Cursor c = mContext.getContentResolver().query(BookProviderMetaData.BookTableMetaData.CONTENT_URI,
                null,
                null,
                null,
                null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            int id = c.getInt(c.getColumnIndex(BookProviderMetaData.BookTableMetaData._ID));
            ContentValues cv = new ContentValues();
            cv.put(BookProviderMetaData.BookTableMetaData.BOOK_NAME, "book2");
            cv.put(BookProviderMetaData.BookTableMetaData.BOOK_ISBN, "isbn-2");
            cv.put(BookProviderMetaData.BookTableMetaData.BOOK_AUTHOR, "author-2");
            int count = mContext.getContentResolver().update(BookProviderMetaData.BookTableMetaData.CONTENT_URI,
                    cv,
                    BookProviderMetaData.BookTableMetaData._ID + " = ?",
                    new String[]{String.valueOf(id)});
            reportString("Update count: " + count);
            Log.w(TAG, "Update count: " + count);
        } else {
            reportString("No data to update.");
            Log.w(TAG, "ContentProvider no data.");
        }
        c.close();
    }

    public void removeBook() {
        int i = getCount();
        ContentResolver cr = this.mContext.getContentResolver();
        Uri uri = BookProviderMetaData.BookTableMetaData.CONTENT_URI;
        Uri delUri = Uri.withAppendedPath(uri, Integer.toString(i));
        reportString("Del Uri:" + delUri);
        cr.delete(delUri, null, null);
        this.reportString("Newcount:" + getCount());
    }

    public void showBooks() {
        Uri uri = BookProviderMetaData.BookTableMetaData.CONTENT_URI;
        Cursor c = mContext.getContentResolver().query(uri,
                null, //projection
                null, //selection string
                null, //selection args array of strings
                null); //sort order
        int iname = c.getColumnIndex(BookProviderMetaData.BookTableMetaData.BOOK_NAME);
        int iisbn = c.getColumnIndex(BookProviderMetaData.BookTableMetaData.BOOK_ISBN);
        int iauthor = c.getColumnIndex(BookProviderMetaData.BookTableMetaData.BOOK_AUTHOR);

        //Report your indexes
        reportString("name,isbn,author:" + iname + iisbn + iauthor);

        //walk through the rows based on indexes
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            //Gather values
            String id = c.getString(1);
            String name = c.getString(iname);
            String isbn = c.getString(iisbn);
            String author = c.getString(iauthor);

            //Report or log the row
            String cbuf = id + "," + name +
                    "," + isbn +
                    "," + author;
            reportString(cbuf);
        }

        //Report how many rows have been read
        int numberOfRecords = c.getCount();
        reportString("Num of Records:" + numberOfRecords);

        //Close the cursor
        //ideally this should be done in
        //a finally block.
        c.close();
    }

    private int getCount() {
        Uri uri = BookProviderMetaData.BookTableMetaData.CONTENT_URI;
        Cursor c = mContext.getContentResolver().query(uri,
                null, //projection
                null, //selection string
                null, //selection args array of strings
                null); //sort order
        int numberOfRecords = c.getCount();
        c.close();
        return numberOfRecords;
    }

    private void reportString(String s) {
        this.mReportTo.reportBack(TAG, s);
    }

}

