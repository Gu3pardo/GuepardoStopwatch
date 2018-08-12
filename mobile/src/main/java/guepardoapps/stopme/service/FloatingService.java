package guepardoapps.stopme.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.rey.material.widget.Button;
import com.rey.material.widget.FloatingActionButton;

import java.util.Locale;

import guepardoapps.stopme.R;
import guepardoapps.stopme.common.*;
import guepardoapps.stopme.controller.SharedPreferenceController;

public class FloatingService extends Service {

    private boolean _isRunning;
    int _round;


    private long _roundStartTime = 0L;
    private int _roundSeconds, _roundMinutes, _roundMilliSeconds;

    private long _startTimeFinal = 0L;
    private int _finalSeconds, _finalMinutes, _finalMilliSeconds;

    private Runnable _updateTimerMethod = new Runnable() {

        public void run() {
            long roundTimeInMillis = SystemClock.uptimeMillis() - _roundStartTime;

            _roundSeconds = (int) (roundTimeInMillis / 1000);
            _roundMinutes = _roundSeconds / 60;
            _roundSeconds = _roundSeconds % 60;
            _roundMilliSeconds = (int) (roundTimeInMillis % 1000);

            long timeInMillisFinal = SystemClock.uptimeMillis() - _startTimeFinal;

            _finalSeconds = (int) (timeInMillisFinal / 1000);
            _finalMinutes = _finalSeconds / 60;
            _finalSeconds = _finalSeconds % 60;
            _finalMilliSeconds = (int) (timeInMillisFinal % 1000);

            _minuteView.setText(String.format(Locale.GERMAN, "%02d", _finalMinutes));
            _secondsView.setText(String.format(Locale.GERMAN, "%02d", _finalSeconds));
            _milliSecondsView.setText(String.format(Locale.GERMAN, "%03d", _finalMilliSeconds));

            _stopwatchHandler.postDelayed(this, 0);
        }
    };
}