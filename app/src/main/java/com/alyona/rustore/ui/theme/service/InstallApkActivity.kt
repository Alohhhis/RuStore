package com.alyona.rustore.ui.theme.service

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File

/**
 * Отдельная Activity нужна, чтобы установка APK была user-initiated:
 * установка запускается только после явного клика по уведомлению/кнопке.
 */
class InstallApkActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_APK_PATH = "apk_path"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apkPath = intent.getStringExtra(EXTRA_APK_PATH)
        if (apkPath.isNullOrBlank()) {
            finish()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                startActivity(
                    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:$packageName")
                    }
                )
                finish()
                return
            }
        }

        val apkFile = File(apkPath)
        if (!apkFile.exists() || apkFile.length() <= 0L) {
            Toast.makeText(this, "APK не найден на устройстве. Скачайте ещё раз.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        val uri = FileProvider.getUriForFile(this, "${packageName}.provider", apkFile)

        val installIntent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
            data = uri
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(installIntent)
        finish()
    }
}

