package io.github.kaimiroawase.countdown.ui.activity

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import io.github.kaimiroawase.countdown.ui.screen.WidgetConfigScreen
import io.github.kaimiroawase.countdown.ui.theme.CountdownTheme
import io.github.kaimiroawase.countdown.ui.theme.OnPrimary
import io.github.kaimiroawase.countdown.ui.theme.Transparent

class WidgetConfigActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(RESULT_CANCELED)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.light(OnPrimary.toArgb(), OnPrimary.toArgb()),
        )

        val appWidgetId =
            intent?.extras?.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID,
            ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()

            return
        }

        setContent {
            CountdownTheme {
                WidgetConfigScreen(
                    appWidgetId = appWidgetId,
                    onClose = { resultOk ->
                        if (resultOk) {
                            setResult(
                                RESULT_OK,
                                Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId),
                            )
                        }

                        finish()
                    },
                )
            }
        }
    }
}
