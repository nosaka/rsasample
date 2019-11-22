package me.nosaka.rsaencrypt

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast

fun Context.showDialog(messageId: Int) {
    AlertDialog.Builder(this)
        .setMessage(messageId)
        .setPositiveButton(android.R.string.ok, null)
        .create()
        .show()
}

fun Context.showDialog(message: String) {
    AlertDialog.Builder(this)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, null)
        .create()
        .show()
}

fun Context.showToast(messageId: Int) {
    Toast.makeText(this, this.getText(messageId), Toast.LENGTH_SHORT)
        .show()
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT)
        .show()
}