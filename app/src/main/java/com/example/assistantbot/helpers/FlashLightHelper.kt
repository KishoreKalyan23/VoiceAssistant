package com.example.voiceassistant.helpers

import android.content.Context
import android.hardware.camera2.CameraManager
import android.widget.Toast

object FlashlightHelper {

    fun toggleFlashlight(context: Context, turnOn: Boolean): String {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val flashAvailable = characteristics.get(
                    android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE
                ) == true
                val lensFacing = characteristics.get(
                    android.hardware.camera2.CameraCharacteristics.LENS_FACING
                )
                flashAvailable && lensFacing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK
            }

            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, turnOn)
                return if (turnOn) "Flashlight turned ON" else "Flashlight turned OFF"
            } else {
                return "No camera with flashlight found"
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            return "Error: ${e.message}"
        }
    }
}
