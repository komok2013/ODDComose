package ru.edinros.agitatoroffline.core.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface RemoteApi {
    @POST("/pub-api/v1.1/phone")
    suspend fun checkPhone(@Body phoneRequest: PhoneRequest): PhoneResponse

    @POST("/pub-api/v1/auth")
    suspend fun doAuth(@Body authRequest: AuthRequest): AuthResponse
}