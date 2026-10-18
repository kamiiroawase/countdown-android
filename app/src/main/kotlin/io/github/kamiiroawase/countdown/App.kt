package io.github.kaimiroawase.countdown

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import io.github.kaimiroawase.countdown.data.AppPrefs
import io.github.kaimiroawase.countdown.model.EventPresets
import io.github.kaimiroawase.countdown.notify.NotifyScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {
    private val lifecycleScope = ProcessLifecycleOwner.get().lifecycleScope

    override fun onCreate() {
        super.onCreate()

        AppPrefs.init(this)

        AppContainer.init(this)

        AppContainer.resetZoneId(AppPrefs.getZoneId())

        NotifyScheduler.ensureChannel(this)

        if (!AppPrefs.getEventPrefilled()) {
            lifecycleScope.launch(Dispatchers.IO) {
                runCatching {
                    AppContainer.eventDao.insertAll(EventPresets.presetEvents(this@App))
                    AppPrefs.setEventPrefilled(true)
                }
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            runCatching {
                NotifyScheduler.syncAll(this@App, AppContainer.eventDao.getAll())
            }
        }
    }
}
