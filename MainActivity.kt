package com.example.iris

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.iris.ui.overlay.FloatingWaveformService
import com.example.iris.ui.theme.IRISTheme
import com.example.iris.ui.visualizer.CircularWaveform
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var tts: TextToSpeech
    private lateinit var assistantReplyState: MutableState<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 오버레이 권한 요청
        requestOverlayPermission()

        // TTS 초기화
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.KOREAN
                Log.d("IRIS", "TTS 초기화 완료")
            } else {
                Toast.makeText(this, "TTS 초기화 실패", Toast.LENGTH_SHORT).show()
            }
        }

        setContent {
            IRISTheme {
                assistantReplyState = remember { mutableStateOf("아이리스에 오신 걸 환영합니다!") }
                var isListening by remember { mutableStateOf(false) }
                var isSpeaking by remember { mutableStateOf(false) }

                IrisUI(
                    assistantReplyState = assistantReplyState,
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    onVoiceInputClick = {
                        if (!checkAudioPermission()) return@IrisUI
                        isListening = true
                        startSpeechRecognition {
                            isListening = false
                        }
                    },
                    onTextSubmit = { text ->
                        assistantReplyState.value = "잠시만 기다려 주세요..."
                        isSpeaking = true

                        startFloatingWaveform() // 파형 서비스 시작

                        getChatbotResponse(text) { gptReply ->
                            assistantReplyState.value = gptReply
                            speakText(gptReply) {
                                isSpeaking = false
                                stopFloatingWaveform() // TTS 끝나면 파형 중지
                            }
                        }
                    }
                )
            }
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        }
    }

    private fun startFloatingWaveform() {
        val intent = Intent(this, FloatingWaveformService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopFloatingWaveform() {
        val intent = Intent(this, FloatingWaveformService::class.java)
        stopService(intent)
    }

    @Composable
    fun IrisUI(
        assistantReplyState: MutableState<String>,
        isListening: Boolean,
        isSpeaking: Boolean,
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
            CircularWaveform(
                isActive = isListening,
                modifier = Modifier
                    .size(150.dp)
                    .padding(16.dp)
            )

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

    private fun startSpeechRecognition(onDone: () -> Unit) {
        val recognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                assistantReplyState.value = "아이리스가 듣고 있어요..."
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                assistantReplyState.value = "음성 인식 중 오류 발생: $error"
                onDone()
            }

            override fun onResults(results: Bundle?) {
                val spokenText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                val userText = spokenText ?: "무슨 말인지 못 들었어요."
                assistantReplyState.value = userText

                getChatbotResponse(userText) { gptReply ->
                    assistantReplyState.value = gptReply
                    speakText(gptReply) { onDone() }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        recognizer.startListening(intent)
    }
    private fun speakText(text: String, onDone: () -> Unit) {
        if (::tts.isInitialized) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "IRIS_TTS")
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {
                    runOnUiThread { onDone() }
                }

                override fun onError(utteranceId: String?) {
                    runOnUiThread { onDone() }
                }
            })
        } else {
            onDone()
        }
    }

    private fun getChatbotResponse(userText: String, onResponse: (String) -> Unit) {
        val fakeReply = "방금 말씀하신 \"$userText\"에 대해 알려드릴게요."
        onResponse(fakeReply)
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }
}

