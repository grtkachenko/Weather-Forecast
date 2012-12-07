package ru.ifmo.rain.tkachenko.activities;

import java.util.ArrayList;
import java.util.TreeSet;

import ru.ifmo.rain.tkachenko.weather.AlarmManagerBroadcastReceiver;
import ru.ifmo.rain.tkachenko.weather.Cities;
import ru.ifmo.rain.tkachenko.weather.CityDbAdapter;
import ru.ifmo.rain.tkachenko.weather.R;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
	private ListView list;
	final int MENU_DELETE = 2;
	final int MENU_EDIT = 1;
	final int EDIT_DIALOG = 1;
	final int DELETE_ALL_ID = 3;
	private Dialog dialog;
	private TimePicker timePicker;
	private int pos = 0;
	private Cursor cityCursor;
	private CityDbAdapter mDbHelper;
	private Button addCity, updSetOK;
	private ArrayList<String> listItems = new ArrayList<String>();
	private ArrayAdapter<String> adapterList;
	private AutoCompleteTextView editText, addCityEditText;
	private TreeSet<String> haveCity = new TreeSet<String>();
	boolean usedHeight = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_settings);
		addCity = (Button) findViewById(R.id.addCity);
		updSetOK = (Button) findViewById(R.id.updSetOk);
		timePicker = (TimePicker) findViewById(R.id.timePicker);
		timePicker.setIs24HourView(true);
		timePicker
				.setCurrentHour(AlarmManagerBroadcastReceiver.updateTime / 1000 / 60 / 60);

		timePicker
				.setCurrentMinute((AlarmManagerBroadcastReceiver.updateTime - AlarmManagerBroadcastReceiver.updateTime
						/ 1000 / 60 / 60 * (1000 * 60 * 60)) / 1000 / 60);

		updSetOK.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				AlarmManagerBroadcastReceiver.updateTime = (timePicker
						.getCurrentHour() * 60 + timePicker.getCurrentMinute()) * 60 * 1000 + 500;
			}
		});

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		usedHeight = Math.max(size.x, size.y) < 800;
		if (usedHeight) {
			addCity.setTextSize(15);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		addCityEditText = (AutoCompleteTextView) findViewById(R.id.addCityEditText);
		addCity.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// onKeyDown(KeyEvent.KEYCODE_BACK, new KeyEvent());
				if (haveCity.size() == 0) {
					Toast.makeText(getApplicationContext(),
							"Please choose at least one city",
							Toast.LENGTH_LONG).show();
				} else {
					Intent returnIntent = new Intent();
					String[] data = new String[haveCity.size()];
					int num = 0;
					cityCursor = mDbHelper.fetchAllCity();
					if (cityCursor.moveToFirst()) {
						do {
							data[num++] = cityCursor.getString(cityCursor
									.getColumnIndex("city"));
						} while (cityCursor.moveToNext());
					}
					cityCursor.close();
					returnIntent.putExtra("data", data);
					setResult(1, returnIntent);
					finish();
				}
			}
		});
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, Cities.CITIES);
		addCityEditText.setAdapter(adapter);
		addCityEditText.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (!haveCity.contains(arg0.getItemAtPosition(arg2).toString())) {
					addCityEditText.setText(arg0.getItemAtPosition(arg2)
							.toString());
					String cur = arg0.getItemAtPosition(arg2).toString();
					clickAdd(addCity, true);
					haveCity.add(cur);
					addCityEditText.setText("");

				} else {
					addCityEditText.setText("");
				}

			}

		});

		list = (ListView) findViewById(R.id.listView);
		adapterList = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, listItems);
		list.setAdapter(adapterList);
		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				pos = position;
				openContextMenu(list);
				return true;
			}
		});
		registerForContextMenu(list);
		mDbHelper = new CityDbAdapter(this);
		mDbHelper.open();
		fillData();
	}

	private void fillData() {
		haveCity.clear();
		adapterList.clear();
		cityCursor = mDbHelper.fetchAllCity();
		if (cityCursor.moveToFirst()) {
			do {
				String data = cityCursor.getString(cityCursor
						.getColumnIndex("city"));
				if (haveCity.contains(data)) {
					continue;
				}
				addCityEditText.setText(data);
				clickAdd(addCity, false);
				haveCity.add(data);

			} while (cityCursor.moveToNext());
		}
		adapterList.notifyDataSetChanged();
		cityCursor.close();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.add(1, MENU_EDIT, 0, "Edit");
		menu.add(2, MENU_DELETE, 0, "Delete");
	}

	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == MENU_EDIT) {

			dialog = new Dialog(SettingsActivity.this);
			dialog.setTitle("Edit city");
			dialog.setContentView(R.layout.edit_dialog);
			dialog.setCancelable(true);
			editText = (AutoCompleteTextView) dialog
					.findViewById(R.id.dialog_edit_text);

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_dropdown_item_1line, Cities.CITIES);
			editText.setAdapter(adapter);
			editText.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					dialog.dismiss();
					if (!haveCity.contains(editText.getText().toString())) {

						mDbHelper.updateCity(adapterList.getItem(pos), editText
								.getText().toString(), "", "", "", "");

						adapterList.insert(editText.getText().toString(), pos);
						// top, right, bottom);
						fillData();
					}

				}

			});

			dialog.show();
		} else {
			mDbHelper.deleteCity(adapterList.getItem(pos));
			haveCity.remove(adapterList.getItem(pos));
			adapterList.remove(adapterList.getItem(pos));
			fillData();
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, DELETE_ALL_ID, 0, "Delete all cities");
		return true;
	}

	public void clickAdd(View v, boolean addToDb) {
		switch (v.getId()) {
		case R.id.addCity:

			TextView tv = new TextView(getApplicationContext());
			String cur = addCityEditText.getText().toString();
			if (haveCity.contains(cur)) {
				break;
			}
			//
			tv.setText(cur);
			// Typeface tf = Typeface.createFromAsset(getAssets(),
			// "fonts/oblique.ttf");
			// tv.setTypeface(tf);
			tv.setTextSize(30);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			lp.setMargins(0, 15, 0, 0); // llp.setMargins(left,
			tv.setLayoutParams(lp);
			tv.setTextColor(Color.BLACK);
			addCityEditText.setText("");
			if (addToDb) {
				mDbHelper.createCity(tv.getText().toString(), "",
				// "7 $ 1 2 3 4 5 1 2 3 4 5 1 2 3 4 5 1 2 3 4 5 1 2 3 4 5 1 2 3 4 5 1 2 3 4 5 1 2 3 4 5 1 2 3 4 5 1 2 3 4 5 1 2 3 4 5 1 2 3 4 5 1 2 3 4 5 1 2 3 4 5 1 2 3 4 5 1 2 3 4 5 1 2 3 4 5 1 2 3 4 5 1 2 3 4 5 1 2 3 4 5 $ 1/5",
						"", "", "");

			}
			adapterList.add(tv.getText().toString());

			registerForContextMenu(tv);
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_ALL_ID:
			cityCursor = mDbHelper.fetchAllCity();
			ArrayList<Long> deleteList = new ArrayList<Long>();
			if (cityCursor.moveToFirst()) {
				do {
					deleteList.add(cityCursor.getLong(cityCursor
							.getColumnIndex("_id")));
				} while (cityCursor.moveToNext());
			}
			cityCursor.close();
			for (Long i : deleteList) {
				mDbHelper.deleteCity(i);
			}
			fillData();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (haveCity.size() == 0) {
				Toast.makeText(getApplicationContext(),
						"Please choose at least one city", Toast.LENGTH_LONG)
						.show();
				return true;
			}
			Intent returnIntent = new Intent();
			String[] data = new String[haveCity.size()];
			int num = 0;
			cityCursor = mDbHelper.fetchAllCity();
			if (cityCursor.moveToFirst()) {
				do {
					data[num++] = cityCursor.getString(cityCursor
							.getColumnIndex("city"));
				} while (cityCursor.moveToNext());
			}
			cityCursor.close();
			returnIntent.putExtra("data", data);
			setResult(1, returnIntent);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
}
