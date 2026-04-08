package com.alyona.rustore.ui.theme

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alyona.rustore.R
import com.alyona.rustore.ui.theme.models.ApplicationItem
import com.alyona.rustore.ui.theme.network.ApiConfig
import com.bumptech.glide.Glide
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.alyona.rustore.ui.theme.service.DownloadUiState

class AppsAdapter(
    private val onClick: ((ApplicationItem) -> Unit)? = null
) : RecyclerView.Adapter<AppsAdapter.ViewHolder>() {

    private var apps = listOf<ApplicationItem>()
    private var shownApps = listOf<ApplicationItem>()
    private var downloadStates: Map<Int, DownloadUiState> = emptyMap()

    fun submitList(list: List<ApplicationItem>) {
        apps = list
        shownApps = list
        notifyDataSetChanged()
    }

    fun submitDownloadStates(states: Map<Int, DownloadUiState>) {
        downloadStates = states
        notifyDataSetChanged()
    }

    fun filterByCategory(category: String) {
        filterByCategories(if (category == "Все приложения") emptySet() else setOf(category))
    }

    fun filterByCategories(selectedCategories: Set<String>) {
        shownApps = if (selectedCategories.isEmpty()) {
            apps
        } else {
            apps.filter { selectedCategories.contains(it.category) }
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.app_name)
        val icon: ImageView = itemView.findViewById(R.id.app_icon)
        val description: TextView = itemView.findViewById(R.id.app_description)
        val category: TextView = itemView.findViewById(R.id.app_categories)
        val downloadButton: Button = itemView.findViewById(R.id.download_button)
        val downloadProgress: ProgressBar = itemView.findViewById(R.id.download_progress)

        init {
            itemView.setOnClickListener {
                val context = itemView.context
                val app = shownApps[bindingAdapterPosition]

                // Логирование клика
                DebugLogger.log(
                    runId = "initial",
                    hypothesisId = "H_APP_CLICK",
                    location = "AppsAdapter.kt:onClick",
                    message = "App item clicked",
                    data = """{"appId":"${app.id}","appName":"${app.name}","appCategory":"${app.category}"}"""
                )

                if (onClick != null) {
                    onClick.invoke(app)
                } else {
                    // AppCardActivity ожидает объект приложения целиком (в виде JSON),
                    // иначе часть полей может "потеряться", а экран просто закроется.
                    val appJson = Json.encodeToString(app)
                    val intent = Intent(context, AppCardActivity::class.java).apply {
                        putExtra("APP_ITEM_JSON", appJson)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.app_card_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = shownApps.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = shownApps[position]
        holder.title.text = app.name
        holder.description.text = app.shortDescription
        holder.category.text = app.category

        val iconFullUrl = ApiConfig.absoluteUrl(app.iconUrl)
        Log.d("GlideDebug", "Loading icon: ${iconFullUrl}")

        Glide.with(holder.icon.context)
            .load(iconFullUrl)
            .placeholder(R.drawable.mini_logo_foreground)
            .into(holder.icon)

        val state = downloadStates[app.id]
        if (state == null || state.status == "failed" || state.status == "canceled") {
            holder.downloadProgress.visibility = View.GONE
            holder.downloadButton.isEnabled = true
            holder.downloadButton.text = "Скачать"
        } else {
            when (state.status) {
                "pending", "downloading" -> {
                    holder.downloadProgress.visibility = View.VISIBLE
                    holder.downloadProgress.progress = state.progress.coerceIn(0, 100)
                    holder.downloadButton.isEnabled = true
                    holder.downloadButton.text = "Отменить"
                }
                "completed", "apk_downloaded" -> {
                    holder.downloadProgress.visibility = View.GONE
                    holder.downloadButton.isEnabled = !state.localApkPath.isNullOrBlank()
                    holder.downloadButton.text = if (state.localApkPath.isNullOrBlank()) "Готово" else "Установить"
                }
                else -> {
                    holder.downloadProgress.visibility = View.GONE
                    holder.downloadButton.isEnabled = true
                    holder.downloadButton.text = state.status
                }
            }
        }

        holder.downloadButton.setOnClickListener {
            val context = holder.itemView.context
            if (state != null && (state.status == "pending" || state.status == "downloading")) {
                val cancelIntent = Intent(context, com.alyona.rustore.ui.theme.service.ApkDownloadService::class.java)
                    .apply {
                        action = com.alyona.rustore.ui.theme.service.ApkDownloadService.ACTION_CANCEL
                        putExtra(com.alyona.rustore.ui.theme.service.ApkDownloadService.EXTRA_APP_ID, app.id)
                        putExtra(com.alyona.rustore.ui.theme.service.ApkDownloadService.EXTRA_TASK_ID, state.taskId)
                    }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(cancelIntent)
                } else {
                    context.startService(cancelIntent)
                }
            } else {
                if (state != null && state.status == "apk_downloaded" && !state.localApkPath.isNullOrBlank()) {
                    val installIntent = Intent(context, com.alyona.rustore.ui.theme.service.InstallApkActivity::class.java).apply {
                        putExtra(com.alyona.rustore.ui.theme.service.InstallApkActivity.EXTRA_APK_PATH, state.localApkPath)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(installIntent)
                    return@setOnClickListener
                }
                val startIntent = Intent(context, com.alyona.rustore.ui.theme.service.ApkDownloadService::class.java).apply {
                    putExtra(com.alyona.rustore.ui.theme.service.ApkDownloadService.EXTRA_APP_ID, app.id)
                    putExtra(com.alyona.rustore.ui.theme.service.ApkDownloadService.EXTRA_APP_NAME, app.name)
                    putExtra(com.alyona.rustore.ui.theme.service.ApkDownloadService.EXTRA_APK_URL, ApiConfig.absoluteUrl(app.apkUrl))
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(startIntent)
                } else {
                    context.startService(startIntent)
                }
            }
        }

    }
}