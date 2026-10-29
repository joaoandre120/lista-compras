package com.joaoandre120.listadecompras.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer

/**
 * Cria o [NavigationState] sobrevivendo a mudanças de configuração e à morte
 * do processo (as pilhas via `rememberNavBackStack`, a aba atual via
 * `rememberSerializable`; não troque por `rememberSaveable`).
 *
 * @param startRoute Rota inicial; o usuário sempre sai do app por ela.
 * @param topLevelRoutes Rotas de nível superior; cada uma ganha sua pilha.
 * @return Estado de navegação memorizado.
 */
@Composable
fun rememberNavigationState(
    startRoute: NavKey,
    topLevelRoutes: Set<NavKey>,
): NavigationState {
    val topLevelRoute = rememberSerializable(
        startRoute, topLevelRoutes,
        serializer = MutableStateSerializer(NavKeySerializer()),
    ) {
        mutableStateOf(startRoute)
    }

    val backStacks = topLevelRoutes.associateWith { key -> rememberNavBackStack(key) }

    return remember(startRoute, topLevelRoutes) {
        NavigationState(
            startRoute = startRoute,
            topLevelRoute = topLevelRoute,
            backStacks = backStacks,
        )
    }
}

/**
 * State Holder da navegação: aba atual e uma pilha por aba.
 * Só deve ser modificado pelo [Navigator].
 *
 * @property startRoute Rota inicial (aba por onde o app é encerrado).
 * @property backStacks Pilha de cada rota de nível superior.
 */
class NavigationState(
    val startRoute: NavKey,
    topLevelRoute: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    /** Aba (rota de nível superior) atualmente selecionada. */
    var topLevelRoute: NavKey by topLevelRoute

    /** Pilhas exibidas: a inicial e, se for outra, a aba atual. */
    val stacksInUse: List<NavKey>
        get() = if (topLevelRoute == startRoute) {
            listOf(startRoute)
        } else {
            listOf(startRoute, topLevelRoute)
        }

    /** Rota visível agora (topo da pilha da aba atual). */
    val currentRoute: Route?
        get() = backStacks[topLevelRoute]?.lastOrNull() as? Route

    /** Configuração da barra superior da rota atual (`null` = sem barra). */
    val topBarState: TopBarState?
        get() = currentRoute?.topBarState()

    /** Se a barra inferior deve aparecer na rota atual. */
    val showsBottomBar: Boolean
        get() = currentRoute?.showsBottomBar() == true

    /** Rotas que viram itens da barra inferior, na ordem de [TopLevelRoutes]. */
    val bottomBarRoutes: List<Route>
        get() = backStacks.keys.filterIsInstance<Route>()
}

/**
 * Converte o [NavigationState] nas entries que o `NavDisplay` exibe.
 *
 * Cada pilha é decorada (estado salvo + ViewModel por entry) e fica viva mesmo
 * quando não está visível; só as entries das pilhas em uso são devolvidas.
 *
 * @param entryProvider Converte uma key em [NavEntry].
 * @return Lista observável de entries a exibir.
 */
@Composable
fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): SnapshotStateList<NavEntry<NavKey>> {
    val decoratedEntries = backStacks.mapValues { (_, stack) ->
        val decorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
            rememberViewModelStoreNavEntryDecorator<NavKey>(), // acréscimo: ViewModel por entry
        )
        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = decorators,
            entryProvider = entryProvider,
        )
    }

    return stacksInUse
        .flatMap { decoratedEntries[it] ?: emptyList() }
        .toMutableStateList()
}