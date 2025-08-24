package com.example.assistantbot

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private val sampleRate = 16000
    private var recognizer: Recognizer? = null
    private var model: Model? = null
    private var audioRecord: AudioRecord? = null

    private lateinit var logTextView: TextView

    private var modelFolder = "vosk-model-small-en-us-0.15"

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
        private const val TAG = "VoskRecognizer"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logTextView = findViewById(R.id.logTextView)

        Log.i(TAG, "Application started")

        // Request microphone permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG, "No permission to RECORD_AUDIO")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSION_REQUEST_CODE
            )
        }else {
            startRecognition()
        }
    }

    private fun startRecognition() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Initialize Vosk library (required in some versions)
                LibVosk.setLogLevel(LogLevel.INFO)

                // Copy model from assets folder to internal storage
                val modelPath = "${filesDir.absolutePath}/model/${modelFolder}"
                copyAssetsFolder(this@MainActivity, "model/${modelFolder}", modelPath)

                // Now load the model from the internal storage path
                try {
                    model = Model(modelPath)
                    Log.i(TAG, "Model loaded successfully")
                } catch (e: Throwable) {
                    Log.e(TAG, "Failed to load model", e)
                }
                recognizer = Recognizer(model, sampleRate.toFloat())

                // Setup AudioRecord
                val bufferSize = AudioRecord.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                audioRecord = createAudioRecord(bufferSize)
                if (audioRecord == null) {
                    Log.e(TAG, "AudioRecord creation failed: RECORD_AUDIO permission is missing")
                    return@launch
                }
                audioRecord?.startRecording()

                val buffer = ByteArray(bufferSize)

                while (true) {
                    val  read= audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        if (recognizer?.acceptWaveForm(buffer, read) == true) {
                            val result = recognizer?.result

                            if(result != null && result.isNotEmpty())
                            {
                                val jsonObject = JSONObject(result)
                                val text = jsonObject.getString("text")

                               if(text.isNotEmpty()){
                                   withContext(Dispatchers.Main) {
                                       logTextView.text = text
                                   }
                                   Log.i(TAG, "Result: $text")
                               }
                            }
                        }
//                        else {
//                            val partialResult = recognizer?.partialResult
//                            Log.i(TAG, "Partial: $partialResult")
//                        }
                    }
                }
            } catch (e: Exception) {
                Log.i(TAG, "Error during recognition", e)
            } finally {
                recognizer?.close()
                model?.close()
                audioRecord?.stop()
                audioRecord?.release()
            }
        }
    }

    private fun createAudioRecord(bufferSize: Int): AudioRecord? {
        return if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED) {

            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
        } else {
            // Permission not granted; handle appropriately or return null
            null
        }
    }

    /**
     * Recursively copies all files and folders from an assets folder to a destination folder.
     */
    private fun copyAssetsFolder(context: Context, assetFolder: String, destinationPath: String) {
        val assetManager = context.assets
        val files = assetManager.list(assetFolder) ?: return

        val destFolder = File(destinationPath)
        if (!destFolder.exists()) {
            destFolder.mkdirs()
        }

        for (filename in files) {
            val assetPath = "$assetFolder/$filename"
            val destFilePath = "$destinationPath/$filename"

            // Check if this is a folder or a file
            val list = assetManager.list(assetPath)
            if (list != null && list.isNotEmpty()) {
                // It's a directory, recurse
                copyAssetsFolder(context, assetPath, destFilePath)
            } else {
                // It's a file, copy it
                assetManager.open(assetPath).use { inputStream ->
                    FileOutputStream(destFilePath).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }
    }
}
