package com.joaoandre120.listadecompras.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.joaoandre120.listadecompras.R

/**
 * Barra superior stateless, descrita por um [TopBarState].
 *
 * @param state Título e visibilidade do botão de voltar.
 * @param onBackClick Chamado ao tocar em voltar.
 * @param modifier Modificador aplicado à barra.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    state: TopBarState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { Text(stringResource(state.titleRes)) },
        navigationIcon = {
            if (state.showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.nav_back),
                    )
                }
            }
        },
        modifier = modifier,
    )
}

/**
 * Barra de navegação inferior stateless.
 *
 * @param routes Rotas exibidas como itens (as sem `bottomBarItem()` são ignoradas).
 * @param isSelected Informa se uma rota é a seção ativa.
 * @param onItemClick Chamado ao tocar em um item.
 * @param modifier Modificador aplicado à barra.
 */
@Composable
fun AppBottomBar(
    routes: List<Route>,
    isSelected: (Route) -> Boolean,
    onItemClick: (Route) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        routes.forEach { route ->
            val item = route.bottomBarItem() ?: return@forEach
            NavigationBarItem(
                selected = isSelected(route),
                onClick = { onItemClick(route) },
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(stringResource(item.labelRes)) },
            )
        }
    }
}