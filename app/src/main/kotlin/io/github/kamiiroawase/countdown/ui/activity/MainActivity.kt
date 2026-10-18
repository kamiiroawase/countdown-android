package io.github.kaimiroawase.countdown.ui.activity

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.lifecycleScope
import io.github.kaimiroawase.countdown.AppContainer
import io.github.kaimiroawase.countdown.model.sortedForDisplay
import io.github.kaimiroawase.countdown.notify.NotifyScheduler
import io.github.kaimiroawase.countdown.ui.screen.MainScreen
import io.github.kaimiroawase.countdown.ui.theme.CountdownTheme
import io.github.kaimiroawase.countdown.ui.theme.Transparent
import io.github.kaimiroawase.countdown.widget.EventCardWidgetProvider
import io.github.kaimiroawase.countdown.widget.EventListWidgetProvider
import io.github.kaimiroawase.countdown.widget.WidgetScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.light(Transparent.toArgb(), Transparent.toArgb()),
        )

        setContent {
            CountdownTheme {
                MainScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        refreshWidgets()
    }

    private fun refreshWidgets() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val events = AppContainer.eventDao.getTopLevel().sortedForDisplay()

                EventCardWidgetProvider.updateAllWidgets(applicationContext, events)

                EventListWidgetProvider.updateAllWidgets(applicationContext, events)

                WidgetScheduler.scheduleAllUpdates(
                    applicationContext,
                    WidgetScheduler.nextRefreshMillis(events),
                )

                NotifyScheduler.syncAll(applicationContext, events)
            } catch (_: Exception) {
                //
            }
        }
    }
}
