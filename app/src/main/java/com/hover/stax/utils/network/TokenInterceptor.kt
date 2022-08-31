package com.hover.stax.utils.network

import com.hover.stax.domain.model.TokenInfo
import com.hover.stax.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(private val authRepository: AuthRepository) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = getTokenInfo()?.accessToken ?: ""

        val request = chain.request()
        val newRequest = request.newBuilder()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(newRequest)
    }

    private fun getTokenInfo(): TokenInfo? = runBlocking {
        withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            authRepository.getTokenInfo()
        }
    }
}