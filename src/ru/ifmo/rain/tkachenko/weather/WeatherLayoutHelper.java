package ru.ifmo.rain.tkachenko.weather;

import android.graphics.Typeface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherLayoutHelper {
	private LinearLayout layout;
	private TextView time, temperature;
	private ImageView weather, tick;


	public WeatherLayoutHelper(TextView time, TextView temperature, ImageView weather, LinearLayout layout) {
		this.weather = weather;
		this.temperature = temperature;
		this.time = time;
		this.layout = layout;
		this.layout.setClickable(true);
	}
	public WeatherLayoutHelper(TextView time, TextView temperature, ImageView weather) {
		this.weather = weather;
		this.temperature = temperature;
		this.time = time;
	}
	public WeatherLayoutHelper(ImageView tick, TextView time, TextView temperature, ImageView weather, LinearLayout layout) {
		this.weather = weather;
		this.temperature = temperature;
		this.time = time;
		this.tick = tick;
		this.layout = layout;
		this.layout.setClickable(true);
	}
	

	void setTimeFont(Typeface tf) {
		time.setTypeface(tf);
	}

	void setTimeSize(float size) {
		time.setTextSize(size);
	}

	void setTimeColor(int color) {
		time.setTextColor(color);
	}

	void setTimeText(String text) {
		time.setText(text);
	}

	void setTemperatureColor(int color) {
		temperature.setTextColor(color);
	}

	void setTemperatureFont(Typeface tf) {
		temperature.setTypeface(tf);
	}

	void setTemperatureSize(float size) {
		temperature.setTextSize(size);
	}

	void setTemperatureValue(int from, int to) {
		temperature.setText("" + from + "°/" + to + "°");
	}
	
	void setWeatherImage(int resId) {
		weather.setImageResource(resId);
	}
	
	void showTick(boolean f) {
		if (f) {
			tick.setVisibility(View.VISIBLE);
		} else {
			tick.setVisibility(View.INVISIBLE);
		}
	}
	
	void setOnClickListener(OnClickListener o) {
		layout.setOnClickListener(o);
	}
}
