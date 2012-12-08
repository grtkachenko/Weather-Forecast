package ru.ifmo.rain.tkachenko.helpers;

import java.util.ArrayList;

import ru.ifmo.rain.tkachenko.activities.MainActivity;
import ru.ifmo.rain.tkachenko.weather_api_stuff.WeatherAPIItem;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class WeatherLayoutHelper {
	public LinearLayout layout;
	public TableRow tableRow;
	public int min, max, now;
	public String weatherIcon, timeString, whenUpdated;

	private TextView time, temperature;
	private static final int[] checkPointHour = { 0, 7, 13, 20 };
	private ImageView weather, tick;
	public ArrayList<WeatherLayoutHelper> today;

	public void fillData(ArrayList<WeatherAPIItem> data) {
		int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		this.today = new ArrayList<WeatherLayoutHelper>();
		for (int i = 0; i < data.size(); i++) {
			min = Math.min(min, data.get(i).now);
			max = Math.max(max, data.get(i).now);
			for (int j = 0; j < 4; j++) {
				if (data.get(i).hh == checkPointHour[j]) {
					String time = "";
					if (j == 0) {
						time = "night";
					}
					if (j == 1) {
						time = "morning";
					}
					if (j == 2) {
						time = "day";
					}
					if (j == 3) {
						time = "evening";
					}

					today.add(new WeatherLayoutHelper(time, data.get(i).now,
							data.get(i).weather));
				}
			}
		}
		if (today.size() == 0) {
			today.add(new WeatherLayoutHelper("evening",
					data.get(data.size() - 1).now,
					data.get(data.size() - 1).weather));
		}
		int index = this.today.size() / 2;
		this.weatherIcon = this.today.get(index).weatherIcon;
		this.max = max;
		this.min = min;
	}

	public WeatherLayoutHelper(String timeString, int now, String weatherIcon) {
		this.weatherIcon = weatherIcon;
		this.now = now;
		this.timeString = timeString;
	}

	public WeatherLayoutHelper(TextView time, TextView temperature,
			ImageView weather, LinearLayout layout) {
		this.weather = weather;
		this.temperature = temperature;
		this.time = time;
		this.layout = layout;
		this.layout.setClickable(true);
	}

	public WeatherLayoutHelper(TextView time, TextView temperature,
			ImageView weather, TableRow tableRow) {
		this.weather = weather;
		this.temperature = temperature;
		this.time = time;
		this.tableRow = tableRow;
	}

	public WeatherLayoutHelper(ImageView tick, TextView time,
			TextView temperature, ImageView weather, LinearLayout layout) {
		this.weather = weather;
		this.temperature = temperature;
		this.time = time;
		this.tick = tick;
		this.layout = layout;
		this.layout.setClickable(true);
	}

	public void setTimeFont(Typeface tf) {
		time.setTypeface(tf);
	}

	public void setTimeSize(float size) {
		time.setTextSize(size);
	}

	public void setTimeColor(int color) {
		time.setTextColor(color);
	}

	public void setTimeText(String text) {
		time.setText(text);
	}

	public void setTemperatureColor(int color) {
		temperature.setTextColor(color);
	}

	public void setTemperatureFont(Typeface tf) {
		temperature.setTypeface(tf);
	}

	public void setTemperatureSize(float size) {
		DisplayMetrics metrics = MainActivity.mainActivity.getResources()
				.getDisplayMetrics();
		float fpixels = metrics.density * size;
		temperature.setTextSize(fpixels);
	}

	public void setTemperatureValue(int from, int to) {
		temperature.setText("" + getOKTemp(from) + "°/" + getOKTemp(to) + "°");
	}

	public void setTemperatureValue(int now) {
		temperature.setText(getOKTemp(now) + "°");
	}

	private String getOKTemp(int temp) {
		if (temp < 0) {
			return "—" + (-temp);
		}
		return "" + temp;
	}

	public void setName(String name) {
		time.setText(name);
	}

	public void setWeatherImage(int resId) {
		weather.setImageResource(resId);
	}

	public void showTick(boolean f) {
		if (f) {
			tick.setVisibility(View.VISIBLE);
		} else {
			tick.setVisibility(View.INVISIBLE);
		}
	}

	public void setOnClickListener(OnClickListener o) {
		layout.setOnClickListener(o);
	}
}
