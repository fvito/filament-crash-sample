package com.example.crashsample

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable

class DetailActivity : AppCompatActivity() {

    private lateinit var header: TextView
    private lateinit var sceneView: SceneView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_defail)

        enableSharedArCache(this)

        header = findViewById(R.id.header)
        sceneView = findViewById(R.id.sceneView)
        findViewById<Button>(R.id.detailsButton).setOnClickListener {
            val intent = ArActivity.newIntent(this@DetailActivity)
            startActivity(intent)
        }

        val item = intent.getParcelableExtra<Item>("ITEM") ?: return
        header.text = "Details of ${item.title}"

        loadModel()
    }

    override fun onDestroy() {
        flushSharedArCache()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        try {
            sceneView.resume()
        } catch (e: CameraNotAvailableException) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        sceneView.pause()
    }

    private fun loadModel() {
        ModelRenderable.builder()
            .setSource(
                this,
                Uri.parse("https://github.com/ThomasGorisse/sceneform-android-sdk/raw/master/samples/image-texture/src/main/assets/models/cube.glb")
            )
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { model: ModelRenderable? ->
                val modelNode1 = Node()
                modelNode1.renderable = model
                modelNode1.localRotation = Quaternion.multiply(
                    Quaternion.axisAngle(Vector3(1f, 0f, 0f), 45f),
                    Quaternion.axisAngle(Vector3(0f, 1f, 0f), 75f)
                )
                modelNode1.localPosition = Vector3(0f, 0f, -0.5f)
                sceneView.scene.addChild(modelNode1)
            }
            .exceptionally { throwable: Throwable? ->
                throwable?.printStackTrace()
                Toast.makeText(
                    this,
                    "Unable to load model",
                    Toast.LENGTH_LONG
                ).show()
                null
            }
    }

    companion object {
        fun newIntent(context: Context, item: Item) =
            Intent(context, DetailActivity::class.java).apply {
                putExtra("ITEM", item)
            }
    }
}