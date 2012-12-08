package ru.ifmo.rain.tkachenko.providers;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import ru.ifmo.rain.tkachenko.helpers.Cities;
import ru.ifmo.rain.tkachenko.weather_api_stuff.WeatherAPIHelper;
import ru.ifmo.rain.tkachenko.weather_api_stuff.WeatherAPIItem;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncTask;

public class CityDbHelper {
	public static final String KEY_CITY = "city";
	public static final String KEY_TEMP = "temp";
	public static final String KEY_TIME = "time";
	public static final String KEY_WEATHER = "weather";
	public static final String KEY_CONDITION = "condition";
	private Cursor cityCursor;
	private Context myContext;

	public CityDbHelper(Context ctx) {
		this.myContext = ctx;
	}

	public void createCity(String city, String temp, String time,
			String weather, String condition) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_CITY, city);
		initialValues.put(KEY_TEMP, temp);
		initialValues.put(KEY_TIME, time);
		initialValues.put(KEY_WEATHER, weather);
		initialValues.put(KEY_CONDITION, condition);

		myContext.getContentResolver().insert(
				CityContentProvider.CONTENT_CITY_URI, initialValues);
	}

	public void deleteCity(long rowId) {
		Uri uri = ContentUris.withAppendedId(
				CityContentProvider.CONTENT_CITY_URI, rowId);
		myContext.getContentResolver().delete(uri, null, null);
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
		return myContext.getContentResolver().query(
				CityContentProvider.CONTENT_CITY_URI, null, null, null, null);
	}

	public Cursor fetchCity(long rowId) throws SQLException {
		Uri uri = Uri.withAppendedPath(CityContentProvider.CONTENT_CITY_URI,
				Long.toString(rowId));
		Cursor mCursor = myContext.getContentResolver().query(uri, null, null,
				null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public void updateCity(long rowId, String city, String temp, String time,
			String weather, String condition) {
		ContentValues args = new ContentValues();
		args.put(KEY_CITY, city);
		args.put(KEY_TEMP, temp);
		args.put(KEY_TIME, time);
		args.put(KEY_WEATHER, weather);
		args.put(KEY_CONDITION, condition);
		Uri uri = Uri.withAppendedPath(CityContentProvider.CONTENT_CITY_URI,
				Long.toString(rowId));
		myContext.getContentResolver().update(uri, args, null, null);
	}

	public void deleteDatabase() {
		myContext.getContentResolver().delete(
				CityContentProvider.CONTENT_CITY_URI, null, null);
	}

	public void updateCity(String name, String city, String temp, String time,
			String weather, String condition) {
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
		this.updateCity(id, city, temp, time, weather, condition);
	}

	public void updateCity(String city, String temp, String time,
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
		this.updateCity(id, city, temp, time, weather, condition);
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
		curAdapter = this;
		MyTask mt = new MyTask();
		mt.execute();
	}

	public volatile boolean f = true, isRefresh = false, isCalced = true;
	public CityDbHelper curAdapter = null;

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
