package com.qty.bookprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

public class BookProvider extends ContentProvider {

    //Logging helper tag. No significance to providers.
    private static final String TAG = "BookProvider";

    //Projection maps are similar to "as" construct
    //in an sql statement where by you can rename the
    //columns.
    private static final HashMap<String, String> sBooksProjectionMap;

    static {
        sBooksProjectionMap = new HashMap<>();
        sBooksProjectionMap.put(BookProviderMetaData.BookTableMetaData._ID,
                BookProviderMetaData.BookTableMetaData._ID);

        //name, isbn, author
        sBooksProjectionMap.put(BookProviderMetaData.BookTableMetaData.BOOK_NAME,
                BookProviderMetaData.BookTableMetaData.BOOK_NAME);
        sBooksProjectionMap.put(BookProviderMetaData.BookTableMetaData.BOOK_ISBN,
                BookProviderMetaData.BookTableMetaData.BOOK_ISBN);
        sBooksProjectionMap.put(BookProviderMetaData.BookTableMetaData.BOOK_AUTHOR,
                BookProviderMetaData.BookTableMetaData.BOOK_AUTHOR);

        //created date, modified date
        sBooksProjectionMap.put(BookProviderMetaData.BookTableMetaData.CREATED_DATE,
                BookProviderMetaData.BookTableMetaData.CREATED_DATE);
        sBooksProjectionMap.put(BookProviderMetaData.BookTableMetaData.MODIFIED_DATE,
                BookProviderMetaData.BookTableMetaData.MODIFIED_DATE);
    }

    //Provide a mechanism to identify
    //all the incoming uri patterns.
    private static final UriMatcher sUriMatcher;
    private static final int INCOMING_BOOK_COLLECTION_URI_INDICATOR = 1;
    private static final int INCOMING_SINGLE_BOOK_URI_INDICATOR = 2;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(BookProviderMetaData.AUTHORITY, "books",
                INCOMING_BOOK_COLLECTION_URI_INDICATOR);
        sUriMatcher.addURI(BookProviderMetaData.AUTHORITY, "books/#",
                INCOMING_SINGLE_BOOK_URI_INDICATOR);

    }

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        Log.d(TAG, "main onCreate called");
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    synchronized public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case INCOMING_BOOK_COLLECTION_URI_INDICATOR:
                count = db.delete(BookProviderMetaData.BookTableMetaData.TABLE_NAME,
                        selection, selectionArgs);
                break;

            case INCOMING_SINGLE_BOOK_URI_INDICATOR:
                String rowId = uri.getPathSegments().get(1);
                count = db.delete(BookProviderMetaData.BookTableMetaData.TABLE_NAME,
                        BookProviderMetaData.BookTableMetaData._ID + "=" + rowId
                                + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
                        selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case INCOMING_BOOK_COLLECTION_URI_INDICATOR:
                return BookProviderMetaData.BookTableMetaData.CONTENT_TYPE;

            case INCOMING_SINGLE_BOOK_URI_INDICATOR:
                return BookProviderMetaData.BookTableMetaData.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    synchronized public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri)
                != INCOMING_BOOK_COLLECTION_URI_INDICATOR) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = System.currentTimeMillis();

        // Make sure that the fields are all set
        if (!values.containsKey(BookProviderMetaData.BookTableMetaData.CREATED_DATE)) {
            values.put(BookProviderMetaData.BookTableMetaData.CREATED_DATE, now);
        }

        if (!values.containsKey(BookProviderMetaData.BookTableMetaData.MODIFIED_DATE)) {
            values.put(BookProviderMetaData.BookTableMetaData.MODIFIED_DATE, now);
        }

        if (!values.containsKey(BookProviderMetaData.BookTableMetaData.BOOK_NAME)) {
            throw new SQLException(
                    "Failed to insert row because Book Name is needed " + uri);
        }

        if (!values.containsKey(BookProviderMetaData.BookTableMetaData.BOOK_ISBN)) {
            values.put(BookProviderMetaData.BookTableMetaData.BOOK_ISBN, "Unknown ISBN");
        }
        if (!values.containsKey(BookProviderMetaData.BookTableMetaData.BOOK_AUTHOR)) {
            values.put(BookProviderMetaData.BookTableMetaData.BOOK_AUTHOR, "Unknown Author");
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(BookProviderMetaData.BookTableMetaData.TABLE_NAME,
                BookProviderMetaData.BookTableMetaData.BOOK_NAME, values);
        if (rowId > 0) {
            Uri insertedBookUri =
                    ContentUris.withAppendedId(
                            BookProviderMetaData.BookTableMetaData.CONTENT_URI, rowId);
            getContext()
                    .getContentResolver()
                    .notifyChange(insertedBookUri, null);

            return insertedBookUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    synchronized public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            case INCOMING_BOOK_COLLECTION_URI_INDICATOR:
                qb.setTables(BookProviderMetaData.BookTableMetaData.TABLE_NAME);
                qb.setProjectionMap(sBooksProjectionMap);
                break;

            case INCOMING_SINGLE_BOOK_URI_INDICATOR:
                qb.setTables(BookProviderMetaData.BookTableMetaData.TABLE_NAME);
                qb.setProjectionMap(sBooksProjectionMap);
                qb.appendWhere(BookProviderMetaData.BookTableMetaData._ID + "="
                        + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = BookProviderMetaData.BookTableMetaData.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection,
                selectionArgs, null, null, orderBy);

        //example of getting a count
        int i = c.getCount();

        // Tell the cursor what uri to watch,
        // so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    synchronized public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case INCOMING_BOOK_COLLECTION_URI_INDICATOR:
                count = db.update(BookProviderMetaData.BookTableMetaData.TABLE_NAME,
                        values, selection, selectionArgs);
                break;

            case INCOMING_SINGLE_BOOK_URI_INDICATOR:
                String rowId = uri.getPathSegments().get(1);
                count = db.update(BookProviderMetaData.BookTableMetaData.TABLE_NAME,
                        values, BookProviderMetaData.BookTableMetaData._ID + "=" + rowId
                                + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
                        selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context,
                    BookProviderMetaData.DATABASE_NAME,
                    null,
                    BookProviderMetaData.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "inner oncreate called");
            db.execSQL("CREATE TABLE " + BookProviderMetaData.BookTableMetaData.TABLE_NAME + " ("
                    + BookProviderMetaData.BookTableMetaData._ID + " INTEGER PRIMARY KEY,"
                    + BookProviderMetaData.BookTableMetaData.BOOK_NAME + " TEXT,"
                    + BookProviderMetaData.BookTableMetaData.BOOK_ISBN + " TEXT,"
                    + BookProviderMetaData.BookTableMetaData.BOOK_AUTHOR + " TEXT,"
                    + BookProviderMetaData.BookTableMetaData.CREATED_DATE + " INTEGER,"
                    + BookProviderMetaData.BookTableMetaData.MODIFIED_DATE + " INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG, "inner onupgrade called");
            Log.w(TAG, "Upgrading database from version "
                    + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " +
                    BookProviderMetaData.BookTableMetaData.TABLE_NAME);
            onCreate(db);
        }
    }
}