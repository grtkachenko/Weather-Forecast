package ru.ifmo.rain.tkachenko.helpers;

import ru.ifmo.rain.tkachenko.activities.MainActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	MainActivity mainActivity;
	public static int last = 0;

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
				last = 0;
				if (mainActivity.isCalced) {
					mainActivity.toNextCity();
				}

			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				// Right
				last = 1;
				if (mainActivity.isCalced) {
					mainActivity.toPrevCity();
				}
			}
		} catch (Exception e) {
			// nothing
		}
		return false;
	}

}