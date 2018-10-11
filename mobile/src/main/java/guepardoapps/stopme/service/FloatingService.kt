package guepardoapps.stopme.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import guepardoapps.stopme.R
import guepardoapps.stopme.common.Constants
import guepardoapps.stopme.controller.SharedPreferenceController
import guepardoapps.stopme.controller.SystemInfoController
import guepardoapps.stopme.logging.Logger
import guepardoapps.stopme.views.ClockView
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

// https://stackoverflow.com/questions/7569937/unable-to-add-window-android-view-viewrootw44da9bc0-permission-denied-for-t#answer-34061521

class FloatingService : Service() {
    private val tag: String = FloatingService::class.java.simpleName

    private lateinit var systemInfoController: SystemInfoController
    private lateinit var windowManager: WindowManager

    private var bubbleParamsStore: WindowManager.LayoutParams? = null
    private lateinit var bubbleView: ImageView
    private var bubblePosX: Int = 0
    private var bubblePosY: Int = 100
    private var bubbleMoved: Boolean = false

    private var stopwatchView: View? = null
    private var subscription: Disposable? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

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
            Logger.instance.error(tag, exception)
        } finally {
            subscription = null
            stopwatchView = null
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("ClickableViewAccessibility")
    private fun initBubbleView() {
        val sharedPreferenceController = SharedPreferenceController(this)

        bubblePosX = sharedPreferenceController.load(Constants.bubblePosX, Constants.bubbleDefaultPosX) as Int
        bubblePosY = sharedPreferenceController.load(Constants.bubblePosY, Constants.bubbleDefaultPosY) as Int

        val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)
        params.gravity = Gravity.TOP or Gravity.START
        params.x = bubblePosX
        params.y = bubblePosY
        if (systemInfoController.currentAndroidApi() >= Build.VERSION_CODES.O) {
            @RequiresApi(Build.VERSION_CODES.O)
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }

        bubbleParamsStore = params

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

                if (params.x > (systemInfoController.displayDimension().width / 2)) {
                    params.x = systemInfoController.displayDimension().width
                } else {
                    params.x = 0
                }
                bubblePosX = params.x
                bubblePosY = params.y

                sharedPreferenceController.save(Constants.bubblePosX, bubblePosX)
                sharedPreferenceController.save(Constants.bubblePosY, bubblePosY)

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
                                { error -> Logger.instance.error(tag, error) })

                val layoutParams = WindowManager.LayoutParams()

                layoutParams.gravity = Gravity.CENTER
                if (systemInfoController.currentAndroidApi() >= Build.VERSION_CODES.O) {
                    @RequiresApi(Build.VERSION_CODES.O)
                    layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                }
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
                layoutParams.alpha = 1.0f
                layoutParams.packageName = packageName
                layoutParams.buttonBrightness = 1f
                layoutParams.windowAnimations = android.R.style.Animation_Dialog

                windowManager.addView(stopwatchView, layoutParams)
            }
        }

        windowManager.addView(bubbleView, bubbleParamsStore)
    }
}