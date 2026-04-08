package com.alyona.rustore.ui.theme

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.alyona.rustore.R
import com.alyona.rustore.ui.theme.viewmodel.AppStoreViewModel
import com.alyona.rustore.ui.theme.viewmodel.UiState
import com.alyona.rustore.ui.theme.util.ErrorStateView
import kotlinx.coroutines.launch

class AppStoreActivity : AppCompatActivity() {

    private val viewModel: AppStoreViewModel by viewModels()
    private lateinit var appsAdapter: AppsAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var initialCategoryFilter: String? = null
    private lateinit var errorStateView: ErrorStateView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.application_showcase)

        val logo: ImageView = findViewById(R.id.logo)
        val searchInput: EditText = findViewById(R.id.search_input)
        val categoriesRecycler = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.categories_list)
        val appsRecycler = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.apps_list)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        errorStateView = ErrorStateView(findViewById(R.id.error_state))

        initialCategoryFilter = intent.getStringExtra("CATEGORY_FILTER")

        logo.setOnClickListener { finish() }
        searchInput.isFocusable = false
        searchInput.isFocusableInTouchMode = false
        searchInput.setOnClickListener {
            startActivity(android.content.Intent(this, SearchActivity::class.java))
        }

        val categoryItems = listOf(
            CategoryFilterAdapter.UiItem.Chip(
                categoryName = "Все приложения",
                iconResUnselected = R.drawable.all_apps,
                iconResSelected = R.drawable.all_apps_selected
            ),
            CategoryFilterAdapter.UiItem.Chip(
                categoryName = "Финансы",
                iconResUnselected = R.drawable.finance,
                iconResSelected = R.drawable.finance_selected
            ),
            CategoryFilterAdapter.UiItem.Chip(
                categoryName = "Инструменты",
                iconResUnselected = R.drawable.tools,
                iconResSelected = R.drawable.tools_selected
            ),
            CategoryFilterAdapter.UiItem.Chip(
                categoryName = "Игры",
                iconResUnselected = R.drawable.games,
                iconResSelected = R.drawable.game_selected
            ),
            CategoryFilterAdapter.UiItem.Chip(
                categoryName = "Государственные",
                iconResUnselected = R.drawable.government,
                iconResSelected = R.drawable.government_selected
            ),
            CategoryFilterAdapter.UiItem.Chip(
                categoryName = "Транспорт",
                iconResUnselected = R.drawable.transport,
                iconResSelected = R.drawable.transport_selected
            ),
            CategoryFilterAdapter.UiItem.MoreCategories
        )

        val initialSelected = when {
            initialCategoryFilter.isNullOrBlank() -> emptySet()
            initialCategoryFilter == "Все приложения" -> emptySet()
            else -> setOf(initialCategoryFilter!!)
        }

        val categoryFilterAdapter = CategoryFilterAdapter(
            context = this,
            items = categoryItems,
            onMoreCategoriesClicked = {
                startActivity(android.content.Intent(this, CategoriesActivity::class.java))
            },
            onSelectionChanged = { selected ->
                appsAdapter.filterByCategories(selected)
            },
            initialSelectedCategories = initialSelected
        )

        categoriesRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        categoriesRecycler.adapter = categoryFilterAdapter
        CategoryFilterAdapter.attachItemSpacing(categoriesRecycler, spacingDp = 8)

        appsAdapter = AppsAdapter()
        appsRecycler.layoutManager = LinearLayoutManager(this)
        appsRecycler.adapter = appsAdapter

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadApps()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        errorStateView.hide()
                        swipeRefreshLayout.isRefreshing = true
                    }
                    is UiState.Success -> {
                        errorStateView.hide()
                        swipeRefreshLayout.isRefreshing = false
                        Log.d("AppDebug", "Received apps: ${state.apps.map { it.name }}")
                        appsAdapter.submitList(state.apps)

                        if (!initialSelected.isEmpty()) {
                            appsAdapter.filterByCategories(initialSelected)
                        }
                    }
                    is UiState.Error -> {
                        swipeRefreshLayout.isRefreshing = false
                        Log.e("AppDebug", "Error loading apps: ${state.message}")
                        errorStateView.show(state.message) { viewModel.loadApps() }
                    }
                }
            }
        }

        viewModel.loadApps()
    }
}