package ru.ifmo.rain.tkachenko.weather;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.TableLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
	TextView city, temeratureNow, weatherNow;
	ImageView refresh, settings;
	TableLayout table;
	LinearLayout fourDaysLayout;
	private WeatherAPIHelper weatherApiHelper = new WeatherAPIHelper();
	private GestureDetector mGestureDetector;
	private WeatherLayoutHelper[] today = new WeatherLayoutHelper[4];
	private WeatherLayoutHelper[] fourDays = new WeatherLayoutHelper[4];
	private String[] daysOfWeek = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri",
			"Sat" };
	private int curDay = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);

		WeatherAPIItem weather = weatherApiHelper.getHourWeather("Russia",
				"Saint_Petersburg", 2, 14);

		mGestureDetector = new GestureDetector(this,
				new MyGestureDetector(this));
		city = (TextView) findViewById(R.id.city);
		temeratureNow = (TextView) findViewById(R.id.temperature_now);
		weatherNow = (TextView) findViewById(R.id.weather_now);
		weatherNow.setText(weather.weather);
		temeratureNow.setText(weather.nowString());

		refresh = (ImageView) findViewById(R.id.refresh);
		settings = (ImageView) findViewById(R.id.settings);
		table = (TableLayout) findViewById(R.id.table);
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
				(ImageView) findViewById(R.id.night_weather));
		today[1] = new WeatherLayoutHelper(
				(TextView) findViewById(R.id.morning),
				(TextView) findViewById(R.id.morning_temperature),
				(ImageView) findViewById(R.id.morning_weather));
		today[2] = new WeatherLayoutHelper((TextView) findViewById(R.id.day),
				(TextView) findViewById(R.id.day_temperature),
				(ImageView) findViewById(R.id.day_weather));
		today[3] = new WeatherLayoutHelper(
				(TextView) findViewById(R.id.evening),
				(TextView) findViewById(R.id.evening_temperature),
				(ImageView) findViewById(R.id.evening_weather));
		for (int i = 0; i < 4; i++) {
			today[i].setTimeFont(Typeface.createFromAsset(getAssets(),
					"fonts/oblique.ttf"));
			today[i].setTimeSize(20);

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
		today[1].setWeatherImage(R.drawable.partly_cloudy);
		today[3].setWeatherImage(R.drawable.snowy);
		fourDays[2].setWeatherImage(R.drawable.partly_cloudy);
	}

	int getCitySize(int val) {
		if (val <= 10) {
			return 45;
		} else {
			return 45 * 10 / val;
		}
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.layout_day1:
			if (curDay == 0) {
				break;
			}
			fourDays[curDay].showTick(false);
			curDay = 0;
			fourDays[curDay].showTick(true);
			break;
		case R.id.layout_day2:
			if (curDay == 1) {
				break;
			}
			Animation translate = AnimationUtils.loadAnimation(this,
					R.anim.falling_from_right);
			table.startAnimation(translate);

			fourDays[curDay].showTick(false);
			curDay = 1;
			fourDays[curDay].showTick(true);
			break;
		case R.id.layout_day3:
			if (curDay == 2) {
				break;
			}
			fourDays[curDay].showTick(false);
			curDay = 2;
			fourDays[curDay].showTick(true);
			break;
		case R.id.layout_day4:
			if (curDay == 3) {
				break;
			}
			fourDays[curDay].showTick(false);
			curDay = 3;
			fourDays[curDay].showTick(true);
			break;
		case R.id.settings:
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, SettingsActivity.class);
			startActivity(intent);
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
}
