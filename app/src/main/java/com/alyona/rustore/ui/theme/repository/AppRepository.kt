package com.alyona.rustore.ui.theme.repository

import com.alyona.rustore.ui.theme.models.ApplicationItem
import com.alyona.rustore.ui.theme.network.RetrofitInstance

class AppRepository {
    suspend fun getApps(): List<ApplicationItem> {
        return RetrofitInstance.api.getApps()
    }

    suspend fun searchApps(query: String): List<ApplicationItem> {
        return getApps().filter { it.name.contains(query, ignoreCase = true) }
    }
}