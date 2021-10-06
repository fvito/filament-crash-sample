package com.example.crashsample

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.TransformableNode
import com.google.android.filament.ColorGrading
import com.google.android.filament.ToneMapper
import com.google.ar.core.*
import com.google.ar.sceneform.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.BaseArFragment
import java.lang.ref.WeakReference

class ArActivity : AppCompatActivity(), BaseArFragment.OnSessionConfigurationListener,
    ArFragment.OnViewCreatedListener, BaseArFragment.OnTapArPlaneListener,
    FragmentOnAttachListener {

    private var arFragment: ArFragment? = null
    private var model: Renderable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableSharedArCache(this)

        setContentView(R.layout.activity_ar)
        supportFragmentManager.addFragmentOnAttachListener(this)
        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.arFragment, ArFragment::class.java, null)
                    .commit()
            }
        }
        loadModels()
    }

    override fun onDestroy() {
        flushSharedArCache()
        super.onDestroy()
    }

    override fun onAttachFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        if (fragment.id == R.id.arFragment) {
            arFragment = fragment as ArFragment
            arFragment!!.setOnSessionConfigurationListener(this)
            arFragment!!.setOnViewCreatedListener(this)
            arFragment!!.setOnTapArPlaneListener(this)
        }
    }

    override fun onSessionConfiguration(session: Session, config: Config) {
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.depthMode = Config.DepthMode.AUTOMATIC
        }
    }

    override fun onViewCreated(arFragment: ArFragment?, arSceneView: ArSceneView) {
        // Currently, the tone-mapping should be changed to FILMIC
        // because with other tone-mapping operators except LINEAR
        // the inverseTonemapSRGB function in the materials can produce incorrect results.
        // The LINEAR tone-mapping cannot be used together with the inverseTonemapSRGB function.
        val renderer: Renderer? = arSceneView.renderer
        renderer?.filamentView?.colorGrading = ColorGrading.Builder()
            .toneMapper(ToneMapper.Filmic())
            .build(EngineInstance.getEngine().filamentEngine)

        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL)
    }

    private fun loadModels() {
        val weakActivity: WeakReference<ArActivity> = WeakReference(this)
        ModelRenderable.builder()
            .setSource(
                this,
                Uri.parse("https://storage.googleapis.com/ar-answers-in-search-models/static/Tiger/model.glb")
            )
            .setIsFilamentGltf(true)
            .setAsyncLoadEnabled(true)
            .build()
            .thenAccept { model: ModelRenderable ->
                val activity: ArActivity? = weakActivity.get()
                if (activity != null) {
                    activity.model = model
                }
            }
            .exceptionally { throwable: Throwable? ->
                Toast.makeText(
                    this, "Unable to load model", Toast.LENGTH_LONG
                ).show()
                null
            }
    }

    override fun onTapPlane(hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent?) {
        if (model == null) {
            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show()
            return
        }

        // Create the Anchor.
        val anchor: Anchor = hitResult.createAnchor()
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arFragment!!.arSceneView.scene)

        // Create the transformable model and add it to the anchor.
        val model = TransformableNode(arFragment!!.transformationSystem)
        model.setParent(anchorNode)
        model.setRenderable(this.model).animate(true).start()
        model.select()

        Node().apply {
            setParent(model)
            isEnabled = false
            localPosition = Vector3(0.0f, 1.0f, 0.0f)
            isEnabled = true
        }
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, ArActivity::class.java)
    }
}