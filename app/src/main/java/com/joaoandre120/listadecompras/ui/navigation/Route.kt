package com.joaoandre120.listadecompras.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.joaoandre120.listadecompras.R
import kotlinx.serialization.Serializable

/**
 * Configuração da barra superior para uma rota.
 *
 * @property titleRes Recurso de string do título.
 * @property showBackButton Se true, exibe o botão de voltar.
 */
data class TopBarState(
    @StringRes val titleRes: Int,
    val showBackButton: Boolean = false,
)

/**
 * Item de uma rota de nível superior na barra de navegação inferior.
 *
 * @property labelRes Recurso de string do rótulo.
 * @property icon Ícone exibido no item.
 */
data class BottomBarItem(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
)

/**
 * Todos os destinos do app. Cada um é uma "key" do Navigation 3: um dado
 * serializável que referencia uma tela (a back stack guarda keys, não telas).
 *
 * Os métodos de configuração são funções, e não propriedades, para não
 * entrarem na serialização da key.
 */
@Serializable
sealed interface Route : NavKey {

    /** Barra superior desta rota. `null` = não exibe barra superior. */
    fun topBarState(): TopBarState? = null

    /** Item desta rota na barra inferior. `null` = não é uma aba de nível superior. */
    fun bottomBarItem(): BottomBarItem? = null

    /** Se a barra inferior fica visível quando esta rota está ativa. */
    fun showsBottomBar(): Boolean = true

    /** Lista dos grupos do usuário (aba inicial). */
    @Serializable
    data object Groups : Route {
        override fun topBarState() = TopBarState(R.string.nav_groups_title)
        override fun bottomBarItem() = BottomBarItem(R.string.nav_groups_label, Icons.Filled.Home)
    }

    /** Listas privadas do usuário (aba). */
    @Serializable
    data object PrivateLists : Route {
        override fun topBarState() = TopBarState(R.string.nav_private_lists_title)
        override fun bottomBarItem() = BottomBarItem(R.string.nav_private_lists_label, Icons.Filled.Lock)
    }

    /** Listas de um grupo. @property groupId Id do documento em `groups`. */
    @Serializable
    data class GroupLists(val groupId: String) : Route {
        override fun topBarState() = TopBarState(R.string.nav_group_lists_title, showBackButton = true)
    }

    /** Itens de uma lista. @property listId Id do documento em `lists`. */
    @Serializable
    data class ListItems(val listId: String) : Route {
        override fun topBarState() = TopBarState(R.string.nav_list_items_title, showBackButton = true)
        override fun showsBottomBar() = false
    }
}

/** Rotas de nível superior (abas), na ordem em que aparecem na barra inferior. */
val TopLevelRoutes: Set<Route> = linkedSetOf(Route.Groups, Route.PrivateLists)