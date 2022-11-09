package tw.com.lig.sdk.sample.kotlinapp

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.ImageButton
import com.google.ar.sceneform.ArSceneView
import tw.com.lig.sdk.scanner.LightID

class ARActivity : Activity() {

    private lateinit var arSceneView: ArSceneView
    private lateinit var lightId: LightID
    private lateinit var controller: ARSceneformController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // init view
        setContentView(R.layout.activity_ar)
        arSceneView = findViewById(R.id.ar_scene_view)
        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            finish()
        }

        // get Light ID data
        lightId = intent.getParcelableExtra("light-id")!!

        // create 3D viewer
        controller = ARSceneformController(this, arSceneView, lightId)
    }

    override fun onResume() {
        super.onResume()
        controller.start()
    }

    override fun onPause() {
        super.onPause()
        controller.stop()
    }

    override fun onDestroy() {
        controller.destroy()
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // hide system bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController ?: return
            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsets.Type.statusBars())
            controller.hide(WindowInsets.Type.navigationBars())
        }
    }
}