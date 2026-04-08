package com.alyona.rustore.ui.theme

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.alyona.rustore.R
import com.alyona.rustore.ui.theme.network.ApiConfig
import com.bumptech.glide.Glide

class ScreensViewPagerAdapter(
    private val screenshots: List<String>
) : RecyclerView.Adapter<ScreensViewPagerAdapter.ViewHolder>() {

    inner class ViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val imageView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_screenshot, parent, false) as ImageView
        imageView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        return ViewHolder(imageView)
    }

    override fun getItemCount() = screenshots.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fullUrl = ApiConfig.absoluteUrl(screenshots[position])
        Log.d("GlideDebug", "Loading screenshot: $fullUrl")
        Glide.with(holder.imageView.context)
            .load(fullUrl)
            .placeholder(R.drawable.mini_logo_foreground)
            .into(holder.imageView)
    }
}