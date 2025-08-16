package com.example.assistantbot.helpers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

object OpenAppHelper {

    fun openAppByName(context: Context, appName: String): Pair<String, Intent?> {
        val pm: PackageManager = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        var packageName: String? = null
        var answerCommand: String

        for (app in packages) {
            val label = pm.getApplicationLabel(app).toString()
            if (label.equals(appName, ignoreCase = true)) {
                packageName = app.packageName
                break
            }
        }

        var launchIntent: Intent? = null

        if (packageName != null) {
            launchIntent = pm.getLaunchIntentForPackage(packageName)
            answerCommand = if (launchIntent != null) {
                "Opening $appName"
            } else {
                "Unable to open $appName"
            }
        } else {
            answerCommand = "$appName not found"
        }

        return Pair(answerCommand, launchIntent)
    }

}
