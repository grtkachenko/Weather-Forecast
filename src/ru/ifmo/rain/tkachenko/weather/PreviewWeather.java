package ru.ifmo.rain.tkachenko.weather;

import android.content.Context;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PreviewWeather extends LinearLayout {
	private Context context;
	private LinearLayout top;
	private ImageView tick, tickTmp;
	public TextView day;

	public PreviewWeather(Context context) {
		super(context);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);

		this.setLayoutParams(lp);
		this.context = context;
		this.setOrientation(VERTICAL);
		this.setGravity(Gravity.CENTER);
		initViews();
	}

	private void initViews() {
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		top = new LinearLayout(context);
		top.setLayoutParams(lp);
		top.setOrientation(HORIZONTAL);
		top.setGravity(Gravity.CENTER);
		tick = new ImageView(context);
		tickTmp = new ImageView(context);
		day = new TextView(context);
		tick.setImageResource(R.drawable.tick);
		LayoutParams lpCur = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, 1);
		tick.setLayoutParams(lpCur);
		tickTmp.setLayoutParams(lpCur);
		day.setLayoutParams(lpCur);
		day.setGravity(Gravity.CENTER);
		

	}

}
