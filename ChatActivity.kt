package com.example.iris.ui

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iris.R
import com.example.iris.data.*
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory


class ChatActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var inputEditText: EditText
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<String>()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(OpenAIService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.recyclerView)
        inputEditText = findViewById(R.id.inputEditText)

        adapter = ChatAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        inputEditText.setOnEditorActionListener { _, _, _ ->
            val userInput = inputEditText.text.toString()
            if (userInput.isNotBlank()) {
                addMessage("You: $userInput")
                sendMessageToGPT(userInput)
                inputEditText.text.clear()
            }
            true
        }
    }

    private fun addMessage(message: String) {
        messages.add(message)
        adapter.notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
    }

    private fun sendMessageToGPT(userInput: String) {
        val request = GPTRequest(
            messages = listOf(GPTMessage("user", userInput))
        )

        service.getChatCompletion(request).enqueue(object : Callback<GPTResponse> {
            override fun onResponse(call: Call<GPTResponse>, response: Response<GPTResponse>) {
                val reply = response.body()?.choices?.firstOrNull()?.message?.content
                reply?.let {
                    addMessage("IRIS: $it")
                }
            }

            override fun onFailure(call: Call<GPTResponse>, t: Throwable) {
                addMessage("IRIS: 오류가 발생했어요.")
            }
        })
    }
}
