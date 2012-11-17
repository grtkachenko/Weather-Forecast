package ru.ifmo.rain.tkachenko.weather;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	MainActivity mainActivity;

	public MyGestureDetector(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		try {
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
				return false;
			// right to left swipe
			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				// Left

				Animation translate = AnimationUtils.loadAnimation(
						mainActivity, R.anim.falling_from_right);
				Animation translateDelay1 = AnimationUtils.loadAnimation(
						mainActivity, R.anim.falling_from_right);
				Animation translateDelay2 = AnimationUtils.loadAnimation(
						mainActivity, R.anim.falling_from_right);
				translateDelay1.setStartOffset(200);
				translateDelay2.setStartOffset(400);
				
				
				mainActivity.city.startAnimation(translate);
				mainActivity.temeratureNow.startAnimation(translate);
				mainActivity.weatherNow.startAnimation(translate);
				mainActivity.table.startAnimation(translateDelay1);
				mainActivity.fourDaysLayout.startAnimation(translateDelay2);
				
				mainActivity.city.setText("Saint-Petersburg");
				mainActivity.city.setTextSize(mainActivity
						.getCitySize("Saint-Petersburg".length()));

			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				// Right
				Animation translate = AnimationUtils.loadAnimation(
						mainActivity, R.anim.falling_from_left);
				Animation translateDelay1 = AnimationUtils.loadAnimation(
						mainActivity, R.anim.falling_from_left);
				Animation translateDelay2 = AnimationUtils.loadAnimation(
						mainActivity, R.anim.falling_from_left);
				translateDelay1.setStartOffset(200);
				translateDelay2.setStartOffset(400);
				
				
				mainActivity.city.startAnimation(translate);
				mainActivity.temeratureNow.startAnimation(translate);
				mainActivity.weatherNow.startAnimation(translate);
				mainActivity.table.startAnimation(translateDelay1);
				mainActivity.fourDaysLayout.startAnimation(translateDelay2);
				
				mainActivity.city.setText("Chelyabinsk");
				mainActivity.city.setTextSize(mainActivity
						.getCitySize("Chelyabinsk".length()));
				;
			}
		} catch (Exception e) {
			// nothing
		}
		return false;
	}

}