package com.joaoandre120.listadecompras.ui.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joaoandre120.listadecompras.data.model.ShoppingList
import com.joaoandre120.listadecompras.ui.theme.ListaDeComprasTheme

/**
 * Ponto de entrada da tela de listas (de um grupo ou privadas).
 *
 * Coleta o estado do [viewModel], guarda o texto do campo "Nova lista" e trata
 * os eventos (criar lista, mensagens), repassando tudo ao [ListsScreenContent].
 *
 * @param scope escopo das listas; usado para criar o ViewModel desta entry.
 * @param onListClick chamado ao tocar em uma lista; recebe o id da lista.
 * @param modifier modificador do contêiner.
 * @param viewModel fonte do estado e das ações da tela.
 */
@Composable
fun ListsScreen(
    scope: ListsScope,
    onListClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ListsViewModel = viewModel(factory = ListsViewModel.factory(scope)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado só de digitação: fica aqui, não no ViewModel. Sobrevive à rotação.
    var newListName by rememberSaveable { mutableStateOf("") }

    // Mostra a mensagem e avisa o ViewModel para apagá-la.
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    ListsScreenContent(
        state = state,
        newListName = newListName,
        snackbarHostState = snackbarHostState,
        onNewListNameChange = { newListName = it.take(60) },
        onCreateList = {
            viewModel.createList(newListName)
            newListName = ""
        },
        onListClick = onListClick,
        onDeleteList = viewModel::askDeleteList,
        onConfirmDelete = viewModel::confirmDeleteList,
        onDismissDelete = viewModel::dismissDeleteDialog,
        modifier = modifier
    )
}

/**
 * Conteúdo da tela de listas, sem estado próprio: só desenha o que recebe.
 *
 * @param state estado da tela (listas, carregando, ocupado).
 * @param newListName texto atual do campo "Nova lista".
 * @param snackbarHostState hospedeiro das mensagens pontuais.
 * @param onNewListNameChange chamado a cada alteração do nome da nova lista.
 * @param onCreateList chamado ao tocar em "Criar".
 * @param onListClick chamado ao tocar em uma lista; recebe o id da lista.
 * @param modifier modificador do contêiner.
 */
@Composable
fun ListsScreenContent(
    state: ListsUiState,
    newListName: String,
    snackbarHostState: SnackbarHostState,
    onNewListNameChange: (String) -> Unit,
    onCreateList: () -> Unit,
    onListClick: (String) -> Unit,
    onDeleteList: (ShoppingList) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = onNewListNameChange,
                    label = { Text("Nova lista") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    enabled = !state.isBusy,
                    onClick = onCreateList
                ) {
                    Text("Criar")
                }
            }

            when {
                state.isLoading -> CircularProgressIndicator()
                state.lists.isEmpty() -> Text("Nenhuma lista ainda. Crie a primeira acima.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(
                        state.lists,
                        key = { it.id }
                    ) { list ->
                        ListCard(
                            list = list,
                            canDelete = list.ownerId == state.currentUid,
                            onClick = { onListClick(list.id) },
                            onDeleteClick = { onDeleteList(list) }
                        )
                    }
                }
            }
        }

        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    state.listToDelete?.let { list ->
        DeleteListDialog(
            listName = list.name,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDelete
        )
    }
}

/**
 * Cartão de uma lista na tela.
 *
 * @param list lista exibida.
 * @param canDelete `true` só para o dono da lista; mostra a lixeira.
 * @param onClick chamado ao tocar no cartão.
 * @param onDeleteClick chamado ao tocar na lixeira.
 */
@Composable
private fun ListCard(
    list: ShoppingList,
    canDelete: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = list.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp)
            )
            if (canDelete) {
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Filled.Delete, contentDescription = "Excluir ${list.name}")
                }
            }
        }
    }
}

/**
 * Diálogo de confirmação para excluir uma lista e seus itens.
 *
 * @param listName nome da lista, para o usuário saber o que está apagando.
 * @param onConfirm chamado ao tocar em "Excluir".
 * @param onDismiss chamado ao cancelar.
 */
@Composable
private fun DeleteListDialog(
    listName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir lista?") },
        text = {
            Text("A lista \"$listName\" e todos os seus itens serão apagados para todos os membros. Essa ação não pode ser desfeita.")
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Excluir") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// ---------- Previews: consomem só o ListsScreenContent, com dados falsos ----------

private val previewLists = listOf(
    ShoppingList(id = "1", name = "Mercado da semana", groupId = "g", ownerId = "a"),
    ShoppingList(id = "2", name = "Feira", groupId = "g", ownerId = "b")
)

@Preview(showBackground = true, name = "Com listas")
@Composable
private fun ListsScreenContentPreview() {
    ListaDeComprasTheme {
        ListsScreenContent(
            state = ListsUiState(lists = previewLists, isLoading = false),
            newListName = "",
            snackbarHostState = remember { SnackbarHostState() },
            onNewListNameChange = {},
            onCreateList = {},
            onListClick = {},
            onDeleteList = {},
            onConfirmDelete = {},
            onDismissDelete = {},
        )
    }
}

@Preview(showBackground = true, name = "Vazia")
@Composable
private fun ListsScreenContentEmptyPreview() {
    ListaDeComprasTheme {
        ListsScreenContent(
            state = ListsUiState(isLoading = false),
            newListName = "Padaria",
            snackbarHostState = remember { SnackbarHostState() },
            onNewListNameChange = {},
            onCreateList = {},
            onListClick = {},
            onDeleteList = {},
            onConfirmDelete = {},
            onDismissDelete = {},
        )
    }
}