package com.joaoandre120.listadecompras.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController

/**
 * State Holder da navegação: concentra o [NavHostController] e expõe
 * apenas operações com nomes do domínio do app (as telas nunca veem o controller).
 *
 * @property navController Controlador usado pelo `NavHost`.
 * @property currentEntryState Entrada atual da pilha, observável pelo Compose.
 */
@Stable
class AppNavigationState(
    val navController: NavHostController,
    private val currentEntryState: State<NavBackStackEntry?>,
) {
    /** Rotas que aparecem como itens da barra inferior, em ordem. */
    val bottomBarRoutes: List<Route> = listOf(Route.Groups, Route.PrivateLists)

    /** Rota atualmente visível, ou `null` durante a inicialização. */
    val currentRoute: Route?
        get() = currentEntryState.value?.toRouteOrNull()

    /** Configuração da barra superior da rota atual (`null` = sem barra). */
    val topBarState: TopBarState?
        get() = currentRoute?.topBarState()

    /** Indica se a barra inferior deve aparecer na rota atual. */
    val showsBottomBar: Boolean
        get() = currentRoute?.bottomBarItem() != null

    /**
     * Indica se [route] é a seção ativa, olhando a hierarquia do destino atual.
     *
     * @param route Rota de um item da barra inferior.
     */
    fun isSelected(route: Route): Boolean =
        currentEntryState.value?.destination?.hierarchy?.any { it.hasRoute(route::class) } == true

    /**
     * Troca de seção na barra inferior, preservando o estado de cada seção
     * e evitando empilhar cópias da mesma tela.
     *
     * @param route Rota de destino de nível superior.
     */
    fun navigateToTopLevel(route: Route) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    /** Abre as listas do grupo [groupId]. */
    fun navigateToGroup(groupId: String) = navController.navigate(Route.GroupLists(groupId))

    /** Abre os itens da lista [listId]. */
    fun navigateToList(listId: String) = navController.navigate(Route.ListItems(listId))

    /** Volta uma tela na pilha. */
    fun navigateUp() {
        navController.navigateUp()
    }
}

/**
 * Cria e memoriza o [AppNavigationState].
 *
 * @param navController Controlador de navegação (por padrão, um novo).
 * @return Estado de navegação estável entre recomposições.
 */
@Composable
fun rememberAppNavigationState(
    navController: NavHostController = rememberNavController(),
): AppNavigationState {
    val currentEntry = navController.currentBackStackEntryAsState()
    return remember(navController) { AppNavigationState(navController, currentEntry) }
}