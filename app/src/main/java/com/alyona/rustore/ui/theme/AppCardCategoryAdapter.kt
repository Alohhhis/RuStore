package com.alyona.rustore.ui.theme

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alyona.rustore.R

class AppCardCategoryAdapter(
    private val context: Context,
    private val categories: List<CategoryItem>
) : RecyclerView.Adapter<AppCardCategoryAdapter.ViewHolder>() {

    data class CategoryItem(
        val name: String,
        val iconRes: Int
    )

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.filter_icon)
        val text: TextView = itemView.findViewById(R.id.filter_text)
        val root: View = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_filter_chip_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = categories.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.icon.setImageResource(category.iconRes)
        holder.text.text = category.name
        holder.root.setBackgroundResource(R.drawable.filter_chip_unselected_bg)
        holder.text.setTextColor(android.graphics.Color.BLACK)
        holder.root.setOnClickListener(null) // убираем кликабельность
    }
}