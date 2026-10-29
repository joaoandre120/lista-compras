package com.joaoandre120.listadecompras.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.joaoandre120.listadecompras.ui.navigation.AppBottomBar
import com.joaoandre120.listadecompras.ui.navigation.AppNavHost
import com.joaoandre120.listadecompras.ui.navigation.AppNavigationState
import com.joaoandre120.listadecompras.ui.navigation.AppTopBar
import com.joaoandre120.listadecompras.ui.navigation.rememberAppNavigationState

/**
 * Raiz do app: monta o Scaffold com as barras decididas pela rota atual
 * e o grafo de navegação.
 *
 * @param navigationState Estado de navegação do app.
 */
@Composable
fun ListaDeComprasApp(
    navigationState: AppNavigationState = rememberAppNavigationState(),
) {
    Scaffold(
        topBar = {
            navigationState.topBarState?.let { state ->
                AppTopBar(state = state, onBackClick = navigationState::navigateUp)
            }
        },
        bottomBar = {
            if (navigationState.showsBottomBar) {
                AppBottomBar(
                    routes = navigationState.bottomBarRoutes,
                    isSelected = navigationState::isSelected,
                    onItemClick = navigationState::navigateToTopLevel,
                )
            }
        },
    ) { innerPadding ->
        AppNavHost(navigationState, Modifier.padding(innerPadding))
    }
}