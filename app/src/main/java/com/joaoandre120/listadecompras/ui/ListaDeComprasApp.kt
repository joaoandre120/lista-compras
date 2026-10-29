package com.joaoandre120.listadecompras.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.joaoandre120.listadecompras.ui.navigation.AppBottomBar
import com.joaoandre120.listadecompras.ui.navigation.AppNavDisplay
import com.joaoandre120.listadecompras.ui.navigation.AppTopBar
import com.joaoandre120.listadecompras.ui.navigation.Navigator
import com.joaoandre120.listadecompras.ui.navigation.Route
import com.joaoandre120.listadecompras.ui.navigation.TopLevelRoutes
import com.joaoandre120.listadecompras.ui.navigation.rememberNavigationState

/**
 * Raiz do app: cria o estado de navegação e monta o Scaffold com as barras
 * decididas pela rota atual.
 */
@Composable
fun ListaDeComprasApp() {
    val navigationState = rememberNavigationState(
        startRoute = Route.Groups,
        topLevelRoutes = TopLevelRoutes,
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }

    Scaffold(
        topBar = {
            navigationState.topBarState?.let { state ->
                AppTopBar(state = state, onBackClick = { navigator.goBack() })
            }
        },
        bottomBar = {
            if (navigationState.showsBottomBar) {
                AppBottomBar(
                    routes = navigationState.bottomBarRoutes,
                    isSelected = { it == navigationState.topLevelRoute },
                    onItemClick = { navigator.navigate(it) },
                )
            }
        },
    ) { innerPadding ->
        AppNavDisplay(navigationState, navigator, Modifier.padding(innerPadding))
    }
}