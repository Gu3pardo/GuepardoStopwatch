package guepardoapps.stopme.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.rey.material.widget.Button;
import com.rey.material.widget.FloatingActionButton;

import java.util.Locale;

import es.dmoral.toasty.Toasty;
import guepardoapps.stopme.R;
import guepardoapps.stopme.common.Constants;
import guepardoapps.stopme.controller.SharedPreferenceController;
import guepardoapps.stopme.controller.SystemInfoController;
import guepardoapps.stopme.service.FloatingService;

public class ActivityMain extends Activity {
    private Context _context;

    private SystemInfoController _androidSystemController;
    private MailController _mailController;
    private NavigationController _navigationController;
    private SharedPreferenceController _sharedPrefController;

    private Class<FloatingService> _floatingService;

    private TextView _minuteView;
    private TextView _secondsView;
    private TextView _milliSecondsView;

    private ScrollView _scrollView;

    private boolean _isRunning;
    private int _round;
    private Handler _stopwatchHandler;

    private long _roundStartTime = 0L;
    private int _roundSeconds;
    private int _roundMinutes;
    private int _roundMilliSeconds;

    private long _startTimeFinal = 0L;
    private int _finalSeconds;
    private int _finalMinutes;
    private int _finalMilliSeconds;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.side_main);

        _context = this;

        _androidSystemController = new SystemInfoController(_context);
        _mailController = new MailController(_context);
        _navigationController = new NavigationController(_context);
        _sharedPrefController = new SharedPreferenceController(_context);

        _floatingService = FloatingService.class;

        tryToStopService();

        _stopwatchHandler = new Handler();
        _round = 1;
        _isRunning = false;

        _minuteView = findViewById(R.id.textTimerMin);
        _secondsView = findViewById(R.id.textTimerSec);
        _milliSecondsView = findViewById(R.id.textTimerMsec);

        _scrollView = findViewById(R.id.scrollView);

        final Button btnExport = findViewById(R.id.timeValue);
        btnExport.setOnClickListener(view -> {
            String times = btnExport.getText().toString();
            _mailController.SendMail("Times", times, false);
        });

        Button btnClear = findViewById(R.id.clearButton);
        btnClear.setOnClickListener(view -> {
            if (!_isRunning) {
                btnExport.setText("");
            }
        });

        Button btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(view -> {
            if (!_isRunning) {
                _roundStartTime = SystemClock.uptimeMillis();
                _startTimeFinal = SystemClock.uptimeMillis();

                _stopwatchHandler.postDelayed(_updateTimerMethod, 0);

                _isRunning = true;
            }
        });

        Button btnPause = findViewById(R.id.btnPause);
        btnPause.setOnClickListener(view -> {
            if (_isRunning) {
                String buttonExportText = btnExport.getText().toString();
                buttonExportText = String.format(Locale.GERMAN, "%sRound %d = %d:%02d:%03d\n",
                        buttonExportText, _round, _roundMinutes, _roundSeconds, _roundMilliSeconds);

                btnExport.setText(buttonExportText);
                _scrollView.fullScroll(View.FOCUS_DOWN);

                _round++;
                _roundStartTime = SystemClock.uptimeMillis();
            }
        });

        Button btnStop = findViewById(R.id.btnStop);
        btnStop.setOnClickListener(view -> {
            if (_isRunning) {
                _roundStartTime = 0L;
                _startTimeFinal = 0L;

                _stopwatchHandler.removeCallbacks(_updateTimerMethod);

                String buttonExportText = btnExport.getText().toString();
                buttonExportText = String.format(Locale.GERMAN, "%sRound %d = %d:%02d:%03d\n",
                        buttonExportText, _round, _roundMinutes, _roundSeconds, _roundMilliSeconds);

                btnExport.setText(buttonExportText);

                String minute = _minuteView.getText().toString();
                String second = _secondsView.getText().toString();
                String milli = _milliSecondsView.getText().toString();

                btnExport.setText(String.format("%s\nTime = %s:%s:%s\n ________________________ \n\n", btnExport.getText().toString(), minute, second, milli));

                _scrollView.fullScroll(View.FOCUS_DOWN);

                _roundStartTime = 0L;
                _startTimeFinal = 0L;

                _minuteView.setText(R.string.dummyTime);
                _secondsView.setText(R.string.dummyTime);
                _milliSecondsView.setText(R.string.dummyTime);

                _finalSeconds = 0;
                _finalMinutes = 0;
                _finalMilliSeconds = 0;

                _roundSeconds = 0;
                _roundMinutes = 0;
                _roundMilliSeconds = 0;

                _isRunning = false;
                _round = 1;
            }
        });

        FloatingActionButton btnAbout = findViewById(R.id.btnImpressum);
        btnAbout.setOnClickListener(view -> {
            if (!_isRunning) {
                _navigationController.NavigateTo(ActivityAbout.class, false);
            } else {
                Toasty.warning(_context, "Stopwatch is running!", Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(view -> {
            if (!_isRunning) {
                _navigationController.NavigateTo(ActivitySettings.class, false);
            } else {
                Toasty.warning(_context, "Stopwatch is running!", Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(view -> {
            if (!_isRunning) {
                finish();
            } else {
                Toasty.warning(_context, "Stopwatch is running!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        tryToStopService();
    }

    @Override
    public void onPause() {
        super.onPause();
        tryToStartService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tryToStartService();
        if (_isRunning) {
            _stopwatchHandler.removeCallbacks(_updateTimerMethod);
            _isRunning = false;
        }
    }

    private void tryToStartService() {
        if (!_androidSystemController.isServiceRunning(_floatingService)
                && (boolean) _sharedPrefController.load(Constants.bubbleState, false)) {
            startService(new Intent(_context, _floatingService));
        }
    }

    private void tryToStopService() {
        if (_androidSystemController.isServiceRunning(_floatingService)) {
            stopService(new Intent(_context, _floatingService));
        }
    }
}
