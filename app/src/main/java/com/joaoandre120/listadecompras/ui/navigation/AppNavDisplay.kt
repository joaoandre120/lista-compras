package com.joaoandre120.listadecompras.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.joaoandre120.listadecompras.ui.groups.GroupsScreen
import com.joaoandre120.listadecompras.ui.lists.GroupListsScreen
import com.joaoandre120.listadecompras.ui.lists.ListItemsScreen
import androidx.compose.material3.Text

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
                Text("Listas privadas (fatia 2)") // provisório
            }
            entry<Route.GroupLists> { key ->
                GroupListsScreen(
                    groupId = key.groupId,
                    onListClick = { navigator.navigate(Route.ListItems(it)) },
                )
            }
            entry<Route.ListItems> { key ->
                ListItemsScreen(
                    listId = key.listId,
                    onListClick = {  }
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