package com.alyona.rustore.ui.theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2
import com.alyona.rustore.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ScreenshotDialogFragment(
    private val screenshots: List<String>,
    private val startPosition: Int = 0
) : DialogFragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var closeButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_screenshots, container, false)
        viewPager = view.findViewById(R.id.screenshot_viewpager)
        closeButton = view.findViewById(R.id.close_button)

        viewPager.adapter = ScreensViewPagerAdapter(screenshots)
        viewPager.setCurrentItem(startPosition, false)

        val indicator = view.findViewById<TabLayout>(R.id.screenshot_indicator)
        TabLayoutMediator(indicator, viewPager) { _, _ -> }.attach()

        closeButton.setOnClickListener { dismiss() }
        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}