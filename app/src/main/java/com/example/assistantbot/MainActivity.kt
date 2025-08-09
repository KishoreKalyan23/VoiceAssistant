package com.example.assistantbot

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService

class MainActivity : AppCompatActivity(), RecognitionListener {

    private var speechService: SpeechService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            1
        )

        StorageService.unpack(this, "model", "model",
            { model ->
                val recognizer = Recognizer(model, 16000.0f)
                speechService = SpeechService(recognizer, 16000.0f)
                speechService!!.startListening(this)
            },
            { exception ->
                exception.printStackTrace()
            }
        )
    }

    override fun onPartialResult(hypothesis: String?) {
        // Partial speech result
    }

    override fun onResult(hypothesis: String?) {
        println("Final Result: $hypothesis")
    }

    override fun onFinalResult(hypothesis: String?) {
        println("Final Result: $hypothesis")
    }

    override fun onError(e: Exception?) {
        e?.printStackTrace()
    }

    override fun onTimeout() {}

}
