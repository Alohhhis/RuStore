package com.alyona.rustore.ui.theme.network

import com.alyona.rustore.ui.theme.models.ApplicationItem
import com.alyona.rustore.ui.theme.models.DownloadTaskCreateRequest
import com.alyona.rustore.ui.theme.models.DownloadTaskCreateResponse
import com.alyona.rustore.ui.theme.models.TaskResultDto
import com.alyona.rustore.ui.theme.models.TaskStatusDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AppApi {
    @GET("apps")
    suspend fun getApps(): List<ApplicationItem>

    @POST("tasks/download")
    suspend fun createDownloadTask(
        @Body body: DownloadTaskCreateRequest,
    ): DownloadTaskCreateResponse

    @GET("tasks/{taskId}/status")
    suspend fun getTaskStatus(
        @Path("taskId") taskId: String,
    ): TaskStatusDto

    @POST("tasks/{taskId}/cancel")
    suspend fun cancelTask(
        @Path("taskId") taskId: String,
    )

    @GET("tasks/{taskId}/result")
    suspend fun getTaskResult(
        @Path("taskId") taskId: String,
    ): TaskResultDto
}