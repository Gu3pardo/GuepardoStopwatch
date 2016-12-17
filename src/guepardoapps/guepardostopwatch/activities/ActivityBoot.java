package guepardoapps.guepardostopwatch.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import guepardoapps.guepardostopwatch.R;
import guepardoapps.guepardostopwatch.common.*;

import guepardoapps.toolset.common.Logger;
import guepardoapps.toolset.controller.SharedPrefController;
import guepardoapps.toolset.services.AndroidSystemService;
import guepardoapps.toolset.services.NavigationService;

public class ActivityBoot extends Activity {

	private static final String TAG = ActivityBoot.class.getName();
	private Logger _logger;

	private Context _context;

	private AndroidSystemService _androidSystemService;
	private NavigationService _navigationService;
	private SharedPrefController _sharedPrefController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.side_boot);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Constants.ACTION_BAR_COLOR));

		_logger = new Logger(TAG, Constants.DEBUGGING_ENABLED);

		_context = this;

		_androidSystemService = new AndroidSystemService(_context);
		_navigationService = new NavigationService(_context);
		_sharedPrefController = new SharedPrefController(_context, Constants.SHARED_PREF_NAME);

		if (!_sharedPrefController.LoadBooleanValueFromSharedPreferences(Constants.SHARED_PREF_NAME)) {
			_sharedPrefController.SaveBooleanValue(Constants.BUBBLE_STATE, true);
			_sharedPrefController.SaveIntegerValue(Constants.BUBBLE_POS_Y, Constants.BUBBLE_DEFAULT_POS_Y);
			_sharedPrefController.SaveBooleanValue(Constants.SHARED_PREF_NAME, true);
		}
	}

	protected void onResume() {
		super.onResume();
		if (_androidSystemService.CurrentAndroidApi() >= android.os.Build.VERSION_CODES.M) {
			_logger.Debug("asking for permission");
			if (_androidSystemService.CheckAPI23SystemPermission(Constants.PERMISSION_REQUEST_CODE)) {
				_navigationService.NavigateTo(ActivityMain.class, true);
			}
		} else {
			_navigationService.NavigateTo(ActivityMain.class, true);
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