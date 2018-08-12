package guepardoapps.stopme.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import guepardoapps.stopme.R;
import guepardoapps.stopme.common.Constants;
import guepardoapps.stopme.controller.NavigationController;
import guepardoapps.stopme.controller.SharedPreferenceController;
import guepardoapps.stopme.controller.SystemInfoController;

public class ActivityBoot extends Activity {
    private SystemInfoController _androidSystemController;
    private NavigationController _navigationController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.side_boot);

        _androidSystemController = new SystemInfoController(this);
        _navigationController = new NavigationController(this);

        SharedPreferenceController sharedPrefController = new SharedPreferenceController(this);
        if (!(boolean) sharedPrefController.load(Constants.sharedPrefName, false)) {
            sharedPrefController.save(Constants.bubbleState, true);
            sharedPrefController.save(Constants.bubblePosY, Constants.bubbleDefaultPosY);
            sharedPrefController.save(Constants.sharedPrefName, true);
        }
    }

    protected void onResume() {
        super.onResume();
        if (_androidSystemController.currentAndroidApi() >= android.os.Build.VERSION_CODES.M) {
            if (_androidSystemController.checkAPI23SystemPermission(Constants.systemPermissionId)) {
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
        new Handler().postDelayed(() -> _navigationController.NavigateTo(ActivityMain.class, true), 1500);
    }
}