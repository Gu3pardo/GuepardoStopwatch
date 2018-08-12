package guepardoapps.stopme.service

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.SystemClock
import guepardoapps.stopme.extensions.replaceLast
import guepardoapps.stopme.models.RxTime
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class ClockService private constructor() : IClockService, Disposable {

    private var context: Context? = null

    private var stopwatchHandler: Handler? = null
    private var isRunning: Boolean = false
    private var startTimeFinal = 0L
    private var roundStartTime = 0L
    private var roundList: ArrayList<Long> = arrayListOf()

    private val updateTimerMethod = object : Runnable {
        override fun run() {
            val timeInMillisFinal = SystemClock.uptimeMillis() - startTimeFinal
            val roundTimeInMillis = SystemClock.uptimeMillis() - roundStartTime
            roundList.replaceLast(roundTimeInMillis)

            timePublishSubject.onNext(RxTime(isRunning, timeInMillisFinal, roundList))

            stopwatchHandler?.postDelayed(this, 1)
        }
    }

    override val timePublishSubject: PublishSubject<RxTime> = PublishSubject.create<RxTime>()!!

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val instance: ClockService = ClockService()
    }

    companion object {
        val instance: ClockService by lazy { Holder.instance }
    }

    override fun initialize(context: Context) {
        if (this.context === null) {
            this.context = context
            this.stopwatchHandler = Handler()
        }
    }

    override fun start() {
        if (!isRunning) {
            startTimeFinal = SystemClock.uptimeMillis()
            roundStartTime = SystemClock.uptimeMillis()
            roundList = arrayListOf(0)

            stopwatchHandler?.postDelayed(updateTimerMethod, 1)
            isRunning = true

            timePublishSubject.onNext(RxTime(isRunning, 0, roundList))
        }
    }

    override fun pause() {
        if (isRunning) {
            val roundTimeInMillis = SystemClock.uptimeMillis() - roundStartTime
            roundList.add(roundTimeInMillis)
            roundStartTime = SystemClock.uptimeMillis()
        }
    }

    override fun stop() {
        if (isRunning) {
            val timeInMillisFinal = SystemClock.uptimeMillis() - startTimeFinal
            val roundTimeInMillis = SystemClock.uptimeMillis() - roundStartTime
            roundList.replaceLast(roundTimeInMillis)

            startTimeFinal = 0L
            roundStartTime = 0L

            stopwatchHandler?.removeCallbacks(updateTimerMethod)
            isRunning = false

            timePublishSubject.onNext(RxTime(isRunning, timeInMillisFinal, roundList))
        }
    }

    override fun isDisposed(): Boolean {
        return this.context === null || this.stopwatchHandler === null
    }

    override fun dispose() {
        this.stopwatchHandler?.removeCallbacks(updateTimerMethod)
        this.stopwatchHandler = null
        this.context = null
    }
}