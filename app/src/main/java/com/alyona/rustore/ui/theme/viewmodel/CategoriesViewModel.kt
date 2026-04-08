package com.alyona.rustore.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alyona.rustore.ui.theme.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoriesViewModel : ViewModel() {

    private val repository = AppRepository()
    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state

    fun loadApps() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val apps = repository.getApps()
                _state.value = UiState.Success(apps)
            } catch (e: Exception) {
                _state.value = UiState.Error("Ошибка загрузки категорий")
            }
        }
    }
}