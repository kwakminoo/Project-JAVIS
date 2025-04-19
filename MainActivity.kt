package com.example.iris

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.iris.ui.theme.IRISTheme
import com.example.iris.ui.WaveformVisualizer
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private lateinit var tts: TextToSpeech
    private lateinit var assistantReplyState: MutableState<String>
    private lateinit var isSpeaking: MutableState<Boolean>
    private lateinit var client: OkHttpClient

    private val apiKey = "Bearer YOUR_OPENAI_API_KEY" // 실제 키로 교체

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.KOREAN
            } else {
                Toast.makeText(this, "TTS 초기화 실패", Toast.LENGTH_SHORT).show()
            }
        }

        client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        setContent {
            IRISTheme {
                assistantReplyState = remember { mutableStateOf("아이리스에 오신 걸 환영합니다!") }
                isSpeaking = remember { mutableStateOf(false) }

                IrisUI(
                    assistantReplyState = assistantReplyState,
                    isSpeaking = isSpeaking,
                    onVoiceInputClick = {
                        if (!checkAudioPermission()) return@IrisUI
                        startSpeechRecognition()
                    },
                    onTextSubmit = { text ->
                        assistantReplyState.value = "잠시만 기다려 주세요..."
                        isSpeaking.value = true
                        lifecycleScope.launch {
                            getChatbotResponse(text)
                        }
                    }
                )
            }
        }
    }

    @Composable
    fun IrisUI(
        assistantReplyState: MutableState<String>,
        isSpeaking: MutableState<Boolean>,
        onVoiceInputClick: () -> Unit,
        onTextSubmit: (String) -> Unit
    ) {
        var userInput by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 파형 시각화 애니메이션
            WaveformVisualizer(isActive = isSpeaking.value)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = assistantReplyState.value,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onVoiceInputClick) {
                Text("아이리스에게 말하기 (음성)")
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                label = { Text("텍스트로 아이리스에게 말하기") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                if (userInput.isNotBlank()) {
                    onTextSubmit(userInput)
                    userInput = ""
                }
            }) {
                Text("텍스트 전송")
            }
        }
    }

    private fun checkAudioPermission(): Boolean {
        val permission = Manifest.permission.RECORD_AUDIO
        return if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 1001)
            false
        } else {
            true
        }
    }

    private suspend fun getChatbotResponse(userInput: String) {
        val url = "https://api.openai.com/v1/chat/completions"

        val messagesArray = JSONArray()
        messagesArray.put(JSONObject().apply {
            put("role", "user")
            put("content", userInput)
        })

        val jsonBody = JSONObject()
        jsonBody.put("model", "gpt-3.5-turbo")
        jsonBody.put("messages", messagesArray)

        val requestBody = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            jsonBody.toString()
        )

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", apiKey)
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    assistantReplyState.value = "API 호출 실패: ${e.message}"
                    isSpeaking.value = false
                }
            }

            override fun onResponse(call: Call, response: Response) {
                isSpeaking.value = false
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    val reply = JSONObject(body!!)
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    runOnUiThread {
                        assistantReplyState.value = reply.trim()
                        tts.speak(reply, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                } else {
                    runOnUiThread {
                        assistantReplyState.value = "API 오류: ${response.code}"
                    }
                }
            }
        })
    }

    private fun startSpeechRecognition() {
        val recognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                assistantReplyState.value = "아이리스가 듣고 있어요..."
                isSpeaking.value = true
            }

            override fun onResults(results: Bundle?) {
                val spokenText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                val userText = spokenText ?: "무슨 말인지 못 들었어요."
                assistantReplyState.value = userText
                isSpeaking.value = false
                tts.speak(userText, TextToSpeech.QUEUE_FLUSH, null, null)
            }

            override fun onError(error: Int) {
                assistantReplyState.value = "음성 인식 중 오류 발생: $error"
                isSpeaking.value = false
            }

            override fun onBeginningOfSpeech() {}
            override fun onEndOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        recognizer.startListening(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }
}

