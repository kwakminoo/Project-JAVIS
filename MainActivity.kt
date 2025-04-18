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
import com.example.iris.ui.theme.IRISTheme
import com.example.iris.api.HuggingChatService  // 추가된 import
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import java.util.*
import androidx.lifecycle.lifecycleScope  // 추가된 import


class MainActivity : ComponentActivity() {
    private lateinit var tts: TextToSpeech
    private lateinit var assistantReplyState: MutableState<String>
    private lateinit var huggingChatService: HuggingChatService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // TTS 초기화
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.KOREAN
            } else {
                Toast.makeText(this, "TTS 초기화 실패", Toast.LENGTH_SHORT).show()
            }
        }

        // Retrofit 인스턴스 생성
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api-inference.huggingface.co/") // API 기본 URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        huggingChatService = retrofit.create(HuggingChatService::class.java)

        setContent {
            IRISTheme {
                assistantReplyState = remember { mutableStateOf("아이리스에 오신 걸 환영합니다!") }

                IrisUI(
                    assistantReplyState = assistantReplyState,
                    onVoiceInputClick = {
                        if (!checkAudioPermission()) return@IrisUI
                        startSpeechRecognition()
                    },
                    onTextSubmit = { text ->
                        assistantReplyState.value = text
                        lifecycleScope.launch {
                            getChatbotResponse(text) // 비동기적으로 호출
                        }
                    }
                )
            }
        }
    }

    @Composable
    fun IrisUI(
        assistantReplyState: MutableState<String>,
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

    // API 호출 함수
    private suspend fun getChatbotResponse(userInput: String) {
        val requestBody = mapOf("inputs" to userInput)

        try {
            val response = huggingChatService.getResponse(
                url = "models/HuggingFaceH4/zephyr-7b-beta", // 사용할 모델
                request = requestBody
            )

            if (response.isSuccessful) {
                val replyText = response.body()?.string()?.let {
                    JSONObject(it).getJSONArray("generated_text").getString(0)
                } ?: "응답이 없습니다."

                assistantReplyState.value = replyText
                tts.speak(replyText, TextToSpeech.QUEUE_FLUSH, null, null)
            } else {
                assistantReplyState.value = "응답 오류: ${response.code()}"
            }
        } catch (e: Exception) {
            // 예외 처리: 에러 발생 시 로그를 찍고, 사용자에게 메시지를 출력
            assistantReplyState.value = "API 호출 중 오류 발생: ${e.message}"
            Log.e("IRIS", "Error during API call", e) // Logcat에 에러를 기록
        }
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
            }

            override fun onResults(results: Bundle?) {
                val spokenText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                val userText = spokenText ?: "무슨 말인지 못 들었어요."
                assistantReplyState.value = userText
                tts.speak(userText, TextToSpeech.QUEUE_FLUSH, null, null)
            }

            override fun onError(error: Int) {
                assistantReplyState.value = "음성 인식 중 오류 발생: $error"
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    IRISTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("아이리스에 오신 걸 환영합니다!")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {}) {
                Text("아이리스에게 말하기 (음성)")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("텍스트로 아이리스에게 말하기") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {}) {
                Text("텍스트 전송")
            }
        }
    }
}

