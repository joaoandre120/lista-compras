package com.joaoandre120.listadecompras.ui.lists

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joaoandre120.listadecompras.data.catalog.ProductCatalog
import com.joaoandre120.listadecompras.data.catalog.StoreCatalog
import com.joaoandre120.listadecompras.data.model.CatalogProduct
import com.joaoandre120.listadecompras.data.model.CatalogStore
import com.joaoandre120.listadecompras.data.model.ShoppingItem
import com.joaoandre120.listadecompras.data.model.ShoppingList
import com.joaoandre120.listadecompras.data.util.IsoDate
import com.joaoandre120.listadecompras.ui.theme.ListaDeComprasTheme
import kotlin.collections.emptyList

/**
 * Ponto de entrada da tela de itens de uma lista.
 *
 * Coleta o estado do [viewModel], guarda o texto dos campos (novo item e nome em
 * edição) e trata os eventos, repassando tudo ao [ListItemsScreenContent].
 *
 * @param listId id da lista (vindo da key de navegação), usado para criar o ViewModel.
 * @param onListUnavailable chamado quando a lista foi excluída ou deixou de ser
 *        acessível; quem navega deve voltar à tela anterior.
 * @param modifier modificador do contêiner (normalmente o padding do Scaffold).
 * @param viewModel fonte do estado e das ações da tela.
 */
@Composable
fun ListItemsScreen(
    listId: String,
    onListUnavailable: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ListItemsViewModel = viewModel(factory = ListItemsViewModel.factory(listId)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Estado só de digitação: fica aqui, não no ViewModel. Sobrevive à rotação.
    var newItemName by rememberSaveable { mutableStateOf("") }
    var nameInput by rememberSaveable(state.itemBeingEdited?.id) {
        mutableStateOf(state.itemBeingEdited?.name.orEmpty())
    }
    // Rascunho do diálogo de detalhes: reinicia com os valores da lista a cada abertura.
    var storeInput by rememberSaveable(state.showDetailsDialog) {
        mutableStateOf(state.list?.store.orEmpty())
    }
    val storeSuggestions = remember(storeInput) {
        StoreCatalog.search(storeInput).filter { it.name != storeInput.trim() }.take(5)
    }
    var dateInput by rememberSaveable(state.showDetailsDialog) {
        mutableStateOf(state.list?.purchaseDate)
    }
    var showDatePicker by rememberSaveable(state.showDetailsDialog) { mutableStateOf(false) }

    // Sugestões do catálogo para o texto digitado: função pura e rápida, calculada aqui.
    val productSuggestions = remember(newItemName) {
        ProductCatalog.search(newItemName).take(5)
    }


    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    LaunchedEffect(state.isListUnavailable) {
        if (state.isListUnavailable) {
            Toast.makeText(context, "Esta lista não está mais disponível.", Toast.LENGTH_LONG).show()
            onListUnavailable()
        }
    }

    ListItemsScreenContent(
        state = state,
        newItemName = newItemName,
        nameInput = nameInput,
        storeInput = storeInput,
        storeSuggestions = storeSuggestions,
        dateInput = dateInput,
        showDatePicker = showDatePicker,
        snackbarHostState = snackbarHostState,
        productSuggestions = productSuggestions,
        onAddProduct = {
            viewModel.addProduct(it)
            newItemName = ""
        },
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
        onEditDetails = viewModel::editDetails,
        onStoreInputChange = { storeInput = it.take(60) },
        onDateInputChange = { dateInput = it },
        onDatePickerVisibleChange = { showDatePicker = it },
        onSaveDetails = { viewModel.savePurchaseDetails(storeInput, dateInput) },
        onDismissDetails = viewModel::dismissDetails,
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
 * @param productSuggestions produtos do catálogo que combinam com [newItemName].
 * @param onAddProduct chamado ao tocar em uma sugestão; recebe o produto escolhido.
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
    productSuggestions: List<CatalogProduct>,
    onAddProduct: (CatalogProduct) -> Unit,
    onNewItemNameChange: (String) -> Unit,
    onAddItem: () -> Unit,
    onCheckedChange: (ShoppingItem, Boolean) -> Unit,
    onEditItem: (ShoppingItem) -> Unit,
    onDeleteItem: (ShoppingItem) -> Unit,
    onNameInputChange: (String) -> Unit,
    onSaveItemName: () -> Unit,
    onDismissEdit: () -> Unit,
    storeInput: String,                       // texto do campo "Supermercado" no diálogo
    storeSuggestions: List<CatalogStore>,
    dateInput: String?,                       // data do rascunho (AAAA-MM-DD) ou null
    showDatePicker: Boolean,                  // seletor de datas visível
    onEditDetails: () -> Unit,                // tocar no cartão de detalhes
    onStoreInputChange: (String) -> Unit,
    onDateInputChange: (String?) -> Unit,     // escolhida no seletor, "Hoje" ou "Limpar"
    onDatePickerVisibleChange: (Boolean) -> Unit,
    onSaveDetails: () -> Unit,
    onDismissDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.list?.let { list ->
                PurchaseDetailsCard(list = list, onClick = onEditDetails)
            }

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

            if (newItemName.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    SuggestionRow(
                        title = "Adicionar “${newItemName.trim()}”",
                        supportingText = if (isNameInList(newItemName, state.items)) "Já está na lista" else null,
                        onClick = onAddItem
                    )
                    productSuggestions.forEach { product ->
                        val inList = isProductInList(product, state.items)
                        SuggestionRow(
                            title = product.name,
                            supportingText = if (inList) "${product.category} · já está na lista" else product.category,
                            onClick = { onAddProduct(product) }
                        )
                    }
                }
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

    if (state.showDetailsDialog) {
        PurchaseDetailsDialog(
            store = storeInput,
            dateIso = dateInput,
            storeSuggestions = storeSuggestions,
            onStoreChange = onStoreInputChange,
            onPickDate = { onDatePickerVisibleChange(true) },
            onUseToday = { onDateInputChange(IsoDate.today()) },
            onClearDate = { onDateInputChange(null) },
            onSave = onSaveDetails,
            onDismiss = onDismissDetails
        )
        if (showDatePicker) {
            PurchaseDatePickerDialog(
                initialIso = dateInput,
                onConfirm = {
                    onDateInputChange(it)
                    onDatePickerVisibleChange(false)
                },
                onDismiss = { onDatePickerVisibleChange(false) }
            )
        }
    }
}

/**
 * Linha clicável de sugestão, usada sob o campo de adicionar item.
 *
 * @param title texto principal.
 * @param onClick chamado ao tocar na sugestão.
 * @param supportingText texto secundário opcional (ex.: categoria do produto).
 */
@Composable
private fun SuggestionRow(
    title: String,
    onClick: () -> Unit,
    supportingText: String? = null
) {
    val supporting: (@Composable () -> Unit)? =
        if (supportingText != null) { { Text(supportingText) } } else null

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = supporting,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick)
    )
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

/**
 * Cartão no topo da tela com o nome da lista, o supermercado e a data da compra.
 *
 * @param list lista exibida.
 * @param onClick chamado ao tocar no cartão, para editar os detalhes.
 */
@Composable
private fun PurchaseDetailsCard(list: ShoppingList, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(list.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = purchaseSummary(list)
                        ?: "Toque para informar supermercado e data da compra",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Filled.Edit, contentDescription = "Editar supermercado e data")
        }
    }
}

/**
 * Diálogo para informar o supermercado e a data da compra.
 *
 * @param store texto atual do campo "Supermercado".
 * @param dateIso data do rascunho (`AAAA-MM-DD`) ou `null`.
 * @param onStoreChange chamado a cada alteração do supermercado.
 * @param onPickDate chamado ao tocar no botão da data, para abrir o seletor.
 * @param onUseToday chamado ao tocar em "Hoje".
 * @param onClearDate chamado ao tocar em "Limpar".
 * @param onSave chamado ao tocar em "Salvar".
 * @param onDismiss chamado ao cancelar.
 */
@Composable
private fun PurchaseDetailsDialog(
    store: String,
    dateIso: String?,
    storeSuggestions: List<CatalogStore>,
    onStoreChange: (String) -> Unit,
    onPickDate: () -> Unit,
    onUseToday: () -> Unit,
    onClearDate: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalhes da compra") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = store,
                    onValueChange = onStoreChange,
                    label = { Text("Supermercado") },
                    singleLine = true,
                    supportingText = { Text("${store.length}/60") }
                )
                if (storeSuggestions.isNotEmpty()) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        storeSuggestions.forEach { store ->
                            SuggestionChip(
                                onClick = { onStoreChange(store.name) },
                                label = { Text(store.name) }
                            )
                        }
                    }
                }
                OutlinedButton(onClick = onPickDate, modifier = Modifier.fillMaxWidth()) {
                    Text(dateIso?.let { IsoDate.display(it) } ?: "Escolher data")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onUseToday) { Text("Hoje") }
                    if (dateIso != null) {
                        TextButton(onClick = onClearDate) { Text("Limpar data") }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onSave) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/**
 * Seletor de data da compra. O estado do seletor é só de interface e fica aqui.
 *
 * @param initialIso data inicialmente selecionada (`AAAA-MM-DD`) ou `null`.
 * @param onConfirm chamado ao confirmar, com a data escolhida como `AAAA-MM-DD`.
 * @param onDismiss chamado ao cancelar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PurchaseDatePickerDialog(
    initialIso: String?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialIso?.let { IsoDate.toPickerMillis(it) }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = pickerState.selectedDateMillis != null,
                onClick = {
                    pickerState.selectedDateMillis?.let { onConfirm(IsoDate.fromPickerMillis(it)) }
                }
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    ) {
        DatePicker(state = pickerState)
    }
}

// ---------- Previews: consomem só o ListItemsScreenContent, com dados falsos ----------

private val previewItems = listOf(
    ShoppingItem(id = "1", name = "Arroz", addedBy = "a"),
    ShoppingItem(id = "2", name = "Leite", addedBy = "b"),
    ShoppingItem(id = "3", name = "Feijão", checked = true, addedBy = "a"),
    ShoppingItem(id = "", name = "laranja", checked = false, addedBy = "a"),
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
            onDismissEdit = {},
            storeInput = "",
            dateInput = "",
            showDatePicker = false,
            onEditDetails = {}, onStoreInputChange = {}, onDateInputChange = {},
            onDatePickerVisibleChange = {}, onSaveDetails = {}, onDismissDetails = {},
            productSuggestions = emptyList(), onAddProduct = {},
            storeSuggestions = emptyList(),
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
            onDismissEdit = {},
            storeInput = "",
            dateInput = "",
            showDatePicker = false,
            onEditDetails = {}, onStoreInputChange = {}, onDateInputChange = {},
            onDatePickerVisibleChange = {}, onSaveDetails = {}, onDismissDetails = {},
            productSuggestions = emptyList(), onAddProduct = {},
            storeSuggestions = emptyList(),
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
            onDismissEdit = {},
            storeInput = "",
            dateInput = "",
            showDatePicker = false,
            onEditDetails = {}, onStoreInputChange = {}, onDateInputChange = {},
            onDatePickerVisibleChange = {}, onSaveDetails = {}, onDismissDetails = {},
            productSuggestions = emptyList(), onAddProduct = {},
            storeSuggestions = emptyList(),
        )
    }
}

@Preview(showBackground = true, name = "Detalhes da compra")
@Composable
private fun ListItemsScreenContentDetailsPreview() {
    ListaDeComprasTheme {
        ListItemsScreenContent(
            state = ListItemsUiState(
                items = previewItems,
                isLoading = false,
                list = ShoppingList(
                    id = "l", name = "Mercado da semana",
                    store = "Extra", purchaseDate = "2026-10-30"
                ),
                showDetailsDialog = true
            ),
            newItemName = "",
            nameInput = "",
            storeInput = "Extra",
            dateInput = "2026-10-30",
            storeSuggestions = StoreCatalog.search("").take(4),
            showDatePicker = false,
            snackbarHostState = remember { SnackbarHostState() },
            onNewItemNameChange = {}, onAddItem = {},
            onCheckedChange = { _, _ -> },
            onEditItem = {}, onDeleteItem = {},
            onNameInputChange = {}, onSaveItemName = {}, onDismissEdit = {},
            onEditDetails = {}, onStoreInputChange = {}, onDateInputChange = {},
            onDatePickerVisibleChange = {}, onSaveDetails = {}, onDismissDetails = {},
            productSuggestions = emptyList(), onAddProduct = {},
        )
    }
}

@Preview(showBackground = true, name = "Com sugestões")
@Composable
private fun ListItemsScreenContentSuggestionsPreview() {
    ListaDeComprasTheme {
        ListItemsScreenContent(
            state = ListItemsUiState(items = previewItems, isLoading = false),
            newItemName = "ar",
            productSuggestions = ProductCatalog.search("ar").take(5),

            nameInput = "",
            storeInput = "Extra",
            dateInput = "2026-10-30",
            showDatePicker = false,
            snackbarHostState = remember { SnackbarHostState() },
            onNewItemNameChange = {}, onAddItem = {},
            onCheckedChange = { _, _ -> },
            onEditItem = {}, onDeleteItem = {},
            onNameInputChange = {}, onSaveItemName = {}, onDismissEdit = {},
            onEditDetails = {}, onStoreInputChange = {}, onDateInputChange = {},
            onDatePickerVisibleChange = {}, onSaveDetails = {}, onDismissDetails = {},
            onAddProduct = {},
            storeSuggestions = emptyList(),
        )
    }
}
