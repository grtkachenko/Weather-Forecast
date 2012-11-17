package ru.ifmo.rain.tkachenko.weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.BitmapFactory;

public class WeatherAPIHelper {
	private static final String KEY = "34239673cd9dde87";

	WeatherAPIItem getBriefForecast(String country, String city) {
		return null;
	}

	static public WeatherAPIItem getCurrentWeather(String country, String city) {
		JSONObject json = getJSONByLink("http://api.wunderground.com/api/"
				+ KEY + "/conditions/q/" + country + "/" + city + ".json");
		String temperatureString = "", weather = "";
		try {
			temperatureString = json.getJSONObject("current_observation")
					.getString("temp_c");
			weather = json.getJSONObject("current_observation").getString(
					"weather");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		int temp = (Math.round((float) Double.parseDouble(temperatureString)));
		return new WeatherAPIItem(country, city, weather, temp);
	}

	static public WeatherAPIItem getHourWeather(String country, String city,
			int numDayFromToday, int hh) {
		// hh != 0
		JSONObject json = getJSONByLink("http://api.wunderground.com/api/"
				+ KEY + "/hourly10day/q/" + country + "/" + city + ".json");
		String temperatureString = "", weather = "";
		try {
			JSONArray array = json.getJSONArray("hourly_forecast");
			int index = hh
					+ numDayFromToday
					* 24
					- Integer
							.parseInt(array.getJSONObject(0).getJSONObject("FCTTIME").getString("hour"));

			temperatureString = array.getJSONObject(index)
					.getJSONObject("temp").getString("metric");

			weather = array.getJSONObject(index).getString("condition");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		int temp = (Math.round((float) Double.parseDouble(temperatureString)));
		return new WeatherAPIItem(country, city, weather, temp);
	}

	static JSONObject getJSONByLink(String link) {
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
