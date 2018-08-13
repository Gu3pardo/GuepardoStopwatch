package guepardoapps.stopme.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.os.SystemClock
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import guepardoapps.stopme.R
import guepardoapps.stopme.common.Constants
import guepardoapps.stopme.controller.SharedPreferenceController
import guepardoapps.stopme.utils.Logger
import io.reactivex.schedulers.Schedulers
import java.util.*

class FloatingService : Service() {
    private val tag: String = FloatingService::class.java.simpleName

    private val mailService: MailService = MailService(this)
    private val sharedPreferenceController: SharedPreferenceController = SharedPreferenceController(this)

    private var bubbleWindowManager: WindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var bubbleParamsStore: WindowManager.LayoutParams? = null
    private var bubbleView: ImageView = ImageView(this)
    private var bubblePosY: Int = 100
    private var bubbleMoved: Boolean = false

    private var stopwatchWindowManager: WindowManager? = null
    private var stopwatchView: View? = null

    private var minuteView: TextView? = null
    private var secondsView: TextView? = null
    private var milliSecondsView: TextView? = null

    private var scrollView: ScrollView? = null
    private var btnAbout: Button? = null
    private var btnSettings: Button? = null
    private var btnClose: Button? = null
    private var btnExport: Button? = null
    private var btnClear: Button? = null
    private var btnStart: Button? = null
    private var btnPause: Button? = null
    private var btnStop: Button? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        initBubbleView()

        ClockService.instance.initialize(this)
        ClockService.instance.timePublishSubject
                .subscribeOn(Schedulers.io())
                .subscribe(
                        // TODO
                        { response -> Logger.instance.info(tag, response) },
                        { error -> Logger.instance.error(tag, error) })
    }

    override fun onDestroy() {
        super.onDestroy()
        ClockService.instance.dispose()
        bubbleWindowManager.removeView(bubbleView)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initBubbleView() {
        bubblePosY = sharedPreferenceController.load(Constants.bubblePosY, Constants.bubbleDefaultPosY) as Int

        val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = bubblePosY
        bubbleParamsStore = params

        bubbleView.setImageResource(R.mipmap.ic_launcher)
        bubbleView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialTouchX: Float = 0.toFloat()
            private var initialTouchY: Float = 0.toFloat()

            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (params.x < 0) {
                            params.x = 0
                            bubbleParamsStore = params
                            bubbleWindowManager.updateViewLayout(bubbleView, bubbleParamsStore)
                        }

                        initialX = params.x
                        initialY = params.y

                        initialTouchX = event.rawX
                        initialTouchY = event.rawY

                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        view.performClick()
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()

                        if (initialX - params.x > 25
                                || initialY - params.y > 25
                                || params.x - initialX > 25
                                || params.y - initialY > 25) {
                            bubbleMoved = true
                        }

                        bubbleParamsStore = params
                        bubbleWindowManager.updateViewLayout(bubbleView, bubbleParamsStore)

                        return true
                    }
                }

                return false
            }
        })

        bubbleView.setOnClickListener {
            if (bubbleMoved) {
                bubbleMoved = false
                params.x = 0
                bubblePosY = params.y

                sharedPreferenceController.save(Constants.bubblePosY, bubblePosY)

                bubbleParamsStore = params
                bubbleWindowManager.updateViewLayout(bubbleView, bubbleParamsStore)
            } else {
                showStopwatchView()
            }
        }

        bubbleWindowManager.addView(bubbleView, bubbleParamsStore)
    }

    private fun showStopwatchView() {

        setStopwatchViews()

        btnAbout?.visibility = View.GONE
        btnSettings?.visibility = View.GONE
        btnClose?.setOnClickListener { removeView() }
        btnExport?.setOnClickListener { mailService.sendMail("Times", btnExport?.text.toString(), arrayListOf(), true) }
        btnClear?.setOnClickListener { btnExport?.text = "" }
        btnStart?.setOnClickListener { ClockService.instance.start() }
        btnPause?.setOnClickListener { ClockService.instance.pause() }
        btnStop?.setOnClickListener { ClockService.instance.stop() }

        btnPause.setOnClickListener({ view ->
            if (_isRunning) {
                var buttonExportText = btnExport.getText().toString()
                buttonExportText = String.format(Locale.GERMAN, "%sRound %d = %d:%02d:%03d\n",
                        buttonExportText, _round, _roundMinutes, _roundSeconds, _roundMilliSeconds)

                btnExport.setText(buttonExportText)
                _scrollView.fullScroll(View.FOCUS_DOWN)

                _round++
                _roundStartTime = SystemClock.uptimeMillis()
            }
        })

        btnStop.setOnClickListener({ view ->
            if (_isRunning) {
                _roundStartTime = 0L
                _startTimeFinal = 0L

                _stopwatchHandler.removeCallbacks(_updateTimerMethod)

                var buttonExportText = btnExport.getText().toString()
                buttonExportText = String.format(Locale.GERMAN, "%sRound %d = %d:%02d:%03d\n",
                        buttonExportText, _round, _roundMinutes, _roundSeconds, _roundMilliSeconds)

                btnExport.setText(buttonExportText)

                val minute = _minuteView.getText().toString()
                val second = _secondsView.getText().toString()
                val milli = _milliSecondsView.getText().toString()

                btnExport.setText(String.format("%s\nTime = %s:%s:%s\n ________________________ \n\n", btnExport.getText().toString(), minute, second, milli))

                _scrollView.fullScroll(View.FOCUS_DOWN)

                _roundStartTime = 0L
                _startTimeFinal = 0L

                _minuteView.setText(R.string.dummyTime)
                _secondsView.setText(R.string.dummyTime)
                _milliSecondsView.setText(R.string.dummyTime)

                _finalSeconds = 0
                _finalMinutes = 0
                _finalMilliSeconds = 0

                _roundSeconds = 0
                _roundMinutes = 0
                _roundMilliSeconds = 0

                _isRunning = false
                _round = 1
            }
        })

        attachView()
    }

    private fun setStopwatchViews() {
        stopwatchWindowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        stopwatchView = View.inflate(applicationContext, R.layout.side_main, null)

        minuteView = stopwatchView?.findViewById(R.id.textTimerMin)
        secondsView = stopwatchView?.findViewById(R.id.textTimerSec)
        milliSecondsView = stopwatchView?.findViewById(R.id.textTimerMsec)

        scrollView = stopwatchView?.findViewById(R.id.scrollView)

        btnAbout = stopwatchView?.findViewById(R.id.btnImpressum)
        btnSettings = stopwatchView?.findViewById(R.id.btnSettings)
        btnClose = stopwatchView?.findViewById(R.id.btnClose)
        btnExport = stopwatchView?.findViewById(R.id.timeValue)
        btnClear = stopwatchView?.findViewById(R.id.clearButton)
        btnStart = stopwatchView?.findViewById(R.id.btnStart)
        btnPause = stopwatchView?.findViewById(R.id.btnPause)
        btnStop = stopwatchView?.findViewById(R.id.btnStop)
    }

    @Suppress("DEPRECATION")
    private fun attachView() {
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.gravity = Gravity.CENTER
        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.alpha = 1.0f
        layoutParams.packageName = packageName
        layoutParams.buttonBrightness = 1f
        layoutParams.windowAnimations = android.R.style.Animation_Dialog

        stopwatchWindowManager?.addView(stopwatchView, layoutParams)
    }

    private fun removeView() {
        stopwatchWindowManager?.removeView(stopwatchView)

        stopwatchView = null

        minuteView = null
        secondsView = null
        milliSecondsView = null

        scrollView = null

        btnAbout = null
        btnSettings = null
        btnClose = null
        btnExport = null
        btnClear = null
        btnStart = null
        btnPause = null
        btnStop = null

        stopwatchWindowManager = null
    }
}