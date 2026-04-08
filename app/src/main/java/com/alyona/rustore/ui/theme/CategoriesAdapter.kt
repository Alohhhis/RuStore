package com.alyona.rustore.ui.theme

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alyona.rustore.R
import com.alyona.rustore.ui.theme.models.ApplicationItem

class CategoriesAdapter(
    private val context: Context,
    val categories: List<Category>,
    var allApps: List<ApplicationItem>
) : RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.category_icon)
        val name: TextView = itemView.findViewById(R.id.category_name)
        val count: TextView = itemView.findViewById(R.id.category_count)

        init {
            itemView.setOnClickListener {
                val categoryName = categories[adapterPosition].name
                // Переход на экран приложений с фильтром по категории
                val intent = Intent(context, AppStoreActivity::class.java)
                intent.putExtra("CATEGORY_FILTER", categoryName)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = categories.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.icon.setImageResource(category.iconRes)
        holder.name.text = category.name
        holder.count.text = category.count.toString()
    }

    fun updateCounts() {
        categories.forEach { cat ->
            cat.count = if (cat.name == "Все приложения") {
                allApps.size
            } else {
                allApps.count { it.category == cat.name }
            }
        }
        notifyDataSetChanged()
    }
}