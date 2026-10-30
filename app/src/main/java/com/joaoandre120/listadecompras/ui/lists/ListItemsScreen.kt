package com.joaoandre120.listadecompras.ui.lists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joaoandre120.listadecompras.data.model.ShoppingItem
import com.joaoandre120.listadecompras.ui.theme.ListaDeComprasTheme

/**
 * Ponto de entrada da tela de itens de uma lista.
 *
 * Coleta o estado do [viewModel], guarda o texto dos campos (novo item e nome em
 * edição) e trata os eventos, repassando tudo ao [ListItemsScreenContent].
 *
 * @param listId id da lista (vindo da key de navegação), usado para criar o ViewModel.
 * @param modifier modificador do contêiner (normalmente o padding do Scaffold).
 * @param viewModel fonte do estado e das ações da tela.
 */
@Composable
fun ListItemsScreen(
    listId: String,
    modifier: Modifier = Modifier,
    viewModel: ListItemsViewModel = viewModel(factory = ListItemsViewModel.factory(listId)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado só de digitação: fica aqui, não no ViewModel. Sobrevive à rotação.
    var newItemName by rememberSaveable { mutableStateOf("") }
    // Reinicia com o nome do item toda vez que o diálogo abre para outro item.
    var nameInput by rememberSaveable(state.itemBeingEdited?.id) {
        mutableStateOf(state.itemBeingEdited?.name.orEmpty())
    }

    // Mostra a mensagem e avisa o ViewModel para apagá-la.
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    ListItemsScreenContent(
        state = state,
        newItemName = newItemName,
        nameInput = nameInput,
        snackbarHostState = snackbarHostState,
        onNewItemNameChange = { newItemName = it.take(100) },
        onAddItem = {
            viewModel.addItem(newItemName)
            newItemName = ""
        },
        onCheckedChange = viewModel::setChecked,
        onEditItem = viewModel::startEditing,
        onDeleteItem = viewModel::deleteItem,
        onNameInputChange = { nameInput = it.take(100) },
        onSaveItemName = { viewModel.saveItemName(nameInput) },
        onDismissEdit = viewModel::dismissEditing,
        modifier = modifier
    )
}

/**
 * Conteúdo da tela de itens, sem estado próprio: só desenha o que recebe.
 *
 * @param state estado da tela (itens, carregando, item em edição).
 * @param newItemName texto atual do campo "Adicionar item".
 * @param nameInput texto atual do campo do diálogo de edição.
 * @param snackbarHostState hospedeiro das mensagens pontuais.
 * @param onNewItemNameChange chamado a cada alteração do nome do novo item.
 * @param onAddItem chamado ao tocar em "Adicionar" ou na ação "Concluído" do teclado.
 * @param onCheckedChange chamado ao marcar/desmarcar; recebe o item e o novo valor.
 * @param onEditItem chamado ao tocar no nome de um item.
 * @param onDeleteItem chamado ao tocar na lixeira de um item.
 * @param onNameInputChange chamado a cada alteração do nome no diálogo.
 * @param onSaveItemName chamado ao tocar em "Salvar" no diálogo.
 * @param onDismissEdit chamado ao cancelar o diálogo.
 * @param modifier modificador do contêiner.
 */
@Composable
fun ListItemsScreenContent(
    state: ListItemsUiState,
    newItemName: String,
    nameInput: String,
    snackbarHostState: SnackbarHostState,
    onNewItemNameChange: (String) -> Unit,
    onAddItem: () -> Unit,
    onCheckedChange: (ShoppingItem, Boolean) -> Unit,
    onEditItem: (ShoppingItem) -> Unit,
    onDeleteItem: (ShoppingItem) -> Unit,
    onNameInputChange: (String) -> Unit,
    onSaveItemName: () -> Unit,
    onDismissEdit: () -> Unit,
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
                    value = newItemName,
                    onValueChange = onNewItemNameChange,
                    label = { Text("Adicionar item") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onAddItem() }),
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = onAddItem) { Text("Adicionar") }
            }

            when {
                state.isLoading -> CircularProgressIndicator()
                state.items.isEmpty() -> Text("Lista vazia. Adicione o primeiro item acima.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.items, key = { it.id }) { item ->
                        ItemRow(
                            item = item,
                            onCheckedChange = { onCheckedChange(item, it) },
                            onEdit = { onEditItem(item) },
                            onDelete = { onDeleteItem(item) }
                        )
                    }
                }
            }
        }

        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    state.itemBeingEdited?.let {
        EditItemDialog(
            name = nameInput,
            onNameChange = onNameInputChange,
            onSave = onSaveItemName,
            onDismiss = onDismissEdit
        )
    }
}

/**
 * Linha de um item: checkbox, nome (toque para editar) e lixeira.
 *
 * @param item item exibido.
 * @param onCheckedChange chamado ao marcar/desmarcar, com o novo valor.
 * @param onEdit chamado ao tocar no nome.
 * @param onDelete chamado ao tocar na lixeira.
 */
@Composable
private fun ItemRow(
    item: ShoppingItem,
    onCheckedChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = item.checked, onCheckedChange = onCheckedChange)
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (item.checked) TextDecoration.LineThrough else null,
                color = if (item.checked) {
                    MaterialTheme.colorScheme.outline
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onEdit)
                    .padding(vertical = 12.dp)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Remover ${item.name}")
            }
        }
    }
}

/**
 * Diálogo para corrigir o nome de um item.
 *
 * @param name texto atual do campo.
 * @param onNameChange chamado a cada alteração do texto.
 * @param onSave chamado ao tocar em "Salvar".
 * @param onDismiss chamado ao cancelar.
 */
@Composable
private fun EditItemDialog(
    name: String,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar item") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Nome") },
                singleLine = true,
                supportingText = { Text("${name.length}/100") }
            )
        },
        confirmButton = { TextButton(onClick = onSave) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// ---------- Previews: consomem só o ListItemsScreenContent, com dados falsos ----------

private val previewItems = listOf(
    ShoppingItem(id = "1", name = "Arroz", addedBy = "a"),
    ShoppingItem(id = "2", name = "Leite", addedBy = "b"),
    ShoppingItem(id = "3", name = "Feijão", checked = true, addedBy = "a")
)

@Preview(showBackground = true, name = "Com itens")
@Composable
private fun ListItemsScreenContentPreview() {
    ListaDeComprasTheme {
        ListItemsScreenContent(
            state = ListItemsUiState(items = previewItems, isLoading = false),
            newItemName = "",
            nameInput = "",
            snackbarHostState = remember { SnackbarHostState() },
            onNewItemNameChange = {},
            onAddItem = {},
            onCheckedChange = { _, _ -> },
            onEditItem = {},
            onDeleteItem = {},
            onNameInputChange = {},
            onSaveItemName = {},
            onDismissEdit = {}
        )
    }
}

@Preview(showBackground = true, name = "Vazia")
@Composable
private fun ListItemsScreenContentEmptyPreview() {
    ListaDeComprasTheme {
        ListItemsScreenContent(
            state = ListItemsUiState(isLoading = false),
            newItemName = "Pão",
            nameInput = "",
            snackbarHostState = remember { SnackbarHostState() },
            onNewItemNameChange = {},
            onAddItem = {},
            onCheckedChange = { _, _ -> },
            onEditItem = {},
            onDeleteItem = {},
            onNameInputChange = {},
            onSaveItemName = {},
            onDismissEdit = {}
        )
    }
}

@Preview(showBackground = true, name = "Editando")
@Composable
private fun ListItemsScreenContentEditingPreview() {
    ListaDeComprasTheme {
        ListItemsScreenContent(
            state = ListItemsUiState(
                items = previewItems,
                isLoading = false,
                itemBeingEdited = previewItems.first()
            ),
            newItemName = "",
            nameInput = "Arroz integral",
            snackbarHostState = remember { SnackbarHostState() },
            onNewItemNameChange = {},
            onAddItem = {},
            onCheckedChange = { _, _ -> },
            onEditItem = {},
            onDeleteItem = {},
            onNameInputChange = {},
            onSaveItemName = {},
            onDismissEdit = {}
        )
    }
}