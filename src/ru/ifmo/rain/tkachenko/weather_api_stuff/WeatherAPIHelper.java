package ru.ifmo.rain.tkachenko.weather_api_stuff;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherAPIHelper {
	// private static final String KEY = "34239673cd9dde87";
	// private static final String KEY_FIRST = "b6de292546742ab5";
	// private static final String KEY_SECOND = "368167e36affbb8e";
	private static final String KEYS[] = { "368167e36affbb8e",
			"b6de292546742ab5", "34239673cd9dde87" };
	private static Random random = new Random();
	private static int pos = random.nextInt(3);

	volatile private String link = "";

	WeatherAPIItem getBriefForecast(String country, String city) {
		return null;
	}

	public ArrayList<WeatherAPIItem> getHourWeather(String country, String city) {
		link = "http://api.wunderground.com/api/" + KEYS[pos]
				+ "/hourly10day/q/" + country + "/" + city + ".json";
		pos++;
		pos %= 3;
		ArrayList<WeatherAPIItem> ans = new ArrayList<WeatherAPIItem>();
		JSONObject json = getJSONByLink(link);
		try {
			JSONArray array = json.getJSONArray("hourly_forecast");

			for (int i = 0; i < 100; i++) {
				int hnow = Integer.parseInt(array.getJSONObject(i)
						.getJSONObject("FCTTIME").getString("hour")) % 24;
				int tempNow = Integer.parseInt(array.getJSONObject(i)
						.getJSONObject("temp").getString("metric"));
				String condition = array.getJSONObject(i)
						.getString("condition");
				String weatherNow = array.getJSONObject(i).getString("icon");
				ans.add(new WeatherAPIItem(country, city, weatherNow, hnow,
						tempNow, condition));

			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			return null;
		}
		return ans;
	}

	public JSONObject getJSONByLink(String link) {
		JSONObject json = null;
		try {
			URL url = new URL(link);
			URLConnection connection = url.openConnection();
			String line;
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			json = new JSONObject(builder.toString());

		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
}
