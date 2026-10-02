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
class CalAIApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
