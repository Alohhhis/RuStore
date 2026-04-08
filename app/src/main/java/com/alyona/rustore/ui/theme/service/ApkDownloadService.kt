package com.alyona.rustore.ui.theme.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.alyona.rustore.R
import com.alyona.rustore.ui.theme.models.DownloadTaskCreateRequest
import com.alyona.rustore.ui.theme.network.ApiConfig
import com.alyona.rustore.ui.theme.network.RetrofitInstance
import kotlinx.coroutines.*
import kotlinx.coroutines.currentCoroutineContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class ApkDownloadService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val okHttp by lazy { OkHttpClient() }

    @Volatile
    private var currentTaskId: String? = null
    @Volatile
    private var currentAppId: Int? = null

    companion object {
        const val CHANNEL_ID = "apk_download_channel"
        const val EXTRA_APP_ID = "app_id"
        const val EXTRA_APP_NAME = "app_name"
        const val EXTRA_APK_URL = "apk_url"
        const val EXTRA_TASK_ID = "task_id"
        const val ACTION_CANCEL = "action_cancel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CANCEL) {
            val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: currentTaskId
            val appId = intent.getIntExtra(EXTRA_APP_ID, -1).takeIf { it > 0 } ?: currentAppId
            serviceScope.launch {
                try {
                    if (taskId != null) {
                        RetrofitInstance.api.cancelTask(taskId)
                    }
                } finally {
                    if (appId != null) {
                        DownloadTaskStore.upsert(
                            DownloadUiState(
                                appId = appId,
                                taskId = taskId ?: "",
                                status = "canceled",
                                progress = 0
                            )
                        )
                    }
                    stopSelf()
                }
            }
            return START_NOT_STICKY
        }

        val appId = intent?.getIntExtra(EXTRA_APP_ID, -1) ?: -1
        val appName = intent?.getStringExtra(EXTRA_APP_NAME) ?: "app"
        val apkUrl = intent?.getStringExtra(EXTRA_APK_URL)
        if (appId <= 0) return START_NOT_STICKY
        if (apkUrl.isNullOrBlank()) {
            DownloadTaskStore.upsert(
                DownloadUiState(
                    appId = appId,
                    taskId = "",
                    status = "failed",
                    progress = 0,
                    errorMessage = "apkUrl не передан в сервис"
                )
            )
            stopSelf()
            return START_NOT_STICKY
        }

        currentAppId = appId

        createNotificationChannel()

        startForeground(
            NOTIFICATION_ID,
            buildNotification(
                appName = appName,
                content = "Подготовка...",
                progress = null,
                appId = appId,
                taskId = null,
                localApkPath = null

            )
        )

        serviceScope.launch {
            runDownloadFlow(appId = appId, appName = appName, apkUrl = apkUrl)
            stopSelf()
        }

        return START_STICKY
    }

    private suspend fun runDownloadFlow(appId: Int, appName: String, apkUrl: String) = coroutineScope {
        try {
            val created = RetrofitInstance.api.createDownloadTask(
                DownloadTaskCreateRequest(
                    appId = appId,
                    apkUrl = apkUrl
                )
            )
            currentTaskId = created.taskId

            DownloadTaskStore.upsert(
                DownloadUiState(
                    appId = appId,
                    taskId = created.taskId,
                    status = "pending",
                    progress = 0
                )
            )

            val start = SystemClock.elapsedRealtime()

            while (currentCoroutineContext().isActive) {
                val status = RetrofitInstance.api.getTaskStatus(created.taskId)
                DownloadTaskStore.upsert(
                    DownloadUiState(
                        appId = appId,
                        taskId = created.taskId,
                        status = status.status,
                        progress = status.progress,
                        resultUrl = status.resultUrl
                    )
                )

                buildAndNotify(
                    appName = appName,
                    content = when (status.status) {
                        "completed" -> "Загрузка на сервере завершена"
                        "failed" -> "Ошибка на сервере"
                        "canceled" -> "Отменено"
                        else -> "Загрузка: ${status.progress}%"
                    },
                    progress = if (status.status == "downloading" || status.status == "pending") status.progress else null,
                    appId = appId,
                    taskId = created.taskId
                )

                when (status.status) {
                    "completed" -> break
                    "failed", "canceled" -> return@coroutineScope
                }

                val elapsed = SystemClock.elapsedRealtime() - start
                val delayMs = if (elapsed < 10_000) 700L else 1200L
                delay(delayMs)
            }

            val result = RetrofitInstance.api.getTaskResult(created.taskId)
            val rawPath = result.resultUrl ?: result.apkPath ?: RetrofitInstance.api.getTaskStatus(created.taskId).resultUrl
            val resultUrl = rawPath?.let { normalizeResultPathToUrl(it) }
            if (resultUrl.isNullOrBlank()) {
                DownloadTaskStore.upsert(
                    DownloadUiState(
                        appId = appId,
                        taskId = created.taskId,
                        status = "failed",
                        progress = 100,
                        errorMessage = "Путь к APK не пришёл"
                    )
                )
                return@coroutineScope
            }

            val apkFile = downloadResultApk(
                url = ApiConfig.absoluteUrl(resultUrl),
                fileName = "${sanitizeFileName(appName)}.apk"
            )

            DownloadTaskStore.upsert(
                DownloadUiState(
                    appId = appId,
                    taskId = created.taskId,
                    status = "apk_downloaded",
                    progress = 100,
                    resultUrl = resultUrl,
                    localApkPath = apkFile.absolutePath
                )
            )

            // ВАЖНО: установка должна быть user-initiated.
            // Поэтому после скачивания только показываем действие "Установить".
            buildAndNotify(
                appName = appName,
                content = "APK скачан. Нажмите «Установить».",
                progress = null,
                appId = appId,
                taskId = created.taskId,
                localApkPath = apkFile.absolutePath
            )

        } catch (e: Exception) {
            val taskId = currentTaskId ?: ""
            DownloadTaskStore.upsert(
                DownloadUiState(
                    appId = appId,
                    taskId = taskId,
                    status = "failed",
                    progress = 0,
                    errorMessage = e.message
                )
            )
            buildAndNotify(
                appName = appName,
                content = "Ошибка: ${e.message ?: "неизвестно"}",
                progress = null,
                appId = appId,
                taskId = taskId,
                localApkPath = null
            )
        }
    }

    private fun sanitizeFileName(name: String): String =
        name.trim().replace(Regex("""[\\/:*?"<>|]"""), "_").ifBlank { "app" }

    private fun normalizeResultPathToUrl(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
        if (trimmed.startsWith("/")) return trimmed

        // бек иногда может вернуть абсолютный путь ФС сервера ".../downloads/<id>.apk"
        val marker = "downloads"
        val idx = trimmed.lastIndexOf(marker)
        if (idx >= 0) {
            val tail = trimmed.substring(idx + marker.length).trimStart('\\', '/')
            return "/downloads/$tail"
        }

        // иначе считаем, что это относительный путь внутри статики
        return "/downloads/$trimmed"
    }

    private suspend fun downloadResultApk(url: String, fileName: String): File = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).get().build()
        val response = okHttp.newCall(request).execute()
        if (!response.isSuccessful) throw IllegalStateException("APK download failed: ${response.code}")
        val body = response.body ?: throw IllegalStateException("Empty body")

        // ВАЖНО: кладём в filesDir/apk, чтобы FileProvider (files-path) гарантированно видел файл.
        val apkDir = File(filesDir, "apk").apply { mkdirs() }
        val outFile = File(apkDir, fileName)

        body.byteStream().use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        }
        outFile
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "APK Downloads",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    // безопасный вызов notify с проверкой разрешений
    private fun notifySafe(notificationId: Int, notification: Notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    NotificationManagerCompat.from(this).notify(notificationId, notification)
                }
            } else {
                NotificationManagerCompat.from(this).notify(notificationId, notification)
            }
        } catch (_: SecurityException) {
            // permission может быть отозвано пользователем — не падаем
        }
    }

    private fun buildNotification(
        appName: String,
        content: String,
        progress: Int?,
        appId: Int,
        taskId: String?,
        localApkPath: String?,
    ): Notification {
        val cancelIntent = Intent(this, ApkDownloadService::class.java).apply {
            action = ACTION_CANCEL
            putExtra(EXTRA_APP_ID, appId)
            if (taskId != null) putExtra(EXTRA_TASK_ID, taskId)
        }
        val cancelPending = PendingIntent.getService(
            this,
            0,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(appName)
            .setContentText(content)
            .setSmallIcon(R.drawable.mini_logo_foreground)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Отменить", cancelPending)

        if (!localApkPath.isNullOrBlank()) {
            val installIntent = Intent(this, InstallApkActivity::class.java).apply {
                putExtra(InstallApkActivity.EXTRA_APK_PATH, localApkPath)
            }
            val installPending = PendingIntent.getActivity(
                this,
                1,
                installIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.stat_sys_download_done, "Установить", installPending)
        }

        if (progress != null) {
            builder.setProgress(100, progress.coerceIn(0, 100), false)
        } else {
            builder.setProgress(0, 0, false)
        }

        return builder.build()
    }

    private fun buildAndNotify(
        appName: String,
        content: String,
        progress: Int?,
        appId: Int,
        taskId: String?,
        localApkPath: String? = null,
    ) {
        val notification = buildNotification(appName, content, progress, appId, taskId, localApkPath)
        notifySafe(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}