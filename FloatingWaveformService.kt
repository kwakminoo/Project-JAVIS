package com.example.iris.ui.overlay

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.example.iris.R
import com.example.iris.ui.theme.IRISTheme
import com.example.iris.ui.visualizer.CircularWaveform

class FloatingWaveformService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: FrameLayout

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceWithNotification()

        // WindowManager 초기화
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // ComposeView 생성
        val composeView = ComposeView(this).apply {
            setContent {
                IRISTheme {
                    FloatingBubble()
                }
            }
        }

        // ComposeView를 담을 FrameLayout 생성
        overlayView = FrameLayout(this).apply {
            addView(composeView)
        }

        // 오버레이 뷰 설정
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 16
            y = 150
        }

        windowManager.addView(overlayView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(overlayView)
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = "iris_overlay_channel"
        val channelName = "Iris Floating Bubble"

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)

            Notification.Builder(this, channelId)
                .setContentTitle("아이리스 실행 중")
                .setContentText("AI 비서가 대기 중입니다.")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("아이리스 실행 중")
                .setContentText("AI 비서가 대기 중입니다.")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
        }

        startForeground(1, notification)
    }

    @Composable
    fun FloatingBubble() {
        // 작고 떠 있는 원형 파형
        CircularWaveform(
            isActive = true,
            modifier = Modifier.size(100.dp)
        )
    }
}
