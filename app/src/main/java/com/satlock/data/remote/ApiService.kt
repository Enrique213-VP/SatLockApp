package com.satlock.data.remote

import com.satlock.domain.User
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {

    @GET("users")
    suspend fun getUsers() : Response<List<User>>
}