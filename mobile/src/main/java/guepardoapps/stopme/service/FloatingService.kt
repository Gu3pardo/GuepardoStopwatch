package guepardoapps.stopme.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import com.rey.material.widget.Button
import com.rey.material.widget.FloatingActionButton
import guepardoapps.stopme.R
import guepardoapps.stopme.common.Constants
import guepardoapps.stopme.controller.SharedPreferenceController
import guepardoapps.stopme.extensions.integerFormat
import guepardoapps.stopme.models.RxTime
import guepardoapps.stopme.utils.Logger
import io.reactivex.schedulers.Schedulers

// TODO check overlay draw permission
// https://stackoverflow.com/questions/7569937/unable-to-add-window-android-view-viewrootw44da9bc0-permission-denied-for-t#answer-34061521

class FloatingService : Service() {
    private val tag: String = FloatingService::class.java.simpleName

    private lateinit var windowManager: WindowManager

    private var bubbleParamsStore: WindowManager.LayoutParams? = null
    private lateinit var bubbleView: ImageView
    private var bubblePosY: Int = 100
    private var bubbleMoved: Boolean = false

    private var stopwatchView: View? = null
    private var minuteView: TextView? = null
    private var secondsView: TextView? = null
    private var milliSecondsView: TextView? = null
    private var scrollView: ScrollView? = null
    private var btnAbout: FloatingActionButton? = null
    private var btnSettings: FloatingActionButton? = null
    private var btnClose: FloatingActionButton? = null
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

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        bubbleView = ImageView(this)

        initBubbleView()

        if (ClockService.instance.isDisposed) {
            ClockService.instance.initialize(this)
        }

        ClockService.instance.timePublishSubject
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { response -> updateViews(response) },
                        { error -> Logger.instance.error(tag, error) })
    }

    override fun onDestroy() {
        super.onDestroy()
        ClockService.instance.dispose()
        windowManager.removeView(bubbleView)
    }

    @Suppress("DEPRECATION")
    @SuppressLint("ClickableViewAccessibility")
    private fun initBubbleView() {
        val sharedPreferenceController = SharedPreferenceController(this)

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
                            windowManager.updateViewLayout(bubbleView, bubbleParamsStore)
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
                        windowManager.updateViewLayout(bubbleView, bubbleParamsStore)

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
                windowManager.updateViewLayout(bubbleView, bubbleParamsStore)
            } else {
                findViews()
                addActionsToViews()
                attachViews()
            }
        }

        windowManager.addView(bubbleView, bubbleParamsStore)
    }

    private fun findViews() {
        stopwatchView = View.inflate(applicationContext, R.layout.side_main, null)

        minuteView = stopwatchView?.findViewById(R.id.textTimerMin)
        secondsView = stopwatchView?.findViewById(R.id.textTimerSec)
        milliSecondsView = stopwatchView?.findViewById(R.id.textTimerMsec)

        scrollView = stopwatchView?.findViewById(R.id.scrollView)

        btnAbout = stopwatchView?.findViewById(R.id.btnAbout)
        btnSettings = stopwatchView?.findViewById(R.id.btnSettings)
        btnClose = stopwatchView?.findViewById(R.id.btnClose)
        btnExport = stopwatchView?.findViewById(R.id.timeValue)
        btnClear = stopwatchView?.findViewById(R.id.clearButton)
        btnStart = stopwatchView?.findViewById(R.id.btnStart)
        btnPause = stopwatchView?.findViewById(R.id.btnPause)
        btnStop = stopwatchView?.findViewById(R.id.btnStop)
    }

    @SuppressLint("SetTextI18n")
    private fun addActionsToViews() {
        btnAbout?.visibility = View.GONE
        btnSettings?.visibility = View.GONE
        btnClose?.setOnClickListener { removeViews() }
        btnExport?.setOnClickListener { MailService(this).sendMail("Times", btnExport?.text.toString(), arrayListOf(), true) }
        btnClear?.setOnClickListener { btnExport?.text = "" }
        btnStart?.setOnClickListener { ClockService.instance.start() }
        btnPause?.setOnClickListener { ClockService.instance.pause() }
        btnStop?.setOnClickListener {
            ClockService.instance.stop()

            val minutes = minuteView?.text.toString()
            val seconds = secondsView?.text.toString()
            val milliseconds = milliSecondsView?.text.toString()
            btnExport?.text = "${btnExport?.text}\nTime = $minutes:$seconds:$milliseconds\n ________________________ \n\n"

            scrollView?.fullScroll(View.FOCUS_DOWN)

            minuteView?.setText(R.string.dummyTime)
            secondsView?.setText(R.string.dummyTime)
            milliSecondsView?.setText(R.string.dummyTime)
        }
    }

    @Suppress("DEPRECATION")
    private fun attachViews() {
        val layoutParams = WindowManager.LayoutParams()

        layoutParams.gravity = Gravity.CENTER
        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.alpha = 1.0f
        layoutParams.packageName = packageName
        layoutParams.buttonBrightness = 1f
        layoutParams.windowAnimations = android.R.style.Animation_Dialog

        windowManager.addView(stopwatchView, layoutParams)
    }

    private fun updateViews(rxTime: RxTime?) {
        if (rxTime === null || !rxTime.running) {
            return
        }

        var finalSeconds = (rxTime.timeInMillisFinal / 1000).toInt()
        val finalMinutes = finalSeconds / 60
        finalSeconds %= 60
        val finalMilliSeconds = (rxTime.timeInMillisFinal % 1000).toInt()

        minuteView?.text = finalMinutes.integerFormat(2)
        secondsView?.text = finalSeconds.integerFormat(2)
        milliSecondsView?.text = finalMilliSeconds.integerFormat(2)

        var btnExportText = ""
        rxTime.rounds.forEachIndexed { index, roundTimeInMillis -> btnExportText += createRoundText(index, roundTimeInMillis) }
        btnExport?.text = btnExportText

        scrollView?.fullScroll(View.FOCUS_DOWN)
    }

    private fun createRoundText(index: Int, roundTimeInMillis: Long): String {
        var roundSeconds = (roundTimeInMillis / 1000).toInt()
        val roundMinutes = roundSeconds / 60
        roundSeconds %= 60
        val roundMilliSeconds = (roundTimeInMillis % 1000).toInt()
        return "Round ${index + 1} = $roundMinutes:${roundSeconds.integerFormat(2)}:${roundMilliSeconds.integerFormat(3)}\n"
    }

    private fun removeViews() {
        windowManager.removeView(stopwatchView)

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
    }
}