package ru.ifmo.rain.tkachenko.providers;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

public class CityContentProvider extends ContentProvider {
	private static String AUTHORITY = "ru.ifmo.rain.tkachenko.provider";
	public static Uri CONTENT_URI = Uri.parse(ContentResolver.SCHEME_CONTENT
			+ "://" + AUTHORITY);

	private static final String DATABASE_NAME = "data";
	private static final int DATABASE_VERSION = 4;
	static final String CITY_ID = "_id";
	private static String CITIES_TABLE = "cities";

	public static Uri CONTENT_CITY_URI = Uri.withAppendedPath(CONTENT_URI,
			CITIES_TABLE);

	private static final int CODE_CITIES = 1;
	private static final int CODE_CITY = 2;

	private static UriMatcher MATCHER = null;
	static {
		MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		MATCHER.addURI(AUTHORITY, CITIES_TABLE, CODE_CITIES);
		MATCHER.addURI(AUTHORITY, CITIES_TABLE + "/*", CODE_CITY);
	}

	private SQLiteOpenHelper dbHelper;

	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	 public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		switch (MATCHER.match(uri)) {
		case CODE_CITY:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				selection = CITY_ID + " = " + id;
			} else {
				selection = selection + " AND " + CITY_ID + " = " + id;
			}
			break;
		default:
			throw new IllegalArgumentException("Wrong uri: " + uri.toString());
		}
		int res = db.delete(CITIES_TABLE, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(CONTENT_CITY_URI, null);
		return res;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues cv) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		switch (MATCHER.match(uri)) {
		case CODE_CITIES:
			break;
		default:
			throw new IllegalArgumentException("Wrong uri: " + uri.toString());
		}
		long rowID = db.insert(CITIES_TABLE, null, cv);
		getContext().getContentResolver().notifyChange(CONTENT_CITY_URI, null);
		return Uri.withAppendedPath(CONTENT_CITY_URI, Long.toString(rowID));
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		switch (MATCHER.match(uri)) {
		case CODE_CITIES:
			break;
		case CODE_CITY:
			break;
		default:
			throw new IllegalArgumentException("Wrong uri: " + uri.toString());
		}
		Cursor c = db.query(CITIES_TABLE, projection, selection, selectionArgs,
				null, null, null);
		c.setNotificationUri(getContext().getContentResolver(),
				CONTENT_CITY_URI);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues cv, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		switch (MATCHER.match(uri)) {
		case CODE_CITY:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				selection = CITY_ID + " = " + id;
			} else {
				selection = selection + " AND " + CITY_ID + " = " + id;
			}
			break;
		default:
			throw new IllegalArgumentException("Wrong uri: " + uri.toString());
		}
		int res = db.update(CITIES_TABLE, cv, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(CONTENT_CITY_URI, null);
		return res;
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		private static final String DATABASE_CREATE = "create table cities (_id integer primary key autoincrement, "
				+ "city text not null, temp text not null, time text not null, weather text not null, condition text not null);";

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS City");
			onCreate(db);
		}
	}
}
