package com.alyona.rustore.ui.theme.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class DownloadUiState(
    val appId: Int,
    val taskId: String,
    val status: String,
    val progress: Int,
    val resultUrl: String? = null,
    val localApkPath: String? = null,
    val errorMessage: String? = null,
)

object DownloadTaskStore {
    private val _states = MutableStateFlow<Map<Int, DownloadUiState>>(emptyMap())
    val states: StateFlow<Map<Int, DownloadUiState>> = _states

    fun upsert(state: DownloadUiState) {
        _states.update { old -> old + (state.appId to state) }
    }

    fun clear(appId: Int) {
        _states.update { old -> old - appId }
    }
}

