package guepardoapps.guepardostopwatch.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import guepardoapps.guepardostopwatch.R;
import guepardoapps.guepardostopwatch.common.*;

import guepardoapps.library.toolset.common.Logger;
import guepardoapps.library.toolset.controller.AndroidSystemController;
import guepardoapps.library.toolset.controller.NavigationController;
import guepardoapps.library.toolset.controller.SharedPrefController;

public class ActivityBoot extends Activity {

	private static final String TAG = ActivityBoot.class.getSimpleName();
	private Logger _logger;

	private Context _context;

	private AndroidSystemController _androidSystemController;
	private NavigationController _navigationController;
	private SharedPrefController _sharedPrefController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.side_boot);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Colors.ACTION_BAR_COLOR));

		_logger = new Logger(TAG, Enables.DEBUGGING);

		_context = this;

		_androidSystemController = new AndroidSystemController(_context);
		_navigationController = new NavigationController(_context);
		_sharedPrefController = new SharedPrefController(_context, SharedPrefConstants.SHARED_PREF_NAME);

		if (!_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.SHARED_PREF_NAME)) {
			_sharedPrefController.SaveBooleanValue(SharedPrefConstants.BUBBLE_STATE, true);
			_sharedPrefController.SaveIntegerValue(SharedPrefConstants.BUBBLE_POS_Y,
					SharedPrefConstants.BUBBLE_DEFAULT_POS_Y);
			_sharedPrefController.SaveBooleanValue(SharedPrefConstants.SHARED_PREF_NAME, true);
		}
	}

	protected void onResume() {
		super.onResume();
		if (_androidSystemController.CurrentAndroidApi() >= android.os.Build.VERSION_CODES.M) {
			_logger.Debug("asking for permission");
			if (_androidSystemController.CheckAPI23SystemPermission(PermissionCodes.SYSTEM_PERMISSION)) {
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