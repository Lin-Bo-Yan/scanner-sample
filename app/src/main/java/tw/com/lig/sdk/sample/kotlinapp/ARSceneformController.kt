package tw.com.lig.sdk.sample.kotlinapp

import android.app.Activity
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import tw.com.lig.sdk.scanner.LightID
import tw.com.lig.sdk.scanner.Transform

class ARSceneformController(
        private val activity: Activity,
        private val sceneView: ArSceneView,
        private val lightId: LightID) : Scene.OnUpdateListener {

    private var isStarted = false
    private var isDestroyed = false
    private var modelPlaced = false

    init {
        sceneView.scene.addOnUpdateListener(this)
    }

    private fun addModelToScene(resId: Int, lightId: LightID) {
        ModelRenderable.builder()
            .setSource(activity, resId)
            .build()
            .thenAccept{ renderable ->

                val cameraPose = Transform()
                sceneView.arFrame?.camera?.displayOrientedPose?.toMatrix(cameraPose.data, 0)
                val transform = lightId.transform(cameraPose)
                val anchor = AnchorNode()
                anchor.renderable = renderable
                anchor.worldPosition = Vector3(
                    transform.translation.x,
                    transform.translation.y,
                    transform.translation.z
                )
                anchor.worldRotation = Quaternion(
                    transform.rotation.x,
                    transform.rotation.y,
                    transform.rotation.z,
                    transform.rotation.w
                )
                sceneView.scene.addChild(anchor)

            }
            .exceptionally { throwable ->
                null
            }
    }

    private fun createARSession(session: Session): Config {
        return Config(session).apply {
            focusMode = Config.FocusMode.AUTO
            augmentedFaceMode = Config.AugmentedFaceMode.DISABLED
            cloudAnchorMode = Config.CloudAnchorMode.DISABLED
            updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            depthMode = Config.DepthMode.DISABLED
            planeFindingMode = Config.PlaneFindingMode.DISABLED
        }
    }

    fun start() {
        if (isStarted || isDestroyed)
            return

        if (sceneView.session == null) {
            val session = Session(activity)
            val config = createARSession(session)
            session.configure(config)
            sceneView.setupSession(session)
        }

        // start
        sceneView.resume()
        isStarted = true
    }

    fun stop() {
        if (!isStarted || isDestroyed)
            return

        isStarted = false
        sceneView.pause()
    }

    fun destroy() {
        isDestroyed = true
        sceneView.session?.close()

    }

    override fun onUpdate(frameTime: FrameTime?) {
        if (sceneView.session == null || sceneView.arFrame == null)
            return

        if (!modelPlaced && sceneView.arFrame?.camera?.trackingState == TrackingState.TRACKING) {
            modelPlaced = true
            addModelToScene(R.raw.andy, lightId)
        }
    }
}