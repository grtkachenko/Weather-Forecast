package ru.ifmo.rain.tkachenko.providers;

import java.util.ArrayList;
import java.util.StringTokenizer;

import ru.ifmo.rain.tkachenko.helpers.Cities;
import ru.ifmo.rain.tkachenko.weather_api_stuff.WeatherAPIItem;

public class CityDbItem {
	public String city, temp, time, weather, condition;

	public CityDbItem(String city, String temp, String time, String weather,
			String condition) {
		this.city = city;
		this.temp = temp;
		this.time = time;
		this.weather = weather;
		this.condition = condition;
	}

	public ArrayList<WeatherAPIItem> getItemList() {
		ArrayList<WeatherAPIItem> ans = new ArrayList<WeatherAPIItem>();
		StringTokenizer stok = new StringTokenizer(temp);
		StringTokenizer weatherStok = new StringTokenizer(weather);
		
		int curh = Integer.parseInt(stok.nextToken().toString());
		stok.nextToken();
		boolean f = true;
		while (f) {
			String temp = stok.nextToken();
			if (temp.equals("$")) {
				break;
			}
			WeatherAPIItem cur = new WeatherAPIItem(Cities.getCountry(city),
					city, weatherStok.nextToken(), curh, Integer.parseInt(temp), condition);
			curh = (curh + 1) % 24;
			weatherStok.nextToken();
			ans.add(cur);
		}
		return ans;
	}
}
