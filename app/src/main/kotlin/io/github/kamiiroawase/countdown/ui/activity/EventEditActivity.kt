package io.github.kaimiroawase.countdown.ui.activity

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import io.github.kaimiroawase.countdown.ui.screen.EventEditScreen
import io.github.kaimiroawase.countdown.ui.theme.CountdownTheme
import io.github.kaimiroawase.countdown.ui.theme.OnPrimary
import io.github.kaimiroawase.countdown.ui.theme.Transparent

class EventEditActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.light(OnPrimary.toArgb(), OnPrimary.toArgb()),
        )

        val eventId = intent.getLongExtra(EXTRA_ID, -1L)

        val parentEventId = intent.getLongExtra(EXTRA_PARENT_ID, 0L)

        setContent {
            CountdownTheme {
                EventEditScreen(
                    eventId = eventId,
                    parentEventId = parentEventId,
                    onClose = { finish() },
                )
            }
        }
    }

    companion object {
        const val EXTRA_ID = "id"

        const val EXTRA_PARENT_ID = "parent_id"
    }
}
