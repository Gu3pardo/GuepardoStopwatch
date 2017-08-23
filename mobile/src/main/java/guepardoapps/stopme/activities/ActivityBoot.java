package guepardoapps.stopme.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import guepardoapps.stopme.R;
import guepardoapps.stopme.common.*;
import guepardoapps.stopme.controller.AndroidSystemController;
import guepardoapps.stopme.controller.NavigationController;
import guepardoapps.stopme.controller.SharedPrefController;
import guepardoapps.stopme.tools.Logger;

public class ActivityBoot extends Activity {
    private static final String TAG = ActivityBoot.class.getSimpleName();
    private Logger _logger;

    private AndroidSystemController _androidSystemController;
    private NavigationController _navigationController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.side_boot);

        _logger = new Logger(TAG, Enables.LOGGING);

        _androidSystemController = new AndroidSystemController(this);
        _navigationController = new NavigationController(this);

        SharedPrefController sharedPrefController = new SharedPrefController(this, SharedPrefConstants.SHARED_PREF_NAME);
        if (!sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.SHARED_PREF_NAME)) {
            sharedPrefController.SaveBooleanValue(SharedPrefConstants.BUBBLE_STATE, true);
            sharedPrefController.SaveIntegerValue(SharedPrefConstants.BUBBLE_POS_Y,
                    SharedPrefConstants.BUBBLE_DEFAULT_POS_Y);
            sharedPrefController.SaveBooleanValue(SharedPrefConstants.SHARED_PREF_NAME, true);
        }
    }

    protected void onResume() {
        super.onResume();
        if (_androidSystemController.CurrentAndroidApi() >= android.os.Build.VERSION_CODES.M) {
            _logger.Debug("asking for permission");
            if (_androidSystemController.CheckAPI23SystemPermission(PermissionCodes.SYSTEM_PERMISSION)) {
                navigateToMain();
            }
        } else {
            navigateToMain();
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

    private void navigateToMain() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                _navigationController.NavigateTo(ActivityMain.class, true);
            }
        }, 1500);
    }
}