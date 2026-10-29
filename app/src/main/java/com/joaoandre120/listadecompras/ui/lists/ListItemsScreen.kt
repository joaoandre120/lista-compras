package com.joaoandre120.listadecompras.ui.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Ponto de entrada da tela de listas de um grupo (provisória).
 *
 * @param listId Id do grupo (vindo da key de navegação), usado para criar o ViewModel.
 * @param onListClick Chamado ao abrir uma lista; recebe o id da lista.
 * @param modifier Modificador aplicado ao conteúdo.
 * @param viewModel ViewModel da entry atual, criado com [listId].
 */
@Composable
fun ListItemsScreen(
    listId: String,
    onListClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ListItemsViewModel = viewModel(factory = ListItemsViewModel.factory(listId)),
) {
    ListItemsScreenContent(
        listId = viewModel.listId,
        onListClick = onListClick,
        modifier = modifier,
    )
}

/**
 * Conteúdo stateless da tela de listas do grupo.
 *
 * @param listId Id do grupo exibido.
 * @param onListClick Callback ao tocar no botão de teste.
 * @param modifier Modificador aplicado ao layout.
 */
@Composable
fun ListItemsScreenContent(
    listId: String,
    onListClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Lista: $listId")
        Text("Lista de teste")
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupListsScreenContentPreview() {
    GroupListsScreenContent(groupId = "grupo-123", onListClick = {})
}