package tw.com.lig.sdk.sample.kotlinapp

import android.app.Application
import tw.com.lig.sdk.scanner.LiGScanner

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        LiGScanner.initialize(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        LiGScanner.deinitialize()
    }
}