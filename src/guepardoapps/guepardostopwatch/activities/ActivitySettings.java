package guepardoapps.guepardostopwatch.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;

import guepardoapps.guepardostopwatch.R;
import guepardoapps.guepardostopwatch.common.*;
import guepardoapps.guepardostopwatch.service.FloatingService;

import guepardoapps.toolset.controller.AndroidSystemController;
import guepardoapps.toolset.controller.SharedPrefController;

public class ActivitySettings extends Activity {

	private Context _context;

	private AndroidSystemController _androidSystemController;
	private SharedPrefController _sharedPrefController;

	private Class<FloatingService> _floatingService = FloatingService.class;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.side_settings);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Colors.ACTION_BAR_COLOR));

		_context = this;
		_androidSystemController = new AndroidSystemController(_context);
		_sharedPrefController = new SharedPrefController(_context, SharedPrefConstants.SHARED_PREF_NAME);

		Switch bubbleStateSwitch = (Switch) findViewById(R.id.switch_bubble_state);
		bubbleStateSwitch.setChecked(
				_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.BUBBLE_STATE));
		bubbleStateSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				_sharedPrefController.SaveBooleanValue(SharedPrefConstants.BUBBLE_STATE, isChecked);
				if (isChecked) {
					if (_androidSystemController.CurrentAndroidApi() >= android.os.Build.VERSION_CODES.M) {
						_androidSystemController.CheckAPI23SystemPermission(PermissionCodes.SYSTEM_PERMISSION);
					} else {
						_context.startService(new Intent(_context, _floatingService));
					}
				} else {
					if (_androidSystemController.isServiceRunning(_floatingService)) {
						_context.stopService(new Intent(_context, _floatingService));
					}
				}
			}
		});
	}
}