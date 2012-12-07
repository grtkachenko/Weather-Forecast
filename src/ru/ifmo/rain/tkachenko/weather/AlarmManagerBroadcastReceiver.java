package ru.ifmo.rain.tkachenko.weather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.Toast;

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {
	public static int updateTime = 1000 * 60 * 60 * 3;

	@Override
	public void onReceive(Context context, Intent intent) {
		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK, "");
		wl.acquire();

		doMyUpdate(context);

		wl.release();
	}

	public void SetAlarm(Context context) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, AlarmManagerBroadcastReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
				updateTime, pi); // Millisec * Second * Minute
	}

	public void CancelAlarm(Context context) {
		Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
		PendingIntent sender = PendingIntent
				.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}

	boolean f = true;

	private void doMyUpdate(Context context) {
		CityDbAdapter help = new CityDbAdapter(context);
		help.updateAllCities();
		// Toast.makeText(context, "Updated", Toast.LENGTH_LONG).show();
	}
}
