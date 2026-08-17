package com.calai.app.data.remote

import retrofit2.http.GET

/**
 * Interface cho Retrofit API
 * Hiện tại để trống, sẽ thêm các endpoint nhận diện món ăn sau
 */
interface CalAIApi {
    // Ví dụ: @POST("analyze-food") suspend fun analyzeFood(...)
    
    companion object {
        const val BASE_URL = "https://api.example.com/" // Thay thế bằng API thật sau
    }
}
