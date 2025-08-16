package com.example.assistantbot.helpers

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.provider.ContactsContract
import androidx.core.net.toUri

class CallHelper(private val context: Context) {

    fun makePhoneCall(number: String): Intent {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = "tel:$number".toUri()
        //context.startActivity(intent)
        return intent
    }

    fun extractPhoneNumber(command: String): String? {
        val cleaned = command.replace("[^0-9]".toRegex(), "")
        return if (cleaned.length >= 3) cleaned else null
    }

    fun extractContactName(command: String, triggers: List<String>): String? {
        val cleanCommand = triggers.fold(command) { acc, trigger ->
            acc.replace(trigger, "", ignoreCase = true)
        }
         cleanCommand.trim().takeIf { it.isNotEmpty() }

        return normalizeSpelledName(cleanCommand)
    }

    private fun normalizeSpelledName(text: String): String {
        val cleaned = text
            .trim()
            .lowercase()
            .replace("-", " ")
            .replace("\\s+".toRegex(), " ") // collapse multiple spaces

        val tokens = cleaned.split(" ")

        // Case 1: If ALL tokens are single letters, join them -> "m a t h a n" -> "mathan"
        if (tokens.all { it.length == 1 && it[0].isLetter() } && tokens.size > 1) {
            return tokens.joinToString("")
        }

        // Case 2: If it’s a normal multi-word name ("john gabbi"), keep as-is
        return cleaned
    }

    fun getPhoneNumberFromName(name: String): String? {
        val contentResolver = context.contentResolver
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$name%")

        var phoneNumber: String? = null
        val cursor: Cursor? = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (it.moveToFirst()) {
                phoneNumber = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
            }
        }
        return phoneNumber
    }

}
