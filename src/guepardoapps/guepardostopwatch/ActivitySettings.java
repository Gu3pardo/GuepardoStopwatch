package guepardoapps.guepardostopwatch;

import guepardoapps.common.Constants;
import guepardoapps.toolset.controller.*;
import guepardoapps.service.FloatingService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ActivitySettings extends Activity {

	private Context _context;

	private CheckController _checkController;
	private SharedPrefController _sharedPrefController;
	
	private Class<FloatingService> _floatingService = FloatingService.class;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.side_settings);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Constants.ACTION_BAR_COLOR));

		_context = this;
		_checkController = new CheckController(_context);
		_sharedPrefController = new SharedPrefController(_context, Constants.SHARED_PREF_NAME);

		Switch bubbleStateSwitch = (Switch) findViewById(R.id.switch_bubble_state);
		bubbleStateSwitch.setChecked(_sharedPrefController.LoadBooleanValueFromSharedPreferences(Constants.BUBBLE_STATE));
		bubbleStateSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				_sharedPrefController.SaveBooleanValue(Constants.BUBBLE_STATE, isChecked);
				if (isChecked) {
					if (_checkController.CurrentAndroidApi() >= android.os.Build.VERSION_CODES.M) {
						_checkController.CheckAPI23SystemPermission(Constants.PERMISSION_REQUEST_CODE);
					} else {
						_context.startService(new Intent(_context, _floatingService));
					}
				} else {
					if (_checkController.IsServiceRunning(_floatingService)) {
						_context.stopService(new Intent(_context, _floatingService));
					}
				}
			}
		});
	}
}