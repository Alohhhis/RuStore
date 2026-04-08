package com.alyona.rustore.ui.theme

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alyona.rustore.R
import com.alyona.rustore.ui.theme.DebugLogger

class CategoryFilterAdapter(
    private val context: Context,
    private val items: List<UiItem>,
    private val onMoreCategoriesClicked: () -> Unit,
    private val onSelectionChanged: (Set<String>) -> Unit,
    initialSelectedCategories: Set<String> = emptySet(),
) : RecyclerView.Adapter<CategoryFilterAdapter.ViewHolder>() {

    private var selectedCategories: Set<String> = initialSelectedCategories

    sealed class UiItem {
        data class Chip(
            val categoryName: String,
            val iconResUnselected: Int,
            val iconResSelected: Int
        ) : UiItem()

        data object MoreCategories : UiItem()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.filter_icon)
        val text: TextView = itemView.findViewById(R.id.filter_text)
        val check: ImageView = itemView.findViewById(R.id.filter_check)
        val root: View = itemView
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is UiItem.Chip -> 0
            is UiItem.MoreCategories -> 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_filter_chip_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        when (item) {
            is UiItem.Chip -> {
                val isSelected = (selectedCategories.isEmpty() && item.categoryName == "Все приложения") ||
                        selectedCategories.contains(item.categoryName)

                holder.icon.setImageResource(
                    if (isSelected) item.iconResSelected else item.iconResUnselected
                )

                holder.text.text = item.categoryName
                holder.check.visibility = if (isSelected) View.VISIBLE else View.GONE
                holder.root.setBackgroundResource(
                    if (isSelected) R.drawable.filter_chip_selected_bg else R.drawable.filter_chip_unselected_bg
                )
                holder.text.setTextColor(
                    if (isSelected) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                )

                holder.root.setOnClickListener {
                    toggleChip(item.categoryName)
                }
            }

            is UiItem.MoreCategories -> {
                holder.icon.setImageResource(R.drawable.to_all_categories)
                holder.check.visibility = View.GONE
                holder.text.text = "Ко всем категориям"
                holder.text.setTextColor(android.graphics.Color.BLACK)
                holder.root.setBackgroundResource(R.drawable.filter_chip_unselected_bg)
                holder.root.setOnClickListener { onMoreCategoriesClicked.invoke() }
            }
        }
    }

    private fun toggleChip(categoryName: String) {
        val updated = if (categoryName == "Все приложения") {
            emptySet()
        } else {
            val mutable = selectedCategories.toMutableSet()
            if (mutable.contains(categoryName)) mutable.remove(categoryName) else mutable.add(categoryName)
            mutable
        }

        selectedCategories = updated

        DebugLogger.log(
            runId = "initial",
            hypothesisId = "H_FILTER_SELECTION",
            location = "CategoryFilterAdapter.kt:toggleChip",
            message = "Filter selection changed",
            data = """{"selectedCount":${selectedCategories.size},"selected":[${selectedCategories.joinToString(",") { "\"$it\"" }}]}"""
        )

        onSelectionChanged.invoke(selectedCategories)
        notifyDataSetChanged()
    }

    companion object {
        fun attachItemSpacing(recyclerView: RecyclerView, spacingDp: Int) {
            val spacingPx = (spacingDp * recyclerView.context.resources.displayMetrics.density).toInt()
            recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.right = spacingPx
                    if (parent.getChildAdapterPosition(view) == 0) {
                        outRect.left = spacingPx
                    }
                }
            })
        }
    }
}