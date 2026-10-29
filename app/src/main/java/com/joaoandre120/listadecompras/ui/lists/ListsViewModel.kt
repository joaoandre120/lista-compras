package com.joaoandre120.listadecompras.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.firestore.FirebaseFirestoreException
import com.joaoandre120.listadecompras.data.model.ShoppingList
import com.joaoandre120.listadecompras.data.repository.AuthRepository
import com.joaoandre120.listadecompras.data.repository.ListRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Quais listas a tela mostra (e onde as novas são criadas). */
sealed interface ListsScope {

    /**
     * Listas de um grupo.
     *
     * @property groupId id do grupo.
     */
    data class Group(val groupId: String) : ListsScope

    /** Listas privadas do usuário logado. */
    data object Private : ListsScope
}

/**
 * Tudo o que a tela de listas precisa para se desenhar.
 *
 * @property lists listas do escopo, em ordem alfabética, atualizadas em tempo real.
 * @property isLoading `true` até a primeira resposta da consulta.
 * @property isBusy `true` enquanto uma ação (criar lista) está em andamento.
 * @property message mensagem pontual para a tela mostrar; `null` quando não há.
 */
data class ListsUiState(
    val lists: List<ShoppingList> = emptyList(),
    val isLoading: Boolean = true,
    val isBusy: Boolean = false,
    val message: String? = null
)

/**
 * Controla as listas de um grupo ou as listas privadas, conforme o [ListsScope].
 *
 * @property listsScope define a consulta e onde as novas listas são criadas.
 * @property authRepository garante o usuário logado e fornece o uid.
 * @property listRepository acesso às listas.
 */
class ListsViewModel(
    private val listsScope: ListsScope,
    private val authRepository: AuthRepository = AuthRepository(),
    private val listRepository: ListRepository = ListRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ListsUiState())
    val state: StateFlow<ListsUiState> = _state.asStateFlow()

    private var uid: String? = null

    /** `groupId` gravado nas novas listas; `null` no escopo privado. */
    private val targetGroupId: String? = (listsScope as? ListsScope.Group)?.groupId

    init { start() }

    /** Obtém o uid e passa a observar as listas do escopo. */
    private fun start() {
        viewModelScope.launch {
            try {
                val currentUid = authRepository.ensureSignedIn()
                uid = currentUid
                val source = when (listsScope) {
                    is ListsScope.Group -> listRepository.observeGroupLists(listsScope.groupId)
                    ListsScope.Private -> listRepository.observePrivateLists(currentUid)
                }
                source
                    .catch { e -> showError(e) }
                    .collect { list ->
                        _state.update {
                            it.copy(
                                lists = list.sortedBy { item -> item.name.lowercase() },
                                isLoading = false
                            )
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
     * Cria uma lista no escopo atual. A lista atualiza sozinha.
     *
     * @param name nome digitado; em branco ou acima de 60 caracteres gera um aviso.
     */
    fun createList(name: String) {
        val id = uid ?: return
        val clean = name.trim()
        if (clean.isEmpty()) {
            _state.update { it.copy(message = "Dê um nome à lista") }
            return
        }
        if (clean.length > 60) {
            _state.update { it.copy(message = "Use no máximo 60 caracteres") }
            return
        }
        runAction { listRepository.createList(clean, id, targetGroupId) }
    }

    /** Chamado pela tela depois de exibir a mensagem, para ela não reaparecer. */
    fun messageShown() = _state.update { it.copy(message = null) }

    /**
     * Executa uma ação marcando [ListsUiState.isBusy] e traduzindo falhas em mensagens.
     *
     * @param block ação suspensa a executar.
     */
    private fun runAction(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true) }
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showError(e)
            } finally {
                _state.update { it.copy(isBusy = false) }
            }
        }
    }

    private fun showError(e: Throwable) {
        _state.update { it.copy(isLoading = false, message = friendlyMessage(e)) }
    }

    /** Converte exceções técnicas em texto próprio para o usuário. */
    private fun friendlyMessage(e: Throwable): String = when {
        e is FirebaseFirestoreException &&
                e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "Sem permissão para acessar estas listas."
        e is FirebaseFirestoreException &&
                e.code == FirebaseFirestoreException.Code.UNAVAILABLE ->
            "Sem conexão com o servidor."
        else -> e.message ?: "Algo deu errado"
    }

    companion object {
        /**
         * Cria a factory que injeta o [ListsScope] no ViewModel.
         *
         * @param scope escopo das listas exibidas.
         * @return Factory para uso em `viewModel(factory = ...)`.
         */
        fun factory(scope: ListsScope): ViewModelProvider.Factory = viewModelFactory {
            initializer { ListsViewModel(scope) }
        }
    }
}