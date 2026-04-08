package com.alyona.rustore.ui.theme

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alyona.rustore.R
import com.alyona.rustore.ui.theme.models.ApplicationItem
import com.alyona.rustore.ui.theme.service.ApkDownloadService
import com.bumptech.glide.Glide
import kotlinx.serialization.json.Json
import com.alyona.rustore.ui.theme.network.ApiConfig
import com.alyona.rustore.ui.theme.util.NotificationPermission

class AppCardActivity : AppCompatActivity() {

    private lateinit var longDescription: TextView
    private lateinit var readMoreButton: Button
    private var isExpanded = false
    private var pendingStartIntent: Intent? = null

    private val requestNotificationsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            pendingStartIntent?.let { startDownloadService(it) }
            pendingStartIntent = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_card)

        // --- UI элементы ---
        val appName: TextView = findViewById(R.id.app_name)
        val appShortDescription: TextView = findViewById(R.id.app_short_description)
        val appDeveloper: TextView = findViewById(R.id.app_developer)
        val appAge: TextView = findViewById(R.id.app_age)
        val appIcon: ImageView = findViewById(R.id.app_icon)
        val installButton: Button = findViewById(R.id.install_button)
        val logo: ImageView = findViewById(R.id.logo)
        val searchInput: EditText = findViewById(R.id.search_input)
        val searchContainer: View = findViewById(R.id.search_container)
        val categoriesList = findViewById<RecyclerView>(R.id.app_categories)
        val screenshotsList = findViewById<RecyclerView>(R.id.screenshots_list)
        longDescription = findViewById(R.id.app_long_description)
        readMoreButton = findViewById(R.id.read_more_button)

        // --- Получаем объект ApplicationItem из Intent ---
        val appJson = intent.getStringExtra("APP_ITEM_JSON")
        val appItem: ApplicationItem = if (appJson != null) {
            Json.decodeFromString(appJson)
        } else {
            finish(); return
        }

        // --- Заполняем UI ---
        appName.text = appItem.name
        appShortDescription.text = appItem.shortDescription
        appDeveloper.text = appItem.developer
        appAge.text = appItem.ageRating
        longDescription.text = appItem.fullDescription
        updateReadMoreVisibility()

        val iconFullUrl = ApiConfig.absoluteUrl(appItem.iconUrl)
        Log.d("GlideDebug", "Loading icon: $iconFullUrl")
        Glide.with(this)
            .load(iconFullUrl)
            .placeholder(R.drawable.mini_logo_foreground)
            .into(appIcon)

        // --- "Читать дальше / Свернуть" ---
        readMoreButton.setOnClickListener {
            isExpanded = !isExpanded
            longDescription.maxLines = if (isExpanded) Int.MAX_VALUE else 4
            readMoreButton.text = if (isExpanded) "Свернуть" else "Читать дальше"
        }

        // --- Поиск ---
        searchInput.isFocusable = false
        searchInput.isFocusableInTouchMode = false
        searchInput.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this, android.R.anim.slide_in_left, android.R.anim.fade_out
            )
            startActivity(intent, options.toBundle())
        }
        searchContainer.setOnClickListener { searchInput.performClick() }

        // --- Кнопка установки APK ---
        installButton.setOnClickListener {
            val startIntent = Intent(this, ApkDownloadService::class.java).apply {
                putExtra(ApkDownloadService.EXTRA_APP_ID, appItem.id)
                putExtra(ApkDownloadService.EXTRA_APP_NAME, appItem.name)
                putExtra(ApkDownloadService.EXTRA_APK_URL, ApiConfig.absoluteUrl(appItem.apkUrl))
            }

            if (!NotificationPermission.isGranted(this)) {
                pendingStartIntent = startIntent
                NotificationPermission.requestIfNeeded(this, requestNotificationsPermission)
            } else {
                startDownloadService(startIntent)
            }
        }

        // --- Категории приложения ---
        val categoryIconRes = getCategoryIcon(appItem.category)
        val categoryList = listOf(AppCardCategoryAdapter.CategoryItem(appItem.category, categoryIconRes))
        categoriesList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        categoriesList.adapter = AppCardCategoryAdapter(this, categoryList)

        // --- Скриншоты ---
        screenshotsList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        screenshotsList.adapter = ScreenshotsAdapter(appItem.screenshots) { clickedIndex ->
            val dialog = ScreenshotDialogFragment(appItem.screenshots, clickedIndex)
            dialog.show(supportFragmentManager, "screenshots")
        }

        // --- Логотип закрывает экран ---
        logo.setOnClickListener { finish() }
    }

    private fun updateReadMoreVisibility() {
        longDescription.post {
            readMoreButton.visibility = if (longDescription.lineCount > 4) View.VISIBLE else View.GONE
        }
    }

    private fun getCategoryIcon(category: String): Int = when(category) {
        "Финансы" -> R.drawable.finance
        "Государственные" -> R.drawable.government
        "Инструменты" -> R.drawable.tools
        "Транспорт" -> R.drawable.transport
        "Игры" -> R.drawable.games
        else -> R.drawable.all_apps
    }

    private fun startDownloadService(intent: Intent) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}