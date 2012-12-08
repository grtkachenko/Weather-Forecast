package ru.ifmo.rain.tkachenko.helpers;

import java.util.HashMap;

import ru.ifmo.rain.tkachenko.weather.R;

public class IconHelper {

	static HashMap<String, Integer> toIcon = new HashMap<String, Integer>();
	static {
		toIcon.put("chanceflurries", R.drawable.chanceflurries);
		toIcon.put("chancerain", R.drawable.chancerain);
		toIcon.put("chancesleet", R.drawable.chancesleet);
		toIcon.put("chancesnow", R.drawable.chancesnow);
		toIcon.put("chancetstorms", R.drawable.chancetstorms);
		toIcon.put("clear", R.drawable.clear);
		toIcon.put("cloudy", R.drawable.cloudy);
		toIcon.put("flurries", R.drawable.flurries);
		toIcon.put("fog", R.drawable.fog);
		toIcon.put("hazy", R.drawable.fog);
		toIcon.put("mostlycloudy", R.drawable.mostlycloudy);
		toIcon.put("mostlysunny", R.drawable.mostlysunny);
		toIcon.put("nt_chanceflurries", R.drawable.nt_chanceflurries);
		toIcon.put("nt_chancerain", R.drawable.nt_chancerain);
		toIcon.put("nt_chancesleet", R.drawable.nt_chancerain);
		toIcon.put("nt_chancesnow", R.drawable.nt_chanceflurries);
		toIcon.put("nt_chancetstorms", R.drawable.chancetstorms);
		toIcon.put("nt_clear", R.drawable.nt_clear);
		toIcon.put("nt_cloudy", R.drawable.nt_mostlycloudy);
		toIcon.put("nt_flurries", R.drawable.nt_chanceflurries);
		toIcon.put("nt_fog", R.drawable.fog);
		toIcon.put("nt_hazy", R.drawable.fog);
		toIcon.put("nt_mostlycloudy", R.drawable.nt_mostlycloudy);
		toIcon.put("nt_mostlysunny", R.drawable.nt_clear);
		toIcon.put("nt_partlycloudy", R.drawable.nt_partlycloudy);
		toIcon.put("nt_partlysunny", R.drawable.nt_clear);
		toIcon.put("nt_rain", R.drawable.nt_chancerain);
		toIcon.put("nt_sleet", R.drawable.nt_chancerain);
		toIcon.put("nt_snow", R.drawable.nt_chanceflurries);
		toIcon.put("nt_sunny", R.drawable.nt_clear);
		toIcon.put("nt_tstorms", R.drawable.tstorms);
		toIcon.put("partlycloudy", R.drawable.partlycloudy);
		toIcon.put("partlysunny", R.drawable.sunny);
		toIcon.put("rain", R.drawable.rain);
		toIcon.put("sleet", R.drawable.sleet);
		toIcon.put("snow", R.drawable.snow);
		toIcon.put("sunny", R.drawable.sunny);
		toIcon.put("tstorms", R.drawable.tstorms);

	}

	public static int getId(String s) {
		if (toIcon.containsKey(s))
			return toIcon.get(s);
		if (s.startsWith("nt_")) {
			s = s.substring(3);
			return getId(s);
		}
		return R.drawable.sunny;
	}
}
