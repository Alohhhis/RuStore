package com.alyona.rustore.ui.theme

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alyona.rustore.R
import com.alyona.rustore.ui.theme.models.ApplicationItem
import com.alyona.rustore.ui.theme.viewmodel.CategoriesViewModel
import com.alyona.rustore.ui.theme.viewmodel.UiState
import kotlinx.coroutines.launch

class CategoriesActivity : AppCompatActivity() {

    private val viewModel: CategoriesViewModel by viewModels()
    private lateinit var categoriesRecycler: RecyclerView
    private lateinit var categoriesAdapter: CategoriesAdapter

    private var allApps = listOf<ApplicationItem>() // все приложения с бэка
    private val categories = listOf(
        Category("Все приложения", R.drawable.all_apps),
        Category("Инструменты", R.drawable.tools),
        Category("Игры", R.drawable.games),
        Category("Государственные", R.drawable.government),
        Category("Транспорт", R.drawable.transport),
        Category("Финансы", R.drawable.finance)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        categoriesRecycler = findViewById(R.id.categories_list)
        val logo: ImageView = findViewById(R.id.logo)
        val searchInput: EditText = findViewById(R.id.search_input)

        // Клик по логотипу → закрытие Activity
        logo.setOnClickListener { finish() }

        // Клик по поиску → открываем SearchActivity
        searchInput.isFocusable = false
        searchInput.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        // Настройка RecyclerView
        categoriesAdapter = CategoriesAdapter(
            context = this,
            categories = categories,
            allApps = emptyList() // пусто пока данные не загрузились
        )
        categoriesRecycler.adapter = categoriesAdapter
        categoriesRecycler.layoutManager = LinearLayoutManager(this)

        // Подписка на данные
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is UiState.Loading -> { /* здесь можно показать прогресс */ }
                        is UiState.Success -> {
                            allApps = state.apps

                            // Обновляем адаптер
                            categoriesAdapter.allApps = allApps
                            categoriesAdapter.updateCounts()
                        }
                        is UiState.Error -> {
                            Toast.makeText(this@CategoriesActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        // Загрузка приложений
        viewModel.loadApps()
    }
}