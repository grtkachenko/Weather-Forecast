package ru.ifmo.rain.tkachenko.activities;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import ru.ifmo.rain.tkachenko.weather.Cities;
import ru.ifmo.rain.tkachenko.weather.CityDbAdapter;
import ru.ifmo.rain.tkachenko.weather.IconHelper;
import ru.ifmo.rain.tkachenko.weather.MyGestureDetector;
import ru.ifmo.rain.tkachenko.weather.R;
import ru.ifmo.rain.tkachenko.weather.WeatherAPIHelper;
import ru.ifmo.rain.tkachenko.weather.WeatherAPIItem;
import ru.ifmo.rain.tkachenko.weather.WeatherLayoutHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	public TextView city, temeratureNow, weatherNow, currentCityList,
			whenUpdated;
	public ImageView refresh, settings;
	public TableLayout table;
	private RelativeLayout allWorkspace;
	private CityDbAdapter mDbHelper;
	public LinearLayout fourDaysLayout, todayLayout;
	private WeatherAPIHelper weatherApiHelper = new WeatherAPIHelper();

	private GestureDetector mGestureDetector;
	private WeatherLayoutHelper[] today = new WeatherLayoutHelper[4];
	private WeatherLayoutHelper[] fourDays = new WeatherLayoutHelper[4];
	private String[] daysOfWeek = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri",
			"Sat" };
	private View line;
	private ProgressBar progressBar;
	private int curDay = 0;
	private String[] cityList;

	private volatile int currentCity = 0;
	private HashMap<String, ArrayList<WeatherAPIItem>> cache = new HashMap<String, ArrayList<WeatherAPIItem>>();
	private HashMap<String, String> updateCache = new HashMap<String, String>();

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);

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
		fourDays[0].showTick(true);
		mDbHelper = new CityDbAdapter(this);
		mDbHelper.open();

		Intent intent = new Intent();
		ArrayList<String> help = mDbHelper.getAllCities();

		String[] data = new String[help.size()];
		for (int i = 0; i < help.size(); i++) {
			data[i] = help.get(i);
		}

		intent.putExtra("data", data);
		onActivityResult(1, 0, intent);
		initViews();
	}

	public int getCitySize(int val) {
		if (val <= 10) {
			return 45;
		} else {
			return 45 * 10 / val;
		}
	}

	private void initViews() {
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.layout_day1:
			if (curDay == 0) {
				break;
			}
			table.startAnimation(AnimationUtils.loadAnimation(this,
					R.anim.falling_from_left));
			fourDays[curDay].showTick(false);
			curDay = 0;
			fourDays[curDay].showTick(true);
			fillToday();
			break;
		case R.id.layout_day2:
			if (curDay == 1) {
				break;
			}
			table.startAnimation(AnimationUtils.loadAnimation(this,
					R.anim.falling_from_left));

			fourDays[curDay].showTick(false);
			curDay = 1;
			fourDays[curDay].showTick(true);
			fillToday();
			break;
		case R.id.layout_day3:
			if (curDay == 2) {
				break;
			}
			table.startAnimation(AnimationUtils.loadAnimation(this,
					R.anim.falling_from_left));
			fourDays[curDay].showTick(false);
			curDay = 2;
			fourDays[curDay].showTick(true);
			fillToday();
			break;
		case R.id.layout_day4:
			if (curDay == 3) {
				break;
			}
			table.startAnimation(AnimationUtils.loadAnimation(this,
					R.anim.falling_from_left));
			fourDays[curDay].showTick(false);
			curDay = 3;
			fourDays[curDay].showTick(true);
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
				HashMap<String, ArrayList<WeatherAPIItem>> tmpCache = new HashMap<String, ArrayList<WeatherAPIItem>>();
				HashMap<String, String> tmpCacheUpd = new HashMap<String, String>();
				cityList = extra.getStringArray("data");
				for (int i = 0; i < cityList.length; i++) {
					if (cache.containsKey(cityList[i])) {
						tmpCache.put(cityList[i], cache.get(cityList[i]));
						tmpCacheUpd.put(cityList[i],
								updateCache.get(cityList[i]));
					}
				}
				updateCache = tmpCacheUpd;
				cache = tmpCache;

				if (cityList.length == 0) {
					Toast.makeText(getApplicationContext(),
							"Please choose at least one city",
							Toast.LENGTH_LONG).show();
					onClick(settings);
					return;
				}
				currentCity = 0;
				allWorkspace.setVisibility(View.VISIBLE);
				fourDaysLayout.setVisibility(View.INVISIBLE);
				table.setVisibility(View.INVISIBLE);
				todayLayout.setVisibility(View.INVISIBLE);
				line.setVisibility(View.INVISIBLE);
				MyGestureDetector.last = 1;
				currentCityList.setText("" + 1 + "/" + cityList.length);
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

	volatile boolean f = true, isRefresh = false;

	class MyTask extends AsyncTask<Void, Void, Void> {
		private volatile ArrayList<WeatherAPIItem> weatherHourly;
		ArrayList<WeatherAPIItem> cur;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (!cache.containsKey(cityList[currentCity - 1]) || isRefresh) {
				progressBar.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
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
			city.startAnimation(translate);
			temeratureNow.startAnimation(translate);
			weatherNow.startAnimation(translate);
			table.startAnimation(translateDelay1);
			fourDaysLayout.startAnimation(translateDelay2);
			line.startAnimation(translateDelay2);

			progressBar.setVisibility(View.INVISIBLE);
			fourDaysLayout.setVisibility(View.VISIBLE);
			table.setVisibility(View.VISIBLE);
			todayLayout.setVisibility(View.VISIBLE);
			line.setVisibility(View.VISIBLE);
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
		if (fourDays[curDay].today.size() == 4) {
			if (isRemoved) {
				table.removeAllViews();
				for (int i = 0; i < 4; i++) {
					table.addView(today[i].tableRow);
				}
				isRemoved = false;

			}
			for (int i = 0; i < today.length; i++) {
				today[i].setWeatherImage(IconHelper
						.getId(fourDays[curDay].today.get(i).weatherIcon));
				today[i].setTemperatureValue(fourDays[curDay].today.get(i).now);
				today[i].setName(fourDays[curDay].today.get(i).timeString);
			}
		} else {
			if (isRemoved) {
				table.removeAllViews();
				for (int i = 0; i < 4; i++) {
					table.addView(today[i].tableRow);
				}
				isRemoved = false;

			}
			table.removeViews(0, 4 - fourDays[curDay].today.size());
			isRemoved = true;
			int index = 0;
			for (int i = 4 - fourDays[curDay].today.size(); i < today.length; i++) {
				today[i].setWeatherImage(IconHelper
						.getId(fourDays[curDay].today.get(index).weatherIcon));
				today[i].setTemperatureValue(fourDays[curDay].today.get(index).now);
				today[i].setName(fourDays[curDay].today.get(index).timeString);

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
