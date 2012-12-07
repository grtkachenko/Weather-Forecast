package ru.ifmo.rain.tkachenko.weather;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

public class CityDbAdapter {
	public static final String KEY_CITY = "city";
	public static final String KEY_TEMP = "temp";
	public static final String KEY_TIME = "time";
	public static final String KEY_WEATHER = "weather";
	public static final String KEY_CONDITION = "condition";

	// temp - "15 $ 11 11 12, 13 $" - start hour$temperature_hourly$min/max
	// weather- "sunny $ rainy $ sunny $"

	public static final String KEY_ROWID = "_id";

	private static final String TAG = "CityDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String DATABASE_CREATE = "create table City (_id integer primary key autoincrement, "
			+ "city text not null, temp text not null, time text not null, weather text not null, condition text not null);";

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

	public long createCity(String city, String temp, String time,
			String weather, String condition) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_CITY, city);
		initialValues.put(KEY_TEMP, temp);
		initialValues.put(KEY_TIME, time);
		initialValues.put(KEY_WEATHER, weather);
		initialValues.put(KEY_CONDITION, condition);

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
					this.deleteCity(id);
				}
			} while (cityCursor.moveToNext());
		}
		cityCursor.close();
		return true;
	}

	public Cursor fetchAllCity() {
		return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_CITY,
				KEY_TEMP, KEY_TIME, KEY_WEATHER, KEY_CONDITION }, null, null,
				null, null, null);
	}

	public Cursor fetchCity(long rowId) throws SQLException {

		Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_CITY, KEY_TEMP, KEY_TIME, KEY_WEATHER,
				KEY_CONDITION }, KEY_ROWID + "=" + rowId, null, null, null,
				null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public boolean updateCity(long rowId, String city, String temp,
			String time, String weather, String condition) {
		ContentValues args = new ContentValues();
		args.put(KEY_CITY, city);
		args.put(KEY_TEMP, temp);
		args.put(KEY_TIME, time);
		args.put(KEY_WEATHER, weather);
		args.put(KEY_CONDITION, condition);

		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public void deleteDatabase() {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		mDb.execSQL("DROP TABLE IF EXISTS City");
		mDbHelper.onCreate(mDb);
	}

	public boolean updateCity(String name, String city, String temp,
			String time, String weather, String condition) {
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
		return this.updateCity(id, city, temp, time, weather, condition);
	}

	public boolean updateCity(String city, String temp, String time,
			String condition, String weather) {
		cityCursor = this.fetchAllCity();
		long id = 0;
		if (cityCursor.moveToFirst()) {
			do {
				String cur = cityCursor.getString(cityCursor
						.getColumnIndex("city"));

				if (cur.equals(city)) {
					id = cityCursor.getLong(cityCursor.getColumnIndex("_id"));
				}
			} while (cityCursor.moveToNext());
		}
		cityCursor.close();
		return this.updateCity(id, city, temp, time, weather, condition);
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

	public ArrayList<CityDbItem> getAllCityItems() {
		ArrayList<CityDbItem> ans = new ArrayList<CityDbItem>();
		cityCursor = this.fetchAllCity();
		if (cityCursor.moveToFirst()) {
			do {
				ans.add(new CityDbItem(
						cityCursor.getString(cityCursor.getColumnIndex("city")),
						cityCursor.getString(cityCursor.getColumnIndex("temp")),
						cityCursor.getString(cityCursor.getColumnIndex("time")),
						cityCursor.getString(cityCursor
								.getColumnIndex("weather")), cityCursor
								.getString(cityCursor
										.getColumnIndex("condition"))));
			} while (cityCursor.moveToNext());
		}
		cityCursor.close();
		return ans;
	}

	public void updateAllCities() {
		this.open();
		curAdapter = this;
		MyTask mt = new MyTask();
		mt.execute();
	}

	public volatile boolean f = true, isRefresh = false, isCalced = true;
	public CityDbAdapter curAdapter = null;

	class MyTask extends AsyncTask<Void, Void, Void> {
		private volatile ArrayList<WeatherAPIItem>[] weatherHourly = null;
		private WeatherAPIHelper weatherApiHelper = new WeatherAPIHelper();
		ArrayList<WeatherAPIItem> cur;
		private String[] cityList;

		@Override
		protected void onPreExecute() {
			ArrayList<String> cities = curAdapter.getAllCities();
			cityList = new String[cities.size()];
			weatherHourly = new ArrayList[cities.size()];
			
			int num = 0;
			for (String s : cities) {
				cityList[num++] = s;
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			for (int i = 0; i < cityList.length; i++) {
				int currentCity = i + 1;
				if (Cities.getCountry(cityList[currentCity - 1]).equals(
						"United_States")) {
					weatherHourly[i] = weatherApiHelper.getHourWeather(
							Cities.states.get(Cities
									.getCity(cityList[currentCity - 1])),
							Cities.getCity(cityList[currentCity - 1]));

				} else {

					weatherHourly[i] = weatherApiHelper.getHourWeather(
							Cities.getCountry(cityList[currentCity - 1]),
							Cities.getCity(cityList[currentCity - 1]));
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			for (int i = 0; i < weatherHourly.length; i++) {
				if (weatherHourly[i] == null) {
					continue;
				}
				updateCityInDatabase(cityList[i], getCurrentTime(),
						weatherHourly[i].get(0).condition, weatherHourly[i]);

			}
		}

		public String getCurrentTime() {
			String help = DateFormat.getDateTimeInstance().format(new Date());
			String ans = "";
			int num = 0;
			for (int i = help.length() - 1; i >= 0; i--) {
				if (help.charAt(i) == ':') {
					num = i;
					break;
				}
			}
			for (int i = 0; i < num; i++) {
				ans += help.charAt(i);
			}
			return ans;
		}

		private void updateCityInDatabase(String city, String time,
				String condition, ArrayList<WeatherAPIItem> weatherHourly) {
			String ansUpd = "" + weatherHourly.get(0).hh + " $ ", ansWeather = "";
			for (int i = 0; i < weatherHourly.size(); i++) {
				ansUpd += weatherHourly.get(i).now + " ";
				ansWeather += " " + weatherHourly.get(i).weather + " $";
			}
			ansUpd += "$";
			curAdapter.updateCity(city, ansUpd, time, condition, ansWeather);
		}
	}
}
