package com.alyona.rustore.ui.theme.util

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.alyona.rustore.R

class ErrorStateView(private val root: View) {
    private val message: TextView = root.findViewById(R.id.error_message)
    private val retry: Button = root.findViewById(R.id.error_retry)

    fun show(messageText: String, onRetry: () -> Unit) {
        message.text = messageText
        retry.setOnClickListener { onRetry() }
        root.visibility = View.VISIBLE
    }

    fun hide() {
        root.visibility = View.GONE
    }
}

