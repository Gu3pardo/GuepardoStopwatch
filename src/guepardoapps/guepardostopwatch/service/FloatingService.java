package guepardoapps.guepardostopwatch.service;

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
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import guepardoapps.guepardostopwatch.R;
import guepardoapps.guepardostopwatch.common.*;

import guepardoapps.library.toolset.controller.MailController;
import guepardoapps.library.toolset.controller.SharedPrefController;

public class FloatingService extends Service {

	private Context _context;
	private MailController _mailController;
	private SharedPrefController _sharedPrefController;

	private WindowManager _bubbleViewManager;
	private ImageView _bubble;
	private LayoutParams _bubbleParamsStore;
	private int _bubblePosY = 100;
	private Boolean _movedBubble = false;

	private WindowManager _stopwatchViewManager;
	private View _stopwatchView;

	private boolean _isRunning;
	int _round;
	private Handler _stopwatchHandler;

	private TextView _minuteView, _secondsView, _milliSecondsView;
	private Button _btnStart, _btnPause, _btnStop, _btnExport, _btnClear, _btnClose;
	private ImageButton _btnImpressum, _btnSettings;

	private ScrollView _scrollView;

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
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		_context = this;
		_mailController = new MailController(_context);
		_sharedPrefController = new SharedPrefController(_context, SharedPrefConstants.SHARED_PREF_NAME);

		_stopwatchHandler = new Handler();
		_round = 1;
		_isRunning = false;

		_bubblePosY = _sharedPrefController.LoadIntegerValueFromSharedPreferences(SharedPrefConstants.BUBBLE_POS_Y);

		_bubbleViewManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		_bubble = new ImageView(_context);
		_bubble.setImageResource(R.drawable.ic_launcher);

		final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);

		params.gravity = Gravity.TOP | Gravity.START;
		params.x = 0;
		params.y = _bubblePosY;

		_bubbleParamsStore = params;

		_bubble.setOnTouchListener(new View.OnTouchListener() {
			private int initialX;
			private int initialY;
			private float initialTouchX;
			private float initialTouchY;

			@Override
			public boolean onTouch(View view, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (params.x < 0) {
						params.x = 0;

						_bubbleParamsStore = params;
						_bubbleViewManager.updateViewLayout(_bubble, _bubbleParamsStore);
					}
					initialX = params.x;
					initialY = params.y;
					initialTouchX = event.getRawX();
					initialTouchY = event.getRawY();
					return true;
				case MotionEvent.ACTION_UP:
					view.performClick();
					return true;
				case MotionEvent.ACTION_MOVE:
					params.x = initialX + (int) (event.getRawX() - initialTouchX);
					params.y = initialY + (int) (event.getRawY() - initialTouchY);
					if (initialX - params.x > 25 || initialY - params.y > 25 || params.x - initialX > 25
							|| params.y - initialY > 25) {
						_movedBubble = true;
					}

					_bubbleParamsStore = params;
					_bubbleViewManager.updateViewLayout(_bubble, _bubbleParamsStore);
					return true;
				}
				return false;
			}
		});

		_bubble.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (_movedBubble) {
					_movedBubble = false;
					params.x = 0;
					_bubblePosY = params.y;

					_sharedPrefController.SaveIntegerValue(SharedPrefConstants.BUBBLE_POS_Y, _bubblePosY);

					_bubbleParamsStore = params;
					_bubbleViewManager.updateViewLayout(_bubble, _bubbleParamsStore);
				} else {
					showStopwatch();
				}
			}
		});
		_bubbleViewManager.addView(_bubble, _bubbleParamsStore);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (_bubble != null) {
			_bubbleViewManager.removeView(_bubble);
		}
		if (_isRunning) {
			_stopwatchHandler.removeCallbacks(_updateTimerMethod);

			_isRunning = false;
		}
	}

	private void showStopwatch() {
		_stopwatchViewManager = (WindowManager) _context.getApplicationContext()
				.getSystemService(Context.WINDOW_SERVICE);
		WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

		layoutParams.gravity = Gravity.CENTER;
		layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		layoutParams.alpha = 1.0f;
		layoutParams.packageName = _context.getPackageName();
		layoutParams.buttonBrightness = 1f;
		layoutParams.windowAnimations = android.R.style.Animation_Dialog;

		_stopwatchView = View.inflate(_context.getApplicationContext(), R.layout.side_main, null);

		_minuteView = (TextView) _stopwatchView.findViewById(R.id.textTimerMin);
		_secondsView = (TextView) _stopwatchView.findViewById(R.id.textTimerSec);
		_milliSecondsView = (TextView) _stopwatchView.findViewById(R.id.textTimerMsec);

		_btnImpressum = (ImageButton) _stopwatchView.findViewById(R.id.btnImpressum);
		_btnImpressum.setVisibility(View.GONE);

		_btnSettings = (ImageButton) _stopwatchView.findViewById(R.id.btnSettings);
		_btnSettings.setVisibility(View.GONE);

		_scrollView = (ScrollView) _stopwatchView.findViewById(R.id.scrollView);

		_btnStart = (Button) _stopwatchView.findViewById(R.id.btnStart);
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

		_btnPause = (Button) _stopwatchView.findViewById(R.id.btnPause);
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

		_btnStop = (Button) _stopwatchView.findViewById(R.id.btnStop);
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

		_btnExport = (Button) _stopwatchView.findViewById(R.id.timeValue);
		_btnExport.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				String times = _btnExport.getText().toString();
				if (times != null) {
					_mailController.SendMailWithContent("Times", times, true);
				}
			}
		});

		_btnClear = (Button) _stopwatchView.findViewById(R.id.clearbutton);
		_btnClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!_isRunning) {
					_btnExport.setText(null);
				}
			}
		});

		_btnClose = (Button) _stopwatchView.findViewById(R.id.btnClose);
		_btnClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!_isRunning) {
					_stopwatchViewManager.removeView(_stopwatchView);
				}
			}
		});

		_stopwatchViewManager.addView(_stopwatchView, layoutParams);
	}
}