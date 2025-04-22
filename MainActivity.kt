package com.example.iris

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var waveformView: WaveformView
    private lateinit var inputEditText: EditText
    private lateinit var speakButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        waveformView = findViewById(R.id.waveformView)
        inputEditText = findViewById(R.id.inputEditText)
        speakButton = findViewById(R.id.speakButton)

        tts = TextToSpeech(this, this)

        speakButton.setOnClickListener {
            val text = inputEditText.text.toString()
            if (text.isNotEmpty()) {
                speakText(text)
            }
        }

        // 포그라운드 서비스 시작
        if (!isServiceRunning(WaveformService::class.java)) {
            val serviceIntent = Intent(this, WaveformService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.KOREAN)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "언어를 지원하지 않습니다.")
            }
        } else {
            Log.e("TTS", "TTS 초기화 실패")
        }
    }

    private fun speakText(text: String) {
        waveformView.startAnimation()
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        // 애니메이션 종료는 TTS 완료 콜백에서 처리하는 것이 좋습니다.
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}

