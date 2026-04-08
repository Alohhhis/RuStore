package com.alyona.rustore.ui.theme.viewmodel

import com.alyona.rustore.ui.theme.models.ApplicationItem

sealed class UiState {
    object Loading : UiState()
    data class Success(val apps: List<ApplicationItem>) : UiState()
    data class Error(val message: String) : UiState()
}