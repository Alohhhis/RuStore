package com.alyona.rustore.ui.theme

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.alyona.rustore.R

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding) // подключаем твой XML-макет
        // region agent log H2
        DebugLogger.log(
            runId = "initial",
            hypothesisId = "H2",
            location = "OnboardingActivity.kt:onCreate",
            message = "Onboarding created"
        )
        // endregion

        val startButton: Button = findViewById(R.id.start_button)
        startButton.post {
            // region agent log H6
            DebugLogger.log(
                runId = "initial",
                hypothesisId = "H6",
                location = "OnboardingActivity.kt:startButton.post",
                message = "Start button layout/runtime state",
                data = """{"x":${startButton.x},"y":${startButton.y},"width":${startButton.width},"height":${startButton.height},"isShown":${startButton.isShown},"isClickable":${startButton.isClickable},"isEnabled":${startButton.isEnabled}}"""
            )
            // endregion
        }
        startButton.setOnClickListener {
            // region agent log H3
            DebugLogger.log(
                runId = "initial",
                hypothesisId = "H3",
                location = "OnboardingActivity.kt:startButton",
                message = "Start button clicked"
            )
             val intent = Intent(this, AppStoreActivity::class.java)
             startActivity(intent)
             finish()
        }

    }
}