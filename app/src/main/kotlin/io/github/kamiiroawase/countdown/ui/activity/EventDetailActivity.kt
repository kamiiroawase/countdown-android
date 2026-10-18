package io.github.kaimiroawase.countdown.ui.activity

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import io.github.kaimiroawase.countdown.ui.screen.EventDetailScreen
import io.github.kaimiroawase.countdown.ui.theme.CountdownTheme
import io.github.kaimiroawase.countdown.ui.theme.Transparent

class EventDetailActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Transparent.toArgb()),
        )

        val eventId = intent.getLongExtra(EXTRA_ID, -1L)

        setContent {
            CountdownTheme {
                EventDetailScreen(
                    eventId = eventId,
                    onClose = { finish() },
                )
            }
        }
    }

    companion object {
        const val EXTRA_ID = "id"
    }
}
