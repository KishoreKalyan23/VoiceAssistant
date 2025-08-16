package com.example.assistantbot

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.assistantbot.helpers.CallHelper
import com.example.assistantbot.helpers.OpenAppHelper
import com.example.assistantbot.keys.callTriggers
import com.example.assistantbot.keys.cameraTriggers
import com.example.assistantbot.keys.flashlightOffTriggers
import com.example.assistantbot.keys.flashlightOnTriggers
import com.example.assistantbot.keys.lockTriggers
import com.example.assistantbot.keys.timeTriggers
import com.example.voiceassistant.helpers.FlashlightHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class CommandProcessor(private val context: Context, private val tts: TextToSpeech, private val activity: AppCompatActivity) {

    private var callHelper = CallHelper(context)
    fun processCommand(command: String) {
        try{
            var answerCommand: String?

            when {

                //Current Time
                timeTriggers.any { command.contains(it, ignoreCase = true) } -> {
                    val time = Calendar.getInstance().time.toString()
                    speak("The current time is $time")
                }

                //Open Camera
                cameraTriggers.any { command.contains(it, ignoreCase = true) } -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    openAppWithSpeech(activity, intent, "Opening camera")
                }

                //Make a Call
                callTriggers.any { command.contains(it, ignoreCase = true) } -> {
                    val phoneNumber = callHelper.extractPhoneNumber(command)
                    if (phoneNumber != null) {
                        val intent = callHelper.makePhoneCall(phoneNumber)
                        openAppWithSpeech(activity, intent, "Calling $phoneNumber")
                    } else {
                        val contactName = callHelper.extractContactName(command, callTriggers)
                        if (contactName != null) {
                            val contactNumber = callHelper.getPhoneNumberFromName(contactName)
                            if (contactNumber != null) {
                                val intent = callHelper.makePhoneCall(contactNumber)
                                openAppWithSpeech(activity, intent, "Calling $contactName")
                            } else {
                                speak("I couldn't find a contact named $contactName")
                            }
                        } else {
                            speak("Please say the phone number or contact name to call")
                        }
                    }
                }

                //Lock the Device (Need to work on this)
                lockTriggers.any { it in command.lowercase() } -> {
                    answerCommand = LockHelper.lockScreen(context, activity)
                    speak(answerCommand)
                }

                //Open Commanded App
                command.startsWith("open ", ignoreCase = true) ||
                        command.startsWith("launch ", ignoreCase = true) -> {
                    val appName = command.substringAfter(" ").trim()
                    val (message, intent) = OpenAppHelper.openAppByName(context, appName)
                    openAppWithSpeech(activity, intent, message)
                }

                flashlightOnTriggers.any { it in command } -> {
                    answerCommand = FlashlightHelper.toggleFlashlight(context, true)
                    speak(answerCommand)
                }
                flashlightOffTriggers.any { it in command } -> {
                    answerCommand = FlashlightHelper.toggleFlashlight(context, false)
                    speak(answerCommand)
                }


                else -> {
                    speak("I'm sorry, I didn't understand that command")
                }
            }

        }
        catch (e: Exception){
            e.printStackTrace()
            Log.e("VoiceAssistant", "Error getting time", e)

        }

    }

    fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun openAppWithSpeech(activity: AppCompatActivity, intent: Intent?, message: String) {

        speak(message)

        if(intent != null)
        {
            activity.lifecycleScope.launch {
                delay(500) // wait 0.5 second
                activity.startActivity(intent)
            }
        }
    }


}
