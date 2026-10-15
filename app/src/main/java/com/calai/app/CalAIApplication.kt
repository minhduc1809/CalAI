package com.calai.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Base Application class khởi tạo Hilt. Implement Configuration.Provider (thay cho
 * androidx.startup mặc định, đã tắt trong Manifest) để WorkManager dùng HiltWorkerFactory —
 * nhờ đó ReminderWorker inject được CalAIRepository và gọi API thật (vd water-logs/today).
 */
@HiltAndroidApp
class CalAIApplication : Application(), Configuration.Provider, coil.ImageLoaderFactory {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var okHttpClient: okhttp3.OkHttpClient

    /** Ảnh đại diện tải qua cùng OkHttp với API nên dùng được cơ chế dò host (máy ảo, USB, Wi-Fi). */
    override fun newImageLoader(): coil.ImageLoader =
        coil.ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .crossfade(true)
            .build()

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
