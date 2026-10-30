package com.joaoandre120.listadecompras.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.joaoandre120.listadecompras.data.model.CatalogProduct
import com.joaoandre120.listadecompras.data.model.ShoppingItem
import com.joaoandre120.listadecompras.data.model.ShoppingList
import com.joaoandre120.listadecompras.data.repository.AuthRepository
import com.joaoandre120.listadecompras.data.repository.ItemRepository
import com.joaoandre120.listadecompras.data.repository.ListRepository
import com.joaoandre120.listadecompras.data.util.IsoDate
import com.joaoandre120.listadecompras.ui.common.friendlyErrorMessage
import com.joaoandre120.listadecompras.ui.common.isPermissionDenied
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Tudo o que a tela de itens precisa para se desenhar.
 *
 * @property items itens da lista: pendentes primeiro, depois os marcados,
 *           cada grupo em ordem alfabética.
 * @property isLoading `true` até a primeira resposta da consulta.
 * @property message mensagem pontual para a tela mostrar; `null` quando não há.
 * @property itemBeingEdited item cujo nome está sendo editado; `null` com o diálogo fechado.
 * @property isListUnavailable `true` quando a lista foi excluída ou o acesso foi perdido;
 *           a tela deve ser fechada.
 * @property list documento da lista (nome, supermercado e data); `null` até carregar.
 * @property showDetailsDialog `true` com o diálogo de detalhes da compra aberto.
 */
data class ListItemsUiState(
    val items: List<ShoppingItem> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null,
    val itemBeingEdited: ShoppingItem? = null,
    val isListUnavailable: Boolean = false,
    val list: ShoppingList? = null,
    val showDetailsDialog: Boolean = false,
)

/**
 * Controla os itens de uma lista: adicionar, marcar, editar o nome e remover.
 *
 * As ações não ligam um indicador de "ocupado": o Firestore atualiza o cache
 * local e o listener na hora, então a tela já reflete a mudança mesmo offline.
 *
 * @property listId id da lista recebido pela key de navegação.
 * @property authRepository garante o usuário logado e fornece o uid.
 * @property itemRepository acesso aos itens.
 */
class ListItemsViewModel(
    private val listId: String,
    private val authRepository: AuthRepository = AuthRepository(),
    private val itemRepository: ItemRepository = ItemRepository(),
    private val listRepository: ListRepository = ListRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(ListItemsUiState())
    val state: StateFlow<ListItemsUiState> = _state.asStateFlow()

    private var uid: String? = null

    /** `true` depois que a data automática já foi pedida nesta sessão; evita gravações repetidas. */
    private var autoDateRequested = false

    init { start() }

    /** Obtém o uid e passa a observar os itens da lista. */
    private fun start() {
        viewModelScope.launch {
            try {
                uid = authRepository.ensureSignedIn()
                observeList()
                itemRepository.observeItems(listId)
                    .catch { e -> showError(e) }
                    .collect { items ->
                        _state.update {
                            it.copy(items = sortForDisplay(items), isLoading = false)
                        }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showError(e)
            }
        }
    }

    /**
     * Adiciona um produto do catálogo: o nome é copiado para o item e o `productId` é guardado.
     *
     * @param product produto escolhido nas sugestões.
     */
    fun addProduct(product: CatalogProduct) {
        val id = uid ?: return
        launchSafely { itemRepository.addItem(listId, product.name, id, product.id) }
    }

    /**
     * Adiciona um item digitado à mão. A lista atualiza sozinha.
     *
     * @param name nome digitado; em branco ou acima de 100 caracteres gera um aviso.
     */
    fun addItem(name: String) {
        val id = uid ?: return
        val clean = name.trim()
        if (!isValidName(clean)) return
        launchSafely { itemRepository.addItem(listId, clean, id) }
    }

    /**
     * Marca ou desmarca [item]. Ao marcar, preenche a data da compra com hoje se ela estiver vazia.
     *
     * @param item item alterado.
     * @param checked novo valor, vindo do checkbox.
     */
    fun setChecked(item: ShoppingItem, checked: Boolean) {
        launchSafely { itemRepository.setChecked(listId, item.id, checked) }
        if (checked) fillPurchaseDateIfEmpty()
    }

    /** Abre o diálogo para editar o nome de [item]. */
    fun startEditing(item: ShoppingItem) = _state.update { it.copy(itemBeingEdited = item) }

    /** Fecha o diálogo de edição sem salvar. */
    fun dismissEditing() = _state.update { it.copy(itemBeingEdited = null) }

    /**
     * Salva o novo nome do item em edição e fecha o diálogo.
     *
     * @param name nome digitado; em branco ou acima de 100 caracteres gera um aviso.
     */
    fun saveItemName(name: String) {
        val item = _state.value.itemBeingEdited ?: return
        val clean = name.trim()
        if (!isValidName(clean)) return
        _state.update { it.copy(itemBeingEdited = null) }
        if (clean != item.name) {
            launchSafely { itemRepository.renameItem(listId, item.id, clean) }
        }
    }

    /** Remove [item] da lista. */
    fun deleteItem(item: ShoppingItem) {
        launchSafely { itemRepository.deleteItem(listId, item.id) }
    }

    /** Abre o diálogo de supermercado e data da compra. */
    fun editDetails() = _state.update { it.copy(showDetailsDialog = true) }

    /** Fecha o diálogo de detalhes sem salvar. */
    fun dismissDetails() = _state.update { it.copy(showDetailsDialog = false) }

    /**
     * Salva supermercado e data da compra e fecha o diálogo.
     *
     * @param store nome digitado; em branco limpa o campo e acima de 60 caracteres gera um aviso.
     * @param dateIso data como `AAAA-MM-DD`; `null` limpa a data.
     */
    fun savePurchaseDetails(store: String, dateIso: String?) {
        val clean = store.trim()
        if (clean.length > 60) {
            _state.update { it.copy(message = "Use no máximo 60 caracteres no supermercado") }
            return
        }
        _state.update { it.copy(showDetailsDialog = false) }
        launchSafely {
            listRepository.updatePurchaseDetails(listId, clean.ifEmpty { null }, dateIso)
        }
    }

    /** Grava a data de hoje na lista se ela ainda não tem data (e já foi carregada). */
    private fun fillPurchaseDateIfEmpty() {
        val list = _state.value.list ?: return   // ainda carregando: tenta na próxima marcação
        if (list.purchaseDate != null || autoDateRequested) return
        autoDateRequested = true
        launchSafely { listRepository.setPurchaseDate(listId, IsoDate.today()) }
        _state.update { it.copy(message = "Data da compra definida como hoje") }
    }

    /** Observa o documento da lista para saber se ela foi excluída. */
    private fun observeList() {
        viewModelScope.launch {
            listRepository.observeList(listId)
                .catch { e -> showError(e) }
                .collect { list ->
                    if (list == null) markListUnavailable()
                    else _state.update { it.copy(list = list) }
                }
        }
    }

    /** Sinaliza à tela que a lista não existe mais (ou não é mais acessível). */
    private fun markListUnavailable() =
        _state.update { it.copy(isLoading = false, isListUnavailable = true) }


    /** Chamado pela tela depois de exibir a mensagem, para ela não reaparecer. */
    fun messageShown() = _state.update { it.copy(message = null) }

    /**
     * Valida o nome de um item; se inválido, publica o aviso correspondente.
     *
     * @param clean nome já sem espaços nas pontas.
     * @return `true` se o nome pode ser gravado.
     */
    private fun isValidName(clean: String): Boolean {
        val problem = when {
            clean.isEmpty() -> "Digite o nome do item"
            clean.length > 100 -> "Use no máximo 100 caracteres"
            else -> null
        }
        if (problem != null) _state.update { it.copy(message = problem) }
        return problem == null
    }

    /** Pendentes primeiro, depois os marcados; em cada grupo, ordem alfabética. */
    private fun sortForDisplay(items: List<ShoppingItem>): List<ShoppingItem> =
        items.sortedWith(
            compareBy<ShoppingItem>({ item -> item.checked }, { item -> item.name.lowercase() })
        )

    /**
     * Executa uma ação sem bloquear a tela, traduzindo falhas em mensagens.
     * Se a tela fechar antes de o servidor confirmar, a gravação não se perde:
     * ela já está na fila do Firestore.
     *
     * @param block ação suspensa a executar.
     */
    private fun launchSafely(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showError(e)
            }
        }
    }


    private fun showError(e: Throwable) {
        if (isPermissionDenied(e)) {
            markListUnavailable()   // lista excluída ou acesso perdido: não há o que mostrar
            return
        }
        _state.update {
            it.copy(
                isLoading = false,
                message = friendlyErrorMessage(
                    e,
                    "Sem permissão para esta lista. Ela pode ter sido excluída."
                )
            )
        }
    }

    companion object {
        /**
         * Cria a factory que injeta o [listId] no ViewModel.
         *
         * @param listId id da lista exibida.
         * @return Factory para uso em `viewModel(factory = ...)`.
         */
        fun factory(listId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer { ListItemsViewModel(listId) }
        }
    }
}