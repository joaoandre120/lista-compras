package com.joaoandre120.listadecompras.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.joaoandre120.listadecompras.ui.groups.GroupsScreen
import com.joaoandre120.listadecompras.ui.lists.ListItemsScreen
import com.joaoandre120.listadecompras.ui.lists.ListsScope
import com.joaoandre120.listadecompras.ui.lists.ListsScreen

/**
 * Exibe a pilha de navegação atual. O `entryProvider` liga cada key à sua tela,
 * entregando às telas apenas lambdas de navegação.
 *
 * @param navigationState Estado que fornece as pilhas.
 * @param navigator Responsável por modificar o estado.
 * @param modifier Modificador (recebe o padding do Scaffold).
 */
@Composable
fun AppNavDisplay(
    navigationState: NavigationState,
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val entryProvider = remember(navigator) {
        entryProvider<NavKey> {
            entry<Route.Groups> {
                GroupsScreen(onGroupClick = { navigator.navigate(Route.GroupLists(it)) })
            }

            entry<Route.PrivateLists> {
                ListsScreen(
                    scope = ListsScope.Private,
                    onListClick = { navigator.navigate(Route.ListItems(it)) },
                )
            }
            entry<Route.GroupLists> { key ->
                ListsScreen(
                    scope = ListsScope.Group(key.groupId),
                    onListClick = { navigator.navigate(Route.ListItems(it)) },
                )
            }

            entry<Route.ListItems> { key ->
                ListItemsScreen(
                    listId = key.listId,
                )
            }
        }
    }

    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() },
        modifier = modifier,
    )
}