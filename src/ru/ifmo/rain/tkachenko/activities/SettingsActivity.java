package ru.ifmo.rain.tkachenko.activities;

import java.util.ArrayList;
import java.util.TreeSet;

import ru.ifmo.rain.tkachenko.weather.Cities;
import ru.ifmo.rain.tkachenko.weather.CityDbAdapter;
import ru.ifmo.rain.tkachenko.weather.R;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
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
	private TreeSet<String> haveCity = new TreeSet<String>();

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
				if (haveCity.contains(data)) {
					continue;
				}
				addCityEditText.setText(data);
				clickAdd(addCity, false);
				haveCity.add(data);

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

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_dropdown_item_1line, Cities.CITIES);
			editText.setAdapter(adapter);
			editText.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					dialog.dismiss();
					if (!haveCity.contains(editText.getText().toString())) {
						mDbHelper.updateCity(((TextView) tmpView).getText()
								.toString(), editText.getText().toString());
						((TextView) tmpView).setText(editText.getText());
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
								LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT);
						lp.setMargins(0, 15, 0, 0); // llp.setMargins(left,
						((TextView) tmpView).setLayoutParams(lp);							// top, right, bottom);
						fillData();
					}

				}

			});

			dialog.show();
		} else {
			mDbHelper.deleteCity(((TextView) tmpView).getText().toString());
			haveCity.remove(((TextView) tmpView).getText().toString());
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

	public void clickAdd(View v, boolean addToDb) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.addCity:

			TextView tv = new TextView(getApplicationContext());
			String cur = addCityEditText.getText().toString();
			if (haveCity.contains(cur)) {
				break;
			}
			tv.setText(cur);
			Typeface tf = Typeface.createFromAsset(getAssets(),
					"fonts/oblique.ttf");
			tv.setTypeface(tf);
			tv.setTextSize(30);
			tv.setTextColor(Color.BLACK);
			addCityEditText.setText("");
			if (addToDb) {
				mDbHelper.createCity(tv.getText().toString());
			}
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
