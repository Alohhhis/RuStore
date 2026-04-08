package com.alyona.rustore.ui.theme

import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.alyona.rustore.R
import com.alyona.rustore.ui.theme.network.ApiConfig
import com.bumptech.glide.Glide

class ScreenshotsAdapter(
    private val screenshots: List<String>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<ScreenshotsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: ImageView) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                onClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val image = ImageView(parent.context)
        val width = (parent.resources.displayMetrics.density * 160).toInt()
        val height = (parent.resources.displayMetrics.density * 300).toInt()
        image.layoutParams = RecyclerView.LayoutParams(width, height)
        image.scaleType = ImageView.ScaleType.CENTER_CROP
        image.setPadding(4, 4, 4, 4)
        return ViewHolder(image)
    }

    override fun getItemCount() = screenshots.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fullUrl = ApiConfig.absoluteUrl(screenshots[position])
        Log.d("GlideDebug", "Loading screenshot: $fullUrl")
        Glide.with(holder.itemView.context)
            .load(fullUrl)
            .placeholder(R.drawable.mini_logo_foreground)
            .into(holder.itemView as ImageView)
    }
}