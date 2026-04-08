package com.alyona.rustore.ui.theme

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.alyona.rustore.R
import com.alyona.rustore.ui.theme.viewmodel.SearchViewModel
import com.alyona.rustore.ui.theme.viewmodel.UiState
import com.alyona.rustore.ui.theme.models.ApplicationItem
import com.alyona.rustore.ui.theme.util.ErrorStateView

class SearchActivity : AppCompatActivity() {

    private lateinit var searchResults: RecyclerView
    private lateinit var adapter: AppsAdapter
    private lateinit var searchInput: EditText
    private lateinit var clearButton: ImageButton
    private lateinit var searchButton: Button
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var errorStateView: ErrorStateView

    private val viewModel: SearchViewModel by viewModels()
    private var allAppsList = listOf<ApplicationItem>()
    private var popularAppsList = listOf<ApplicationItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search)

        val backButton: ImageButton = findViewById(R.id.back_button)
        searchInput = findViewById(R.id.search_input)
        clearButton = findViewById(R.id.clear_button)
        searchButton = findViewById(R.id.search_button)
        searchResults = findViewById(R.id.search_results)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        errorStateView = ErrorStateView(findViewById(R.id.error_state))

        adapter = AppsAdapter(onClick = null)
        searchResults.layoutManager = LinearLayoutManager(this)
        searchResults.adapter = adapter

        backButton.setOnClickListener { finish() }

        clearButton.setOnClickListener {
            searchInput.text.clear()
            adapter.submitList(popularAppsList)
            clearButton.visibility = ImageButton.GONE
        }

        searchInput.requestFocus()
        showKeyboard()

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    adapter.submitList(popularAppsList)
                    clearButton.visibility = ImageButton.GONE
                } else {
                    val filtered = allAppsList.filter { it.name.contains(query, ignoreCase = true) }
                    adapter.submitList(if (filtered.isEmpty()) popularAppsList else filtered)
                    clearButton.visibility = ImageButton.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                runSearch()
                true
            } else false
        }

        searchButton.setOnClickListener { runSearch() }

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
                        allAppsList = state.apps
                        popularAppsList = allAppsList
                            .sortedByDescending { it.rating }
                            .take(10)

                        val currentQuery = searchInput.text?.toString()?.trim().orEmpty()
                        if (currentQuery.isEmpty()) {
                            adapter.submitList(popularAppsList)
                        } else {
                            runSearch()
                        }
                    }
                    is UiState.Error -> {
                        swipeRefreshLayout.isRefreshing = false
                        errorStateView.show(state.message) { viewModel.loadApps() }
                    }
                }
            }
        }

        viewModel.loadApps()
    }

    private fun runSearch() {
        val query = searchInput.text?.toString()?.trim().orEmpty()
        val list = if (query.isEmpty()) {
            popularAppsList
        } else {
            val filtered = allAppsList.filter { it.name.contains(query, ignoreCase = true) }
            if (filtered.isEmpty()) popularAppsList else filtered
        }
        adapter.submitList(list)

        searchInput.clearFocus()
        hideKeyboard()
    }

    private fun showKeyboard() {
        searchInput.post {
            searchInput.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchInput, InputMethodManager.SHOW_FORCED)
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchInput.windowToken, 0)
    }
}