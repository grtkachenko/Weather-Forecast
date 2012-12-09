package ru.ifmo.rain.tkachenko.activities;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import ru.ifmo.rain.tkachenko.alarm.AlarmManagerBroadcastReceiver;
import ru.ifmo.rain.tkachenko.helpers.Cities;
import ru.ifmo.rain.tkachenko.helpers.IconHelper;
import ru.ifmo.rain.tkachenko.helpers.MyGestureDetector;
import ru.ifmo.rain.tkachenko.helpers.WeatherLayoutHelper;
import ru.ifmo.rain.tkachenko.providers.CityDbHelper;
import ru.ifmo.rain.tkachenko.providers.CityDbItem;
import ru.ifmo.rain.tkachenko.weather.R;
import ru.ifmo.rain.tkachenko.weather_api_stuff.WeatherAPIHelper;
import ru.ifmo.rain.tkachenko.weather_api_stuff.WeatherAPIItem;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	public TextView city, temeratureNow, weatherNow, currentCityList,
			whenUpdated;
	public static boolean active = false;
	public static MainActivity mainActivity;
	public ImageView refresh, settings;
	private boolean debugWithNoInternet = false;
	public TableLayout table;
	private RelativeLayout allWorkspace;
	private static CityDbHelper mDbHelper;
	public LinearLayout fourDaysLayout, todayLayout, allBottomLayout;
	private WeatherAPIHelper weatherApiHelper = new WeatherAPIHelper();

	private GestureDetector mGestureDetector;
	private WeatherLayoutHelper[] today = new WeatherLayoutHelper[4];
	private WeatherLayoutHelper[] fourDays = new WeatherLayoutHelper[4];
	private String[] daysOfWeek = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri",
			"Sat" };
	private View line;
	private ProgressBar progressBar;
	private int currentDay = 0;
	private String[] cityList;

	private volatile int currentCity = 0;
	private static HashMap<String, ArrayList<WeatherAPIItem>> cache = new HashMap<String, ArrayList<WeatherAPIItem>>();
	private static HashMap<String, String> updateCache = new HashMap<String, String>();
	private boolean needDeleteDatabase = false;
	public static AlarmManagerBroadcastReceiver alarm = new AlarmManagerBroadcastReceiver();;

	@Override
	protected void onDestroy() {
		MainActivity.active = false;
		super.onDestroy();
	}

	@Override
	protected void onPause() {

		// TODO Auto-generated method stub
		SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
		editor.putInt("currentCity", currentCity);
		editor.putInt("currentDay", currentDay);
		editor.putInt("timeInterval", MainActivity.alarm.getUpdateTime());
		editor.commit();
		super.onDestroy();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		MainActivity.active = true;
		super.onCreate(savedInstanceState);
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		if (prefs.contains("currentCity")) {
			currentCity = prefs.getInt("currentCity", -1);
			currentDay = prefs.getInt("currentDay", -1);
			MainActivity.alarm.setUpdateTime(prefs.getInt("timeInterval", -1));
		}
		MainActivity.mainActivity = this;
		Log.d("db_upd", "In mainActivity active is " + MainActivity.active);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		needChange = Math.max(size.x, size.y) <= 800;
		allWorkspace = (RelativeLayout) findViewById(R.id.all_workspace);
		if (!isNetworkConnected()) {
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle("Oops...");
			alertDialog.setMessage("Please check your connection");
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});

			alertDialog.show();
			allWorkspace.setVisibility(View.INVISIBLE);
			return;
		}
		mGestureDetector = new GestureDetector(this,
				new MyGestureDetector(this));
		city = (TextView) findViewById(R.id.city);
		whenUpdated = (TextView) findViewById(R.id.when_update);
		temeratureNow = (TextView) findViewById(R.id.temperature_now);
		weatherNow = (TextView) findViewById(R.id.weather_now);
		currentCityList = (TextView) findViewById(R.id.current_city_in_list);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		line = (View) findViewById(R.id.line);
		refresh = (ImageView) findViewById(R.id.refresh);
		settings = (ImageView) findViewById(R.id.settings);
		table = (TableLayout) findViewById(R.id.table);
		todayLayout = (LinearLayout) findViewById(R.id.today_layout);
		allBottomLayout = (LinearLayout) findViewById(R.id.allBottomPart);
		fourDaysLayout = (LinearLayout) findViewById(R.id.four_days_layout);
		refresh.setClickable(true);
		settings.setClickable(true);
		refresh.setOnClickListener(this);
		settings.setOnClickListener(this);

		Typeface tf = Typeface
				.createFromAsset(getAssets(), "fonts/rounded.ttf");
		city.setTypeface(tf);
		city.setTextSize(getCitySize(city.getText().length()));

		temeratureNow.setTypeface(tf);
		temeratureNow.setTextSize(45);

		weatherNow.setTypeface(tf);
		today[0] = new WeatherLayoutHelper((TextView) findViewById(R.id.night),
				(TextView) findViewById(R.id.night_temperature),
				(ImageView) findViewById(R.id.night_weather),
				(TableRow) findViewById(R.id.tableRow1));
		today[1] = new WeatherLayoutHelper(
				(TextView) findViewById(R.id.morning),
				(TextView) findViewById(R.id.morning_temperature),
				(ImageView) findViewById(R.id.morning_weather),
				(TableRow) findViewById(R.id.tableRow2));
		today[2] = new WeatherLayoutHelper((TextView) findViewById(R.id.day),
				(TextView) findViewById(R.id.day_temperature),
				(ImageView) findViewById(R.id.day_weather),
				(TableRow) findViewById(R.id.tableRow3));
		today[3] = new WeatherLayoutHelper(
				(TextView) findViewById(R.id.evening),
				(TextView) findViewById(R.id.evening_temperature),
				(ImageView) findViewById(R.id.evening_weather),
				(TableRow) findViewById(R.id.tableRow4));
		for (int i = 0; i < 4; i++) {
			today[i].setTimeFont(Typeface.createFromAsset(getAssets(),
					"fonts/oblique.ttf"));
			today[i].setTimeSize(30);

		}

		fourDays[0] = new WeatherLayoutHelper(
				(ImageView) findViewById(R.id.tick_day1),
				(TextView) findViewById(R.id.day1),
				(TextView) findViewById(R.id.temperature_day1),
				(ImageView) findViewById(R.id.weather_day1),
				(LinearLayout) findViewById(R.id.layout_day1));
		fourDays[1] = new WeatherLayoutHelper(
				(ImageView) findViewById(R.id.tick_day2),
				(TextView) findViewById(R.id.day2),
				(TextView) findViewById(R.id.temperature_day2),
				(ImageView) findViewById(R.id.weather_day2),
				(LinearLayout) findViewById(R.id.layout_day2));
		fourDays[2] = new WeatherLayoutHelper(
				(ImageView) findViewById(R.id.tick_day3),
				(TextView) findViewById(R.id.day3),
				(TextView) findViewById(R.id.temperature_day3),
				(ImageView) findViewById(R.id.weather_day3),
				(LinearLayout) findViewById(R.id.layout_day3));
		fourDays[3] = new WeatherLayoutHelper(
				(ImageView) findViewById(R.id.tick_day4),
				(TextView) findViewById(R.id.day4),
				(TextView) findViewById(R.id.temperature_day4),
				(ImageView) findViewById(R.id.weather_day4),
				(LinearLayout) findViewById(R.id.layout_day4));

		Calendar calendar = Calendar.getInstance();
		int day = (calendar.get(Calendar.DAY_OF_WEEK) - 1);
		for (int i = 0; i < 4; i++) {
			fourDays[i].showTick(false);
			fourDays[i].setTimeText(daysOfWeek[day]);
			fourDays[i].setTimeColor(Color.DKGRAY);
			fourDays[i].setTemperatureValue(7, 15);
			fourDays[i].setTemperatureColor(Color.DKGRAY);
			fourDays[i].setOnClickListener(this);
			day = (day + 1) % 7;
		}
		fourDays[currentDay].showTick(true);
		mDbHelper = new CityDbHelper(this);
		if (needDeleteDatabase) {
			mDbHelper.deleteDatabase();
		}
		Intent intent = new Intent();
		ArrayList<CityDbItem> help = mDbHelper.getAllCityItems();

		String[] data = new String[help.size()];
		for (int i = 0; i < help.size(); i++) {
			data[i] = help.get(i).city;
			if (help.get(i).time.length() != 0) {
				cache.put(data[i], help.get(i).getItemList());
				updateCache.put(data[i], help.get(i).time);
			}
		}

		intent.putExtra("data", data);
		onActivityResult(1, 0, intent);
		initViews();
		Log.d("db_upd", "mDbHelper1" + MainActivity.mDbHelper);

		alarm.setAlarm(this);
	}

	public static void updateCache() {
		// Log.d("db_upd", "mDbHelper" + mDbHelper);
		Log.d("db_upd", "mDbHelper2" + MainActivity.mDbHelper);
		final ArrayList<CityDbItem> help = MainActivity.mDbHelper
				.getAllCityItems();
		for (int i = 0; i < help.size(); i++) {
			if (help.get(i).time.length() != 0) {
				cache.put(help.get(i).city, help.get(i).getItemList());
				updateCache.put(help.get(i).city, help.get(i).time);
			}
		}

	}

	public int getCitySize(int val) {
		if (val <= 10) {
			return 45;
		} else {
			return 45 * 10 / val;
		}
	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt("curCity", currentCity);
		outState.putInt("curDay", currentDay);
	}

	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		currentCity = savedInstanceState.getInt("curCity");
		currentDay = savedInstanceState.getInt("curDay");
		for (int i = 0; i < 4; i++) {
			fourDays[i].showTick(false);
		}
		fourDays[currentDay].showTick(true);
		View v = fourDays[currentDay].layout;
		onClick(v);
	}

	boolean needChange = false;

	private void initViews() {
		if (needChange) {

			LinearLayout topLayout = (LinearLayout) findViewById(R.id.topLayout);
			LinearLayout fourDaysWithLine = (LinearLayout) findViewById(R.id.fourDaysWithLine);
			topLayout
					.setLayoutParams(new LinearLayout.LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.MATCH_PARENT, 3.2f));
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			fourDaysWithLine.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 2f));
		}
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.layout_day1:
			if (currentDay == 0) {
				break;
			}
			table.startAnimation(AnimationUtils.loadAnimation(this,
					R.anim.falling_from_left));
			fourDays[currentDay].showTick(false);
			currentDay = 0;
			fourDays[currentDay].showTick(true);
			fillToday();
			break;
		case R.id.layout_day2:
			if (currentDay == 1) {
				break;
			}
			table.startAnimation(AnimationUtils.loadAnimation(this,
					R.anim.falling_from_left));

			fourDays[currentDay].showTick(false);
			currentDay = 1;
			fourDays[currentDay].showTick(true);
			fillToday();
			break;
		case R.id.layout_day3:
			if (currentDay == 2) {
				break;
			}
			table.startAnimation(AnimationUtils.loadAnimation(this,
					R.anim.falling_from_left));
			fourDays[currentDay].showTick(false);
			currentDay = 2;
			fourDays[currentDay].showTick(true);
			fillToday();
			break;
		case R.id.layout_day4:
			if (currentDay == 3) {
				break;
			}
			table.startAnimation(AnimationUtils.loadAnimation(this,
					R.anim.falling_from_left));
			fourDays[currentDay].showTick(false);
			currentDay = 3;
			fourDays[currentDay].showTick(true);
			fillToday();
			break;
		case R.id.settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivityForResult(intent, 1);
			break;
		case R.id.refresh:
			isRefresh = true;
			currentCity--;
			MyGestureDetector.last = 1;
			toNextCity();
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mGestureDetector.onTouchEvent(event))
			return true;
		else
			return false;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1 && data != null) {

			Bundle extra = data.getExtras();
			if (extra != null) {
				ArrayList<CityDbItem> help = mDbHelper.getAllCityItems();
				for (int i = 0; i < help.size(); i++) {
					if (help.get(i).time.length() != 0) {
						cache.put(help.get(i).city, help.get(i).getItemList());
						updateCache.put(help.get(i).city, help.get(i).time);
					}
				}
				allBottomLayout.setVisibility(View.INVISIBLE);
				HashMap<String, ArrayList<WeatherAPIItem>> tmpCache = new HashMap<String, ArrayList<WeatherAPIItem>>();
				HashMap<String, String> tmpCacheUpd = new HashMap<String, String>();
				cityList = extra.getStringArray("data");
				updateCache();
				updateCache = tmpCacheUpd;
				cache = tmpCache;

				if (cityList.length == 0) {
					Toast.makeText(getApplicationContext(),
							"Please choose at least one city",
							Toast.LENGTH_LONG).show();
					onClick(settings);
					return;
				}
				if (currentCity > cityList.length) {
					currentCity = 0;
				} else {
					currentCity--;
				}
				if (currentCity < 0 || currentCity >= cityList.length) {
					currentCity = 0;
				}
				allWorkspace.setVisibility(View.VISIBLE);
				fourDaysLayout.setVisibility(View.INVISIBLE);
				table.setVisibility(View.INVISIBLE);
				todayLayout.setVisibility(View.INVISIBLE);
				line.setVisibility(View.INVISIBLE);
				MyGestureDetector.last = 1;
				currentCityList.setText("" + (currentCity + 1) + "/"
						+ cityList.length);
				whenUpdated.setText("...");
				toNextCity();
			}
		}
	}

	public void toNextCity() {
		currentCity++;
		if (currentCity == cityList.length + 1) {
			currentCity = 1;
		}

		MyTask mt = new MyTask();
		mt.execute();
	}

	public void toPrevCity() {
		currentCity--;
		if (currentCity == 0) {
			currentCity = cityList.length;
		}

		MyTask mt = new MyTask();
		mt.execute();

	}

	private void setCityByNumber(int index) {
		currentCityList.setText("" + index + "/" + cityList.length);
		city.setText(Cities.getOKCity(cityList[index - 1]));
		city.setTextSize(getCitySize(Cities.getCity(cityList[index - 1])
				.length()));

	}

	public volatile boolean f = true, isRefresh = false, isCalced = true;;

	class MyTask extends AsyncTask<Void, Void, Void> {
		private volatile ArrayList<WeatherAPIItem> weatherHourly;
		ArrayList<WeatherAPIItem> cur;

		@Override
		protected void onPreExecute() {
			isCalced = false;
			super.onPreExecute();
			if (!cache.containsKey(cityList[currentCity - 1])) {
				progressBar.setVisibility(View.VISIBLE);

				Animation translate = null, translateDelay1 = null, translateDelay2 = null;
				if (MyGestureDetector.last != 1) {
					translate = AnimationUtils.loadAnimation(MainActivity.this,
							R.anim.falling_to_left);
					translateDelay1 = AnimationUtils.loadAnimation(
							MainActivity.this, R.anim.falling_to_left);
					translateDelay2 = AnimationUtils.loadAnimation(
							MainActivity.this, R.anim.falling_to_left);
				} else {
					translate = AnimationUtils.loadAnimation(MainActivity.this,
							R.anim.falling_to_right);
					translateDelay1 = AnimationUtils.loadAnimation(
							MainActivity.this, R.anim.falling_to_right);
					translateDelay2 = AnimationUtils.loadAnimation(
							MainActivity.this, R.anim.falling_to_right);
				}
				translateDelay1.setStartOffset(200);
				translateDelay2.setStartOffset(400);

				city.startAnimation(translate);
				temeratureNow.startAnimation(translate);
				weatherNow.startAnimation(translate);
				table.startAnimation(translateDelay1);
				fourDaysLayout.startAnimation(translateDelay2);
				line.startAnimation(translateDelay2);
				translate.setAnimationListener(new AnimationListener() {

					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						city.setVisibility(View.INVISIBLE);
						temeratureNow.setVisibility(View.INVISIBLE);
						weatherNow.setVisibility(View.INVISIBLE);
					}

					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub

					}

					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub

					}
				});
				translateDelay1.setAnimationListener(new AnimationListener() {

					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						table.setVisibility(View.INVISIBLE);
					}

					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub

					}

					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub

					}
				});
				translateDelay2.setAnimationListener(new AnimationListener() {

					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						fourDaysLayout.setVisibility(View.INVISIBLE);
						line.setVisibility(View.INVISIBLE);
					}

					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub

					}

					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub

					}
				});
			}
			if (isRefresh) {
				progressBar.setVisibility(View.VISIBLE);
			}

		}

		@SuppressWarnings("deprecation")
		@Override
		protected Void doInBackground(Void... params) {
			updateCache();
			if (debugWithNoInternet) {
				return null;
			}
			if (!isNetworkConnected()) {
				AlertDialog alertDialog = new AlertDialog.Builder(mainActivity)
						.create();
				alertDialog.setTitle("Oops...");
				alertDialog.setMessage("Please check your connection");
				alertDialog.setButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int which) {
								finish();
							}
						});

				alertDialog.show();
				allWorkspace.setVisibility(View.INVISIBLE);
				finish();
			}

			if (cache.containsKey(cityList[currentCity - 1]) && !isRefresh) {
				weatherHourly = cache.get(cityList[currentCity - 1]);
				return null;
			}
			updateCache.put(cityList[currentCity - 1], getCurrentTime());

			if (Cities.getCountry(cityList[currentCity - 1]).equals(
					"United_States")) {
				weatherHourly = weatherApiHelper.getHourWeather(Cities.states
						.get(Cities.getCity(cityList[currentCity - 1])), Cities
						.getCity(cityList[currentCity - 1]));

			} else {

				weatherHourly = weatherApiHelper.getHourWeather(
						Cities.getCountry(cityList[currentCity - 1]),
						Cities.getCity(cityList[currentCity - 1]));
			}
			if (!cache.containsKey(cityList[currentCity - 1]) || isRefresh) {
				cache.put(cityList[currentCity - 1], weatherHourly);
				updateCache.put(cityList[currentCity - 1], getCurrentTime());
			}
			isRefresh = false;
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			allBottomLayout.setVisibility(View.VISIBLE);
			isCalced = true;
			if (debugWithNoInternet) {
				progressBar.setVisibility(View.INVISIBLE);
				fourDaysLayout.setVisibility(View.VISIBLE);
				table.setVisibility(View.VISIBLE);
				city.setVisibility(View.INVISIBLE);
				temeratureNow.setVisibility(View.VISIBLE);
				weatherNow.setVisibility(View.VISIBLE);
				todayLayout.setVisibility(View.VISIBLE);
				line.setVisibility(View.VISIBLE);
				return;
			}
			int num = 0;
			cur = new ArrayList<WeatherAPIItem>();
			if (weatherHourly == null) {
				Toast.makeText(
						getApplicationContext(),
						"Sorry, weather API doesn't have this city. Please, choose another one",
						Toast.LENGTH_LONG).show();
				progressBar.setVisibility(View.INVISIBLE);
				fourDaysLayout.setVisibility(View.INVISIBLE);
				table.setVisibility(View.INVISIBLE);
				todayLayout.setVisibility(View.INVISIBLE);
				city.setVisibility(View.INVISIBLE);
				temeratureNow.setVisibility(View.INVISIBLE);
				line.setVisibility(View.INVISIBLE);
				setCityByNumber(currentCity);
				return;
			}
			for (int i = 0; i < weatherHourly.size(); i++) {
				if (weatherHourly.get(i).hh == 0) {
					if (num > 3) {
						break;
					}
					if (cur.size() == 0) {
						cur.add(weatherHourly.get(i));
					}
					fourDays[num].fillData(cur);
					num++;
					cur = new ArrayList<WeatherAPIItem>();
				}
				cur.add(weatherHourly.get(i));
			}
			fillFourDays();
			fillToday();
			temeratureNow.setText(weatherHourly.get(0).nowString());
			weatherNow.setText(weatherHourly.get(0).condition);
			setCityByNumber(currentCity);
			Animation translate = null, translateDelay1 = null, translateDelay2 = null;
			if (MyGestureDetector.last == 1) {
				translate = AnimationUtils.loadAnimation(MainActivity.this,
						R.anim.falling_from_left);
				translateDelay1 = AnimationUtils.loadAnimation(
						MainActivity.this, R.anim.falling_from_left);
				translateDelay2 = AnimationUtils.loadAnimation(
						MainActivity.this, R.anim.falling_from_left);
			} else {
				translate = AnimationUtils.loadAnimation(MainActivity.this,
						R.anim.falling_from_right);
				translateDelay1 = AnimationUtils.loadAnimation(
						MainActivity.this, R.anim.falling_from_right);
				translateDelay2 = AnimationUtils.loadAnimation(
						MainActivity.this, R.anim.falling_from_right);
			}
			translateDelay1.setStartOffset(200);
			translateDelay2.setStartOffset(400);

			whenUpdated.setText(updateCache.get(cityList[currentCity - 1]));
			updateCityInDatabase(cityList[currentCity - 1],
					updateCache.get(cityList[currentCity - 1]),
					weatherHourly.get(0).condition);

			city.startAnimation(translate);
			temeratureNow.startAnimation(translate);
			weatherNow.startAnimation(translate);
			table.startAnimation(translateDelay1);
			fourDaysLayout.startAnimation(translateDelay2);
			line.startAnimation(translateDelay2);

			progressBar.setVisibility(View.INVISIBLE);
			city.setVisibility(View.VISIBLE);
			temeratureNow.setVisibility(View.VISIBLE);
			weatherNow.setVisibility(View.VISIBLE);
			fourDaysLayout.setVisibility(View.VISIBLE);
			table.setVisibility(View.VISIBLE);
			todayLayout.setVisibility(View.VISIBLE);
			line.setVisibility(View.VISIBLE);
		}

		private void updateCityInDatabase(String city, String time,
				String condition) {
			String ansUpd = "" + weatherHourly.get(0).hh + " $ ", ansWeather = "";
			for (int i = 0; i < weatherHourly.size(); i++) {
				ansUpd += weatherHourly.get(i).now + " ";
				ansWeather += " " + weatherHourly.get(i).weather + " $";
			}
			ansUpd += "$";
			mDbHelper.updateCity(city, ansUpd, time, condition, ansWeather);
		}

		private void fillFourDays() {
			for (int i = 0; i < 4; i++) {
				fourDays[i].setTemperatureValue(fourDays[i].min,
						fourDays[i].max);
				fourDays[i].setWeatherImage(IconHelper
						.getId(fourDays[i].weatherIcon));

			}
		}
	}

	boolean isRemoved = false;

	public void fillToday() {
		if (fourDays[currentDay].today.size() == 4) {
			if (isRemoved) {
				table.removeAllViews();
				for (int i = 0; i < 4; i++) {
					table.addView(today[i].tableRow);
				}
				isRemoved = false;

			}
			for (int i = 0; i < today.length; i++) {
				today[i].setWeatherImage(IconHelper
						.getId(fourDays[currentDay].today.get(i).weatherIcon));
				today[i].setTemperatureValue(fourDays[currentDay].today.get(i).now);
				today[i].setName(fourDays[currentDay].today.get(i).timeString);
			}
		} else {
			if (isRemoved) {
				table.removeAllViews();
				for (int i = 0; i < 4; i++) {
					table.addView(today[i].tableRow);
				}
				isRemoved = false;

			}
			table.removeViews(0, 4 - fourDays[currentDay].today.size());
			isRemoved = true;
			int index = 0;
			for (int i = 4 - fourDays[currentDay].today.size(); i < today.length; i++) {
				today[i].setWeatherImage(IconHelper
						.getId(fourDays[currentDay].today.get(index).weatherIcon));
				today[i].setTemperatureValue(fourDays[currentDay].today
						.get(index).now);
				today[i].setName(fourDays[currentDay].today.get(index).timeString);

				index++;
			}
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

	private boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			// There are no active networks.
			return false;
		} else
			return true;
	}
}
