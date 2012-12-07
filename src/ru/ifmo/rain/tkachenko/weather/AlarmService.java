package ru.ifmo.rain.tkachenko.weather;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class AlarmService extends Service {
	AlarmManagerBroadcastReceiver alarm = new AlarmManagerBroadcastReceiver();

	public void onCreate() {
		super.onCreate();
	}

	public void onStart(Context context, Intent intent, int startId) {
		alarm.SetAlarm(context);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}