package com.joaoandre120.listadecompras.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.joaoandre120.listadecompras.ui.groups.GroupsScreen
import com.joaoandre120.listadecompras.ui.lists.GroupListsScreen
import com.joaoandre120.listadecompras.ui.lists.ListItemsScreen

/**
 * Grafo de navegação do app. Cada destino liga a rota tipada à sua tela,
 * entregando às telas apenas lambdas de navegação.
 *
 * @param navigationState Estado de navegação que fornece o controller.
 * @param modifier Modificador (recebe o padding do Scaffold).
 */
@Composable
fun AppNavHost(
    navigationState: AppNavigationState,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navigationState.navController,
        startDestination = Route.Groups,
        modifier = modifier,
    ) {
        composable<Route.Groups> {
            GroupsScreen(onGroupClick = navigationState::navigateToGroup)
        }
        composable<Route.PrivateLists> {
            Text("Listas privadas (fatia 2)") // provisório
        }
        composable<Route.GroupLists> {
            GroupListsScreen(onListClick = navigationState::navigateToList)
        }
        composable<Route.ListItems> {
            ListItemsScreen()
        }
    }
}