package io.github.kaimiroawase.countdown.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import io.github.kaimiroawase.countdown.R
import io.github.kaimiroawase.countdown.ui.activity.BaseActivity
import io.github.kaimiroawase.countdown.ui.components.selectableNoRipple
import io.github.kaimiroawase.countdown.ui.theme.BrandOrange
import io.github.kaimiroawase.countdown.ui.theme.NavUnselectedGray
import io.github.kaimiroawase.countdown.ui.theme.SurfaceWhite
import io.github.kaimiroawase.countdown.ui.theme.Transparent

private val mainTabs = listOf(MainTab.Home, MainTab.Mine)

private sealed class MainTab(
    val labelResId: Int,
    val iconResId: Int,
    val isLightStatusBars: Boolean,
) {
    data object Home :
        MainTab(labelResId = R.string.tab_home, iconResId = R.drawable.ic_nav_event, isLightStatusBars = false)

    data object Mine :
        MainTab(labelResId = R.string.tab_mine, iconResId = R.drawable.ic_nav_mine, isLightStatusBars = false)
}

@Composable
fun MainScreen() {
    var currentTab by remember { mutableStateOf<MainTab>(MainTab.Home) }

    val view = LocalView.current
    val isLightStatusBars = currentTab.isLightStatusBars

    SideEffect {
        if (!view.isInEditMode) {
            WindowInsetsControllerCompat((view.context as BaseActivity).window, view)
                .isAppearanceLightStatusBars = isLightStatusBars
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier =
                    Modifier
                        .background(SurfaceWhite)
                        .navigationBarsPadding()
                        .height(64.dp),
                containerColor = Transparent,
                windowInsets = WindowInsets(0, 0, 0, 0),
            ) {
                mainTabs.forEach { tab ->
                    val selected = currentTab == tab

                    Column(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .selectableNoRipple(selected = selected, role = Role.Tab) { currentTab = tab },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    ) {
                        Icon(
                            painter = painterResource(tab.iconResId),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (selected) BrandOrange else NavUnselectedGray,
                        )
                        Text(
                            stringResource(tab.labelResId),
                            style =
                                MaterialTheme.typography.labelMedium.merge(
                                    TextStyle(fontSize = 12.sp),
                                ),
                            color = if (selected) BrandOrange else NavUnselectedGray,
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Box {
            mainTabs.forEach { tab ->
                Box(
                    modifier =
                        if (currentTab == tab) {
                            Modifier.fillMaxSize()
                        } else {
                            Modifier.size(0.dp)
                        },
                ) {
                    when (tab) {
                        MainTab.Home -> EventScreen(innerPadding)
                        MainTab.Mine -> MineScreen(innerPadding)
                    }
                }
            }
        }
    }
}
