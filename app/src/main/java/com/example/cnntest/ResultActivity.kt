package com.example.cnntest

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val img = findViewById<ImageView>(R.id.ivhasil)
        val tvResult = findViewById<TextView>(R.id.tvHasil)
        val tvKandungan = findViewById<TextView>(R.id.tvKandungan)
        val btnBack = findViewById<Button>(R.id.buttonselesai)

        val imageUri = intent.getStringExtra(EXTRA_IMAGE_URI)?.let { Uri.parse(it) }
        val label = intent.getStringExtra(EXTRA_RESULT_LABEL).orEmpty()

        if (imageUri != null) img.setImageURI(imageUri)
        tvResult.text = label.capitalizeFirst()

        tvKandungan.text = buildKandungan(label)

        btnBack.setOnClickListener { finish() }
    }

    private fun buildKandungan(label: String): String {
        return when (label.lowercase()) {
            "mentah" -> """
                • Kadar air (%): 89,38%
                • pH: 4,23
                • Aktivitas Antioksidan: 37,5
                • Likopen: 22,1 mg/kg
                • Vitamin C: 5,4 mg/100gr
                • Antimikroba (ZOI bakteri,mm): 10,7-11,6mm
                • Antijamur (ZOI fungi,mm): 11,2-11,8mm
            """.trimIndent()

            "setengah matang", "setengahmatang" -> """
                • Kadar air (%): 92,79%
                • pH: 4,50
                • Aktivitas Antioksidan: 37,6 s/d 43,5
                • Likopen: 26,0
                • Vitamin C: 17 mg/100gr
                • Antimikroba (ZOI bakteri,mm): 9,3-10,4mm
                • Antijamur (ZOI fungi,mm): 9,7-10,8mm
            """.trimIndent()

            "matang" -> """
                • Kadar air (%): 93,55%
                • pH: 4,77
                • Aktivitas Antioksidan: 43,6
                • Likopen: 33 mg/kg
                • Vitamin C: 14,5 mg/100gr
                • Antimikroba (ZOI bakteri,mm): 9,0-9,3mm
                • Antijamur (ZOI fungi,mm): 9,3-9,8mm
            """.trimIndent()

            else -> "Kandungan: (tidak tersedia untuk label: $label)"
        }
    }

    private fun String.capitalizeFirst(): String =
        replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_RESULT_LABEL = "extra_result_label"
    }
}
