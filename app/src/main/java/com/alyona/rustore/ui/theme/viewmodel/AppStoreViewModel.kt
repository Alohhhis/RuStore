package com.alyona.rustore.ui.theme.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alyona.rustore.ui.theme.models.ApplicationItem
import com.alyona.rustore.ui.theme.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppStoreViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state

    // Метод загрузки приложений
    fun loadApps() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val apps = repository.getApps() // запрос к API
                _state.value = UiState.Success(apps)
            } catch (e: Exception) {
                Log.e("AppDebug", "Retrofit exception", e)
                _state.value = UiState.Error("Ошибка загрузки приложений")
            }
        }
    }
}