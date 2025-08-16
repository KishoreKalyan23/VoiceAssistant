package com.example.assistantbot

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast

object LockHelper {

    fun lockScreen(context: Context, activity: android.app.Activity): String {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminName = ComponentName(context, MyDeviceAdminReceiver::class.java)
        var result: String?

        if (dpm.isAdminActive(adminName)) {
            dpm.lockNow()
            result = "Locking screen"
        } else {
            result = "Please enable Device Admin for this app"
            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
            enableDeviceAdmin(activity)
        }
        return result
    }

    fun enableDeviceAdmin(activity: android.app.Activity) {
        val componentName = ComponentName(activity, MyDeviceAdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "We need this permission to lock the screen.")
        activity.startActivityForResult(intent, 101)
    }
}
