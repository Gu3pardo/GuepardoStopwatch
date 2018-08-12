package guepardoapps.stopme.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Switch;

import guepardoapps.stopme.R;
import guepardoapps.stopme.common.Constants;
import guepardoapps.stopme.controller.SharedPreferenceController;
import guepardoapps.stopme.controller.SystemInfoController;
import guepardoapps.stopme.service.FloatingService;

public class ActivitySettings extends Activity {
    private Context _context;

    private SystemInfoController _androidSystemController;
    private SharedPreferenceController _sharedPrefController;

    private Class<FloatingService> _floatingService = FloatingService.class;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.side_settings);

        _context = this;
        _androidSystemController = new SystemInfoController(_context);
        _sharedPrefController = new SharedPreferenceController(_context);

        Switch bubbleStateSwitch = findViewById(R.id.switch_bubble_state);
        bubbleStateSwitch.setChecked((boolean) _sharedPrefController.load(Constants.bubbleState, false));
        bubbleStateSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            _sharedPrefController.save(Constants.bubbleState, isChecked);
            if (isChecked) {
                if (_androidSystemController.currentAndroidApi() >= android.os.Build.VERSION_CODES.M) {
                    _androidSystemController.checkAPI23SystemPermission(Constants.systemPermissionId);
                } else {
                    _context.startService(new Intent(_context, _floatingService));
                }
            } else {
                if (_androidSystemController.isServiceRunning(_floatingService)) {
                    _context.stopService(new Intent(_context, _floatingService));
                }
            }
        });
    }
}