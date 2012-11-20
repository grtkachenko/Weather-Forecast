package ru.ifmo.rain.tkachenko.weather;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CityDbAdapter {
	public static final String KEY_CITY = "city";
	public static final String KEY_ROWID = "_id";

	private static final String TAG = "CityDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String DATABASE_CREATE = "create table City (_id integer primary key autoincrement, "
			+ "city text not null);";

	private static final String DATABASE_NAME = "data";
	private static final String DATABASE_TABLE = "City";
	private static final int DATABASE_VERSION = 2;
	private Cursor cityCursor;
	private Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.w(TAG, "Create");
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS City");
			onCreate(db);
		}
	}

	public CityDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	public CityDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		// mDbHelper.onCreate(mDb); Почему когда в он клик не описано добавление
		// само не делает Create
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public long createCity(String city) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_CITY, city);
		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	public boolean deleteCity(long rowId) {
		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public boolean deleteCity(String name) {
		cityCursor = this.fetchAllCity();
		long id = 0;
		if (cityCursor.moveToFirst()) {
			do {
				String cur = cityCursor.getString(cityCursor
						.getColumnIndex("city"));
				if (cur.equals(name)) {
					id = cityCursor.getLong(cityCursor.getColumnIndex("_id"));
				}
			} while (cityCursor.moveToNext());
		}
		cityCursor.close();
		return this.deleteCity(id);
	}

	public Cursor fetchAllCity() {
		return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_CITY },
				null, null, null, null, null);
	}

	public Cursor fetchCity(long rowId) throws SQLException {

		Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_CITY }, KEY_ROWID + "=" + rowId, null, null,
				null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public boolean updateCity(long rowId, String city) {
		ContentValues args = new ContentValues();
		args.put(KEY_CITY, city);

		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public boolean updateCity(String name, String city) {
		cityCursor = this.fetchAllCity();
		long id = 0;
		if (cityCursor.moveToFirst()) {
			do {
				String cur = cityCursor.getString(cityCursor
						.getColumnIndex("city"));
				if (cur.equals(name)) {
					id = cityCursor.getLong(cityCursor.getColumnIndex("_id"));
				}
			} while (cityCursor.moveToNext());
		}
		cityCursor.close();
		return this.updateCity(id, city);
	}
	
	public ArrayList<String> getAllCities() {
		ArrayList<String> ans = new ArrayList<String>();
		cityCursor = this.fetchAllCity();
		if (cityCursor.moveToFirst()) {
			do {
				String cur = cityCursor.getString(cityCursor
						.getColumnIndex("city"));
				ans.add(cur);
			} while (cityCursor.moveToNext());
		}
		cityCursor.close();
		return ans;
	}
}
