package tw.com.lig.sdk.sample.kotlinapp

import android.app.Application
import tw.com.lig.sdk.scanner.LiGScanner

class MainApplication: Application() {

    companion object {
        private const val SDK_PRODUCT_KEY = "B62D1-AED87-2C3B0-54A1F-D09D2"
    }

    override fun onCreate() {
        super.onCreate()
        LiGScanner.initialize(this, SDK_PRODUCT_KEY)
    }

    override fun onTerminate() {
        super.onTerminate()
        LiGScanner.deinitialize()
    }
}