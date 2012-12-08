package ru.ifmo.rain.tkachenko.alarm;

import ru.ifmo.rain.tkachenko.providers.CityDbHelper;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {
	private static int updateTime = 1000 * 60 * 60 * 3;
	private Context context;
	private boolean isCreated = false;

	@Override
	public void onReceive(Context context, Intent intent) {
		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK, "");
		wl.acquire();
		doMyUpdate(context);
		// Toast.makeText(context, "OK", Toast.LENGTH_LONG).show();
		wl.release();
	}

	public void setAlarm(Context context) {
		if (isCreated) {
			return;
		}
		isCreated = true;
		this.context = context;
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, AlarmManagerBroadcastReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
				updateTime, pi); // Millisec * Second * Minute
	}

	public void setAlarmTrue(Context context) {
		this.context = context;
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, AlarmManagerBroadcastReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
				updateTime, pi); // Millisec * Second * Minute
	}

	public void cancelAlarm(Context context) {
		Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
		PendingIntent sender = PendingIntent
				.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}

	public void setUpdateTimeAndStart(int time) {
		AlarmManagerBroadcastReceiver.updateTime = time;
		cancelAlarm(context);
		setAlarmTrue(context);
	}

	public void setUpdateTime(int time) {
		AlarmManagerBroadcastReceiver.updateTime = time;
	}

	public int getUpdateTime() {
		return updateTime;
	}

	boolean f = true;

	private void doMyUpdate(Context context) {
		CityDbHelper help = new CityDbHelper(context);
		help.updateAllCities();
	}
}
