package ru.ifmo.rain.tkachenko.weather_api_stuff;

public class WeatherAPIItem {
	public String country, city, weather, condition;
	public int from, to, now, hh;
	public WeatherAPIItem(String country, String city, String weather, int now) {
		this.country = country;
		this.city = city;
		this.weather = weather;
		this.now = now;
	}
	
	public WeatherAPIItem(String country, String city, String weather, int hh, int now, String condition) {
		this.country = country;
		this.condition = condition;
		this.city = city;
		this.weather = weather;
		this.hh = hh;
		this.now = now;
	}
	public String nowString() {
		return Integer.toString(now) + "°";
	}
	
}
