package guepardoapps.stopme.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.annotation.RequiresApi
import guepardoapps.stopme.R
import guepardoapps.stopme.controller.SharedPreferenceController
import guepardoapps.stopme.controller.SystemInfoController
import guepardoapps.stopme.logging.Logger
import guepardoapps.stopme.views.ClockView
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

// https://stackoverflow.com/questions/7569937/unable-to-add-window-android-view-viewrootw44da9bc0-permission-denied-for-t#answer-34061521

@ExperimentalUnsignedTypes
class FloatingService : Service() {

    private lateinit var bubbleView: ImageView

    private var stopwatchView: View? = null

    private var subscription: Disposable? = null

    private lateinit var systemInfoController: SystemInfoController

    private lateinit var windowManager: WindowManager

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        systemInfoController = SystemInfoController(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        bubbleView = ImageView(this)

        initBubbleView()

        if (ClockService.instance.isDisposed) {
            ClockService.instance.initialize(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ClockService.instance.dispose()

        try {
            subscription?.dispose()
            windowManager.removeView(stopwatchView)
            windowManager.removeView(bubbleView)
        } catch (exception: Exception) {
            Logger.instance.error(FloatingService::class.java.simpleName, exception)
        } finally {
            subscription = null
            stopwatchView = null
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("ClickableViewAccessibility")
    private fun initBubbleView() {
        val sharedPreferenceController = SharedPreferenceController(this)

        var bubbleMoved = false
        var bubbleParamsStore: WindowManager.LayoutParams?

        val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.START
            x = sharedPreferenceController.load(getString(R.string.sharedPrefBubblePosX), resources.getInteger(R.integer.sharedPrefBubbleDefaultPosX))
            y = sharedPreferenceController.load(getString(R.string.sharedPrefBubblePosY), resources.getInteger(R.integer.sharedPrefBubbleDefaultPosY))
        }

        if (systemInfoController.currentAndroidApi() >= Build.VERSION_CODES.O) {
            @RequiresApi(Build.VERSION_CODES.O)
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }

        val backgroundShape = GradientDrawable()
        backgroundShape.setColor(resources.getColor(R.color.colorPrimaryDark))
        backgroundShape.cornerRadius = 100.0f

        bubbleView.background = backgroundShape
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

                        val minMovement = resources.getInteger(R.integer.bubbleMinMove)
                        bubbleMoved = initialX - params.x > minMovement
                                || initialY - params.y > minMovement
                                || params.x - initialX > minMovement
                                || params.y - initialY > minMovement

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

                params.x = if (params.x > systemInfoController.displayDimension().width / 2) systemInfoController.displayDimension().width else 0

                sharedPreferenceController.save(getString(R.string.sharedPrefBubblePosX), params.x)
                sharedPreferenceController.save(getString(R.string.sharedPrefBubblePosY), params.y)

                bubbleParamsStore = params
                windowManager.updateViewLayout(bubbleView, bubbleParamsStore)
            } else {
                stopwatchView = View.inflate(applicationContext, R.layout.side_main, null)

                val clockView: ClockView = stopwatchView!!.findViewById(R.id.clockView)
                clockView.setCloseCallback {
                    subscription?.dispose()
                    subscription = null
                    windowManager.removeView(stopwatchView)
                    stopwatchView = null
                }

                subscription = ClockService.instance.timePublishSubject
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                { response -> clockView.updateViews(response) },
                                { error -> Logger.instance.error(FloatingService::class.java.simpleName, error) })

                val layoutParams = WindowManager.LayoutParams()
                        .apply {
                            gravity = Gravity.CENTER
                            width = WindowManager.LayoutParams.MATCH_PARENT
                            height = WindowManager.LayoutParams.WRAP_CONTENT
                            alpha = 1.0f
                            buttonBrightness = 1f
                            windowAnimations = android.R.style.Animation_Dialog
                        }
                layoutParams.packageName = packageName
                if (systemInfoController.currentAndroidApi() >= Build.VERSION_CODES.O) {
                    @RequiresApi(Build.VERSION_CODES.O)
                    layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                }

                windowManager.addView(stopwatchView, layoutParams)
            }
        }

        bubbleParamsStore = params
        windowManager.addView(bubbleView, bubbleParamsStore)
    }
}