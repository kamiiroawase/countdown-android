package io.github.kaimiroawase.countdown.ui.activity

import androidx.activity.ComponentActivity
import io.github.kaimiroawase.countdown.AppContainer
import io.github.kaimiroawase.countdown.data.AppPrefs

abstract class BaseActivity : ComponentActivity() {
    override fun onStart() {
        super.onStart()

        AppContainer.resetZoneId(AppPrefs.getZoneId())
    }
}
