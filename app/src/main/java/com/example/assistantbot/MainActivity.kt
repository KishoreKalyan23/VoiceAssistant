package com.example.assistantbot

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.Locale

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var tvResult: TextView
    private lateinit var btnSpeak: Button
    private lateinit var tts: TextToSpeech

    @Suppress("PrivatePropertyName")
    private val REQUEST_CODE_SPEECH = 100
    @Suppress("PrivatePropertyName")
    private val REQUEST_PERMISSION_CODE = 200
    private lateinit var commandProcessor: CommandProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvResult = findViewById(R.id.tvResult)
        btnSpeak = findViewById(R.id.btnSpeak)

        // Request microphone permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_PERMISSION_CODE
            )
        }

        // Initialize Text-to-Speech
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
                commandProcessor = CommandProcessor(this, tts, this)
            }
        }

        // When button is clicked, start listening
        btnSpeak.setOnClickListener {
            startSpeechToText()
        }
    }

    private fun startSpeechToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something...")
        startActivityForResult(intent, REQUEST_CODE_SPEECH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = result?.get(0) ?: ""
            tvResult.text = spokenText
            commandProcessor.processCommand(spokenText.lowercase())
        }
    }

    override fun onDestroy() {
        if (tts.isSpeaking) tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}
