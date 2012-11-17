package ru.ifmo.rain.tkachenko.weather;

public class WeatherAPIItem {
	String country, city, weather;
	int from, to, now;
	public WeatherAPIItem(String country, String city, String weather, int now) {
		this.country = country;
		this.city = city;
		this.weather = weather;
		this.now = now;
	}
	String nowString() {
		return Integer.toString(now) + "°";
	}
	
}
