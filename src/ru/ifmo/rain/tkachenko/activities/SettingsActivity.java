package ru.ifmo.rain.tkachenko.activities;

import java.util.ArrayList;
import java.util.TreeSet;

import ru.ifmo.rain.tkachenko.helpers.Cities;
import ru.ifmo.rain.tkachenko.providers.CityDbHelper;
import ru.ifmo.rain.tkachenko.weather.R;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

public class SettingsActivity extends Activity {
	/** Called when the activity is first created. */
	final int MENU_DELETE = 2;
	final int MENU_EDIT = 1;
	final int EDIT_DIALOG = 1;
	private Dialog dialog;
	private TabHost tabs;
	private CityDbHelper mDbHelper;
	private Button addCity, updSetOK, clear;
	private TimePicker timePicker;
	private AutoCompleteTextView editText, addCityEditText;
	private TreeSet<String> haveCity = new TreeSet<String>();
	private Cursor cityCursor;
	private TextView empty = null;
	private ArrayList<String> listItems = new ArrayList<String>();
	private ListView list;
	private ArrayAdapter<String> adapterList;
	private int pos = 0;
	private Context myContext;
	private SettingsActivity settingsActivity;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_settings);
		myContext = this;
		settingsActivity = this;
		empty = new TextView(this);
		empty.setText("There is no cities");
		tabs = (TabHost) findViewById(R.id.TabHost01);

		tabs.setup();

		TabHost.TabSpec citiesTab = tabs.newTabSpec("tag1");

		citiesTab.setContent(R.id.citySettingsLayout);
		citiesTab.setIndicator("Cities");

		tabs.addTab(citiesTab);

		TabHost.TabSpec timeUpdatingTab = tabs.newTabSpec("tag2");
		timeUpdatingTab.setContent(R.id.timeSettingsLayout);
		timeUpdatingTab.setIndicator("Update intevral");

		tabs.addTab(timeUpdatingTab);
		View tmp = tabs.getTabContentView();
		tuneCitySettings(tmp);
		tabs.setOnTabChangedListener(new OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				int i = tabs.getCurrentTab();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(addCityEditText.getWindowToken(), 0);
				if (i == 0) {
					// city tab
					tuneCitySettings(tabs.getTabContentView());

				} else {
					// time upd tab
					tuneTimeSettings(tabs.getTabContentView());
				}

			}
		});
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

	private void tuneCitySettings(View tmp) {
		clear = (Button) tmp.findViewById(R.id.clearData);
		
		addCity = (Button) tmp.findViewById(R.id.addCity);
		addCityEditText = (AutoCompleteTextView) tmp
				.findViewById(R.id.addCityEditText);
		addCity.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// onKeyDown(KeyEvent.KEYCODE_BACK, new KeyEvent());
				if (haveCity.size() == 0) {
					Toast.makeText(myContext,
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
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(myContext,
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

		list = (ListView) tmp.findViewById(R.id.listView);
		adapterList = new ArrayAdapter<String>(myContext,
				android.R.layout.simple_list_item_1, listItems);
		list.setAdapter(adapterList);

		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				pos = position;
				settingsActivity.openContextMenu(list);
				return true;
			}
		});

		settingsActivity.registerForContextMenu(list);
		mDbHelper = new CityDbHelper(myContext);
		clear.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
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
			}
		});
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

	public void clickAdd(View v, boolean addToDb) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(addCityEditText.getWindowToken(), 0);
		TextView tv = new TextView(myContext);
		String cur = addCityEditText.getText().toString();
		if (haveCity.contains(cur)) {
			return;
		}
		tv.setText(cur);
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

		settingsActivity.registerForContextMenu(tv);
		adapterList.notifyDataSetChanged();
	}

	private void tuneTimeSettings(View tmp) {
		updSetOK = (Button) tmp.findViewById(R.id.updSetOk);

		updSetOK.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				MainActivity.alarm.setUpdateTimeAndStart((timePicker.getCurrentHour() * 60 + timePicker
						.getCurrentMinute()) * 60 * 1000);
				updSetOK.setBackgroundResource(R.drawable.button_green_border);
			}
		});

		timePicker = (TimePicker) tmp.findViewById(R.id.timePicker);
		timePicker.setIs24HourView(true);

		timePicker
				.setCurrentHour(MainActivity.alarm.getUpdateTime() / 1000 / 60 / 60);

		timePicker
				.setCurrentMinute((MainActivity.alarm.getUpdateTime() - MainActivity.alarm.getUpdateTime()
						/ 1000 / 60 / 60 * (1000 * 60 * 60)) / 1000 / 60);
		timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				if (hourOfDay + minute == 0) {
					timePicker.setCurrentHour(0);
					timePicker.setCurrentMinute(1);
					onTimeChanged(view, 0, 1);
					return;
				}
				if ((hourOfDay * 60 + minute) * 60 * 1000 == MainActivity.alarm.getUpdateTime()) {
					updSetOK.setBackgroundResource(R.drawable.button_green_border);
				} else {
					updSetOK.setBackgroundResource(R.drawable.button_red_border);
				}
			}
		});

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