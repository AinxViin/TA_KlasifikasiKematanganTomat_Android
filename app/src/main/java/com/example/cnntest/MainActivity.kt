package com.example.cnntest

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private lateinit var buttonScan: Button

    private val imageSize = 224
    private var tempCameraUri: Uri? = null

    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var requestCameraPermission: ActivityResultLauncher<String>

    private lateinit var interpreter: Interpreter

    private val classes = arrayOf("matang", "mentah", "setengah matang")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonScan = findViewById(R.id.buttonscan)

        val modelBuffer = FileUtil.loadMappedFile(this, "klasiftomatv3.tflite")
        interpreter = Interpreter(modelBuffer)

        supportFragmentManager.setFragmentResultListener(
            BottomSheetPilihGambar.RESULT_KEY, this
        ) { _, bundle ->
            when (bundle.getString(BottomSheetPilihGambar.RESULT_SOURCE)) {
                BottomSheetPilihGambar.SOURCE_GALLERY -> pickFromGallery()
                BottomSheetPilihGambar.SOURCE_CAMERA  -> pickFromCamera()
            }
        }

        registerLaunchers()

        buttonScan.setOnClickListener {
            BottomSheetPilihGambar().show(supportFragmentManager, "BottomSheetPilihGambar")
        }
    }

    private fun registerLaunchers() {
        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri -> uri?.let { handleImageUri(it) } }

        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { ok -> if (ok && tempCameraUri != null) handleImageUri(tempCameraUri!!) }

        requestCameraPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted -> if (granted) launchCameraNow() }
    }

    private fun pickFromGallery() = galleryLauncher.launch("image/*")

    private fun pickFromCamera() {
        val hasCam = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCam) {
            launchCameraNow()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCameraNow() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "capture_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        tempCameraUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        tempCameraUri?.let { cameraLauncher.launch(it) }
    }

    private fun handleImageUri(uri: Uri) {
        val bmp = decodeBitmapFromUri(uri) ?: return
        val label = classify(bmp)
        goToResult(uri, label)
    }

    private fun decodeBitmapFromUri(uri: Uri): Bitmap? =
        try {
            contentResolver.openInputStream(uri)?.use { input: InputStream ->
                BitmapFactory.decodeStream(input)
            }
        } catch (_: Exception) { null }

    private fun centerCropSquare(b: Bitmap): Bitmap {
        val size = min(b.width, b.height)
        val x = (b.width - size) / 2
        val y = (b.height - size) / 2
        return Bitmap.createBitmap(b, x, y, size, size)
    }

    private fun classify(srcBitmap: Bitmap): String {
        val square = centerCropSquare(srcBitmap)
        val image = Bitmap.createScaledBitmap(square, imageSize, imageSize, false)

        val inputTensor = TensorBuffer.createFixedSize(
            intArrayOf(1, imageSize, imageSize, 3),
            DataType.FLOAT32
        )

        val buffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3).apply {
            order(ByteOrder.nativeOrder())
        }

        val intValues = IntArray(imageSize * imageSize)
        image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)

        var p = 0
        for (i in 0 until imageSize) {
            for (j in 0 until imageSize) {
                val v = intValues[p++]

                val r = ((v shr 16) and 0xFF).toFloat()
                val g = ((v shr 8) and 0xFF).toFloat()
                val b = (v and 0xFF).toFloat()

                buffer.putFloat(r)
                buffer.putFloat(g)
                buffer.putFloat(b)
            }
        }
        inputTensor.loadBuffer(buffer)

        val output = Array(1) { FloatArray(classes.size) }

        interpreter.run(inputTensor.buffer, output)

        val conf = output[0]

        var maxIdx = 0
        var maxVal = Float.NEGATIVE_INFINITY
        for (i in conf.indices) {
            if (conf[i] > maxVal) {
                maxVal = conf[i]
                maxIdx = i
            }
        }

        return classes[maxIdx]
    }

    private fun goToResult(imageUri: Uri, resultLabel: String) {
        startActivity(Intent(this, ResultActivity::class.java).apply {
            putExtra(ResultActivity.EXTRA_IMAGE_URI, imageUri.toString())
            putExtra(ResultActivity.EXTRA_RESULT_LABEL, resultLabel)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::interpreter.isInitialized) {
            interpreter.close()
        }
    }
}
