package com.alyona.rustore.ui.theme

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alyona.rustore.R
import com.alyona.rustore.ui.theme.models.ApplicationItem
import com.alyona.rustore.ui.theme.network.ApiConfig
import com.bumptech.glide.Glide
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AppsAdapter(
    private val onClick: ((ApplicationItem) -> Unit)? = null
) : RecyclerView.Adapter<AppsAdapter.ViewHolder>() {

    private var apps = listOf<ApplicationItem>()
    private var shownApps = listOf<ApplicationItem>()

    fun submitList(list: List<ApplicationItem>) {
        apps = list
        shownApps = list
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
    }
}