package tw.com.lig.sdk.sample.kotlinapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import tw.com.lig.sdk.scanner.LiGScanner
import tw.com.lig.sdk.scanner.LightID

class MainActivity: Activity() {

    private var surfaceView: LiGSurfaceView? = null
    private var opened = false

    private val sLackPermissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private fun updateLightIDMessage(id: LightID) {
        val builder = StringBuilder();

        var status = "";
        when (id.status) {
            LightID.Status.READY -> status = "READY"
            LightID.Status.NOT_DETECTED -> status = "NOT_DETECTED"
            LightID.Status.NOT_DECODED -> status = "NOT_DECODED"
            LightID.Status.INVALID_POSITION -> status = "INVALID_POSITION"
            LightID.Status.NOT_REGISTERED -> status = "NOT_REGISTERED"
            LightID.Status.INVALID_POSITION_TOO_CLOSE -> status = "INVALID_POSITION_TOO_CLOSE"
            LightID.Status.INVALID_POSITION_UNKNOWN -> status = "INVALID_POSITION_UNKNOWN"
        }
        builder.append("Status: $status\n")

        if (id.isDetected) {
            builder.append("Detection: ${id.detectionTime} ms\n")
            builder.append("Decoded: ${id.decodedTime} ms\n")
        }

        if (id.isReady) {
            builder.append("Rotation: ${id.rotation.x}, ${id.rotation.y}, ${id.rotation.z}\n")
            builder.append("Translation: ${id.translation.x}, ${id.translation.y}, ${id.translation.z}\n")
            builder.append("Position: ${id.position.x}, ${id.position.y}, ${id.position.z}\n")
        }

        findViewById<TextView>(R.id.lightid_message).text = builder.toString()
    }

    private fun updateCommandMessage(msg: String) {
        val view = findViewById<TextView>(R.id.command_msg)
        val origin = view.text
        val combine = "${origin}\n${msg}"
        view.text = combine
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQ_CODE) {
            // start scanner
            LiGScanner.start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_main);
        surfaceView = findViewById(R.id.surface_view)
        findViewById<TextView>(R.id.app_version).text = BuildConfig.VERSION_NAME

        // receive scanner status
        LiGScanner.setStatusListener { status ->
            runOnUiThread { updateCommandMessage("onStatus > $status") }
        }

        // receive scan result
        LiGScanner.setResultListener { ids ->
            if (ids.isNotEmpty()) {
                surfaceView?.let {
                    val lightId = ids[0]
                    it.send(lightId)
                    runOnUiThread {  updateLightIDMessage(lightId) }

                    if (lightId.isReady && !opened) {
                        opened = true
                        val intent = Intent(this, ARActivity::class.java)
                        intent.putExtra("light-id", lightId)
                        startActivity(intent)
                    }
                }
            }
        }

        // show the scanner version and UUID
        updateCommandMessage("UUID > ${LiGScanner.getUUID()}")
        updateCommandMessage("SDK Version > ${LiGScanner.version}")
    }

    override fun onStart() {
        super.onStart()
        findViewById<LiGSurfaceView>(R.id.surface_view).startDrawing()
        requestPermissions(sLackPermissions, PERMISSION_REQ_CODE)
    }

    override fun onStop() {
        super.onStop()
        findViewById<LiGSurfaceView>(R.id.surface_view).stopDrawing()

        // stop scanner
        LiGScanner.stop()

        // clear message
        findViewById<TextView>(R.id.command_msg).text = ""

        opened = false
    }

    companion object {
        private const val PERMISSION_REQ_CODE = 10202
    }
}