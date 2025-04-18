package com.example.iris.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

interface HuggingChatService {
    @Headers(
        "Authorization: ", // 여기서 YOUR_API_TOKEN은 실제 발급받은 Hugging Face 토큰으로 교체
        "Content-Type: application/json"
    )
    @POST
    suspend fun getResponse(
        @Url url: String,
        @Body request: Map<String, String>
    ): Response<ResponseBody>
}
