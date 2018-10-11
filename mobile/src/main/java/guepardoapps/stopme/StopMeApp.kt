package guepardoapps.stopme

import android.app.Application
import guepardoapps.stopme.logging.Logger

class StopMeApp : Application() {
    private val tag: String = StopMeApp::class.java.simpleName

    override fun onCreate() {
        super.onCreate()
        Logger.instance.initialize(this)
        Logger.instance.debug(tag, "onCreate")
    }
}