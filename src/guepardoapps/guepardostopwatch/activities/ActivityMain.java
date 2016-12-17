package guepardoapps.guepardostopwatch.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import guepardoapps.guepardostopwatch.R;
import guepardoapps.guepardostopwatch.common.*;
import guepardoapps.guepardostopwatch.service.FloatingService;

import guepardoapps.toolset.controller.*;
import guepardoapps.toolset.services.AndroidSystemService;
import guepardoapps.toolset.services.MailService;
import guepardoapps.toolset.services.NavigationService;

public class ActivityMain extends Activity {

	private Context _context;

	private AndroidSystemService _androidSystemService;
	private MailService _mailService;
	private NavigationService _navigationService;
	private SharedPrefController _sharedPrefController;

	private Class<FloatingService> _floatingService;

	private TextView _minuteView, _secondsView, _milliSecondsView;
	private Button _btnStart, _btnPause, _btnStop, _btnExport, _btnClear, _btnClose;
	private ImageButton _btnImpressum, _btnSettings;

	private ScrollView _scrollView;

	private boolean _isRunning;
	private int _round;
	private Handler _stopwatchHandler;

	private long _roundStartTime = 0L;
	private long _roundTimeInMillies = 0L;
	private long _roundTimeSwap = 0L;
	private long _roundTime = 0L;
	private int _roundSeconds, _roundMinutes, _roundMilliSeconds;

	private long _startTimeFinal = 0L;
	private long _timeInMilliesFinal = 0L;
	private long _timeSwapFinal = 0L;
	private long _finalTime = 0L;
	private int _finalSeconds, _finalMinutes, _finalMilliSeconds;

	private Runnable _updateTimerMethod = new Runnable() {
		public void run() {
			_roundTimeInMillies = SystemClock.uptimeMillis() - _roundStartTime;
			_roundTime = _roundTimeSwap + _roundTimeInMillies;

			_roundSeconds = (int) (_roundTime / 1000);
			_roundMinutes = _roundSeconds / 60;
			_roundSeconds = _roundSeconds % 60;
			_roundMilliSeconds = (int) (_roundTime % 1000);

			_timeInMilliesFinal = SystemClock.uptimeMillis() - _startTimeFinal;
			_finalTime = _timeSwapFinal + _timeInMilliesFinal;

			_finalSeconds = (int) (_finalTime / 1000);
			_finalMinutes = _finalSeconds / 60;
			_finalSeconds = _finalSeconds % 60;
			_finalMilliSeconds = (int) (_finalTime % 1000);

			_minuteView.setText("" + _finalMinutes);
			_secondsView.setText("" + String.format("%02d", _finalSeconds));
			_milliSecondsView.setText("" + String.format("%03d", _finalMilliSeconds));

			_stopwatchHandler.postDelayed(this, 0);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.side_main);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Constants.ACTION_BAR_COLOR));

		_context = this;

		_androidSystemService = new AndroidSystemService(_context);
		_mailService = new MailService(_context);
		_navigationService = new NavigationService(_context);
		_sharedPrefController = new SharedPrefController(_context, Constants.SHARED_PREF_NAME);

		_floatingService = FloatingService.class;

		tryToStopService();

		_stopwatchHandler = new Handler();
		_round = 1;
		_isRunning = false;

		_minuteView = (TextView) findViewById(R.id.textTimerMin);
		_secondsView = (TextView) findViewById(R.id.textTimerSec);
		_milliSecondsView = (TextView) findViewById(R.id.textTimerMsec);

		_scrollView = (ScrollView) findViewById(R.id.scrollView);

		_btnStart = (Button) findViewById(R.id.btnStart);
		_btnStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!_isRunning) {
					_roundStartTime = SystemClock.uptimeMillis();
					_startTimeFinal = SystemClock.uptimeMillis();

					_stopwatchHandler.postDelayed(_updateTimerMethod, 0);

					_isRunning = true;
				}
			}
		});

		_btnPause = (Button) findViewById(R.id.btnPause);
		_btnPause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (_isRunning) {
					_btnExport.setText(_btnExport.getText().toString() + "" + "Round " + _round + " = " + _roundMinutes
							+ ":" + String.format("%02d", _roundSeconds) + ":"
							+ String.format("%03d", _roundMilliSeconds) + "\n");
					_scrollView.fullScroll(View.FOCUS_DOWN);

					_round++;

					_roundStartTime = SystemClock.uptimeMillis();
				}
			}
		});

		_btnStop = (Button) findViewById(R.id.btnStop);
		_btnStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (_isRunning) {
					_roundStartTime = 0L;
					_startTimeFinal = 0L;

					_stopwatchHandler.removeCallbacks(_updateTimerMethod);

					_btnExport.setText(_btnExport.getText().toString() + "" + "Round " + _round + " = " + _roundMinutes
							+ ":" + String.format("%02d", _roundSeconds) + ":"
							+ String.format("%03d", _roundMilliSeconds) + "\n");

					String minute = _minuteView.getText().toString();
					String sekunde = _secondsView.getText().toString();
					String milli = _milliSecondsView.getText().toString();

					_btnExport.setText(_btnExport.getText().toString() + "\n" + "Time = " + minute + ":" + sekunde + ":"
							+ milli + "\n ________________________ \n\n");

					_roundStartTime = 0L;
					_startTimeFinal = 0L;

					_minuteView.setText("00");
					_secondsView.setText("00");
					_milliSecondsView.setText("00");

					_finalSeconds = 0;
					_finalMinutes = 0;
					_finalMilliSeconds = 0;

					_roundSeconds = 0;
					_roundMinutes = 0;
					_roundMilliSeconds = 0;

					_isRunning = false;
					_round = 1;

					_scrollView.fullScroll(View.FOCUS_DOWN);
				}
			}
		});

		_btnExport = (Button) findViewById(R.id.timeValue);
		_btnExport.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				String times = _btnExport.getText().toString();
				if (times != null) {
					_mailService.SendMailWithContent("Times", times, false);
				}
			}
		});

		_btnClear = (Button) findViewById(R.id.clearbutton);
		_btnClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!_isRunning) {
					_btnExport.setText(null);
				}
			}
		});

		_btnImpressum = (ImageButton) findViewById(R.id.btnImpressum);
		_btnImpressum.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!_isRunning) {
					_navigationService.NavigateTo(ActivityImpressum.class, false);
				} else {
					Toast.makeText(_context, "Stopwatch is running!", Toast.LENGTH_SHORT).show();
				}
			}
		});

		_btnClose = (Button) findViewById(R.id.btnClose);
		_btnClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!_isRunning) {
					finish();
				} else {
					Toast.makeText(_context, "Stopwatch is running!", Toast.LENGTH_SHORT).show();
				}
			}
		});

		_btnSettings = (ImageButton) findViewById(R.id.btnSettings);
		_btnSettings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!_isRunning) {
					_navigationService.NavigateTo(ActivitySettings.class, false);
				} else {
					Toast.makeText(_context, "Stopwatch is running!", Toast.LENGTH_SHORT).show();
				}
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
		if (!_androidSystemService.IsServiceRunning(_floatingService)
				&& _sharedPrefController.LoadBooleanValueFromSharedPreferences(Constants.BUBBLE_STATE)) {
			startService(new Intent(_context, _floatingService));
		}
	}

	private void tryToStopService() {
		if (_androidSystemService.IsServiceRunning(_floatingService)) {
			stopService(new Intent(_context, _floatingService));
		}
	}
}
