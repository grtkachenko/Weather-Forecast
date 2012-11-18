package ru.ifmo.rain.tkachenko.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import ru.ifmo.rain.tkachenko.weather.Cities;
import ru.ifmo.rain.tkachenko.weather.CityDbAdapter;
import ru.ifmo.rain.tkachenko.weather.R;
import ru.ifmo.rain.tkachenko.weather.R.id;
import ru.ifmo.rain.tkachenko.weather.R.layout;
import ru.ifmo.rain.tkachenko.weather.R.menu;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsActivity extends Activity implements OnClickListener {
	private LinearLayout list;
	final int MENU_DELETE = 2;
	final int MENU_EDIT = 1;
	final int EDIT_DIALOG = 1;
	final int DELETE_ALL_ID = 3;
	private Dialog dialog;
	private Cursor cityCursor;
	private CityDbAdapter mDbHelper;
	private View tmpView = null;
	private Button addCity;
	private AutoCompleteTextView editText, addCityEditText;
	private HashMap<String, Boolean> haveCity = new HashMap<String, Boolean>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_settings);

		addCity = (Button) findViewById(R.id.addCity);
		addCityEditText = (AutoCompleteTextView) findViewById(R.id.addCityEditText);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, Cities.CITIES);
		addCityEditText.setAdapter(adapter);
		addCityEditText.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (!haveCity.containsKey(arg0.getItemAtPosition(arg2)
						.toString())) {
					addCityEditText.setText(arg0.getItemAtPosition(arg2)
							.toString());
					String cur = arg0.getItemAtPosition(arg2).toString();
					onClick(addCity);
					haveCity.put(cur, true);
					addCityEditText.setText("");

				} else {
					addCityEditText.setText("");
				}

			}

		});

		addCity.setOnClickListener(this);
		list = (LinearLayout) findViewById(R.id.city_list);
		mDbHelper = new CityDbAdapter(this);
		mDbHelper.open();
		fillData();
	}

	private void fillData() {
		haveCity.clear();
		list.removeAllViews();
		cityCursor = mDbHelper.fetchAllCity();
		if (cityCursor.moveToFirst()) {
			do {
				String data = cityCursor.getString(cityCursor
						.getColumnIndex("city"));
				if (haveCity.containsKey(data)) {
					continue;
				}
				addCityEditText.setText(data);
				onClick(addCity);
				haveCity.put(data, true);

			} while (cityCursor.moveToNext());
		}
		cityCursor.close();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		menu.add(1, MENU_EDIT, 0, "Edit");
		menu.add(2, MENU_DELETE, 0, "Delete");
		tmpView = v;
	}

	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if (item.getItemId() == MENU_EDIT) {

			dialog = new Dialog(SettingsActivity.this);
			dialog.setTitle("Edit city");
			dialog.setContentView(R.layout.edit_dialog);
			dialog.setCancelable(true);
			editText = (AutoCompleteTextView) dialog
					.findViewById(R.id.dialog_edit_text);
			editText.setText(((TextView) tmpView).getText());

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_dropdown_item_1line, Cities.CITIES);
			editText.setAdapter(adapter);
			editText.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					editText.setText(arg0.getItemAtPosition(arg2).toString());
					dialog.dismiss();
					((TextView) tmpView).setText(editText.getText());
				}

			});

			dialog.show();
		} else {
			mDbHelper.deleteCity(((TextView) tmpView).getText().toString());
			list.removeView(tmpView);
			fillData();
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, DELETE_ALL_ID, 0, "Delete all cities");
		return true;
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.addCity:

			TextView tv = new TextView(getApplicationContext());
			String cur = addCityEditText.getText().toString();
			if (haveCity.containsKey(cur)) {
				break;
			}
			tv.setText(cur);
			Typeface tf = Typeface.createFromAsset(getAssets(),
					"fonts/oblique.ttf");
			// tv.setTypeface(tf);
			tv.setTextSize(30);
			tv.setTextColor(Color.BLACK);
			addCityEditText.setText("");
			mDbHelper.createCity(tv.getText().toString());
			list.addView(tv);

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
}
