package com.alyona.rustore.ui.theme.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DownloadTaskCreateRequest(
    val appId: Int,
    val apkUrl: String,
)

@Serializable
data class DownloadTaskCreateResponse(
    val taskId: String,
)

@Serializable
data class TaskStatusDto(
    val taskId: String,
    val appId: Int,
    val status: String,
    val progress: Int = 0,
    val resultUrl: String? = null,
)

@Serializable
data class TaskResultDto(
    // бэк может вернуть либо "resultUrl", либо "apkPath"
    @SerialName("resultUrl") val resultUrl: String? = null,
    @SerialName("apkPath") val apkPath: String? = null,
)

