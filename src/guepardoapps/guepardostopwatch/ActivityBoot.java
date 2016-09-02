package guepardoapps.guepardostopwatch;

import guepardoapps.common.*;
import guepardoapps.toolset.controller.*;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;

public class ActivityBoot extends Activity {

	private Context _context;
	
	private CheckController _checkController;
	private NavigationController _navigationController;
	private SharedPrefController _sharedPrefController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.side_boot);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Constants.ACTION_BAR_COLOR));

		_context = this;
		_checkController = new CheckController(_context);
		_navigationController = new NavigationController(_context);
		_sharedPrefController = new SharedPrefController(_context, Constants.SHARED_PREF_NAME);

		if (!_sharedPrefController.LoadBooleanValueFromSharedPreferences(Constants.SHARED_PREF_NAME)) {
			_sharedPrefController.SaveBooleanValue(Constants.BUBBLE_STATE, true);
			_sharedPrefController.SaveIntegerValue(Constants.BUBBLE_POS_Y, Constants.BUBBLE_DEFAULT_POS_Y);
			_sharedPrefController.SaveBooleanValue(Constants.SHARED_PREF_NAME, true);
		}
	}

	protected void onResume() {
		super.onResume();
		if (_checkController.CurrentAndroidApi() >= android.os.Build.VERSION_CODES.M) {
			Log.d("stopwatch", "asking for permission");
			if (_checkController.CheckAPI23SystemPermission(Constants.PERMISSION_REQUEST_CODE)) {
				_navigationController.NavigateTo(ActivityMain.class, true);
			}
		} else {
			_navigationController.NavigateTo(ActivityMain.class, true);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}