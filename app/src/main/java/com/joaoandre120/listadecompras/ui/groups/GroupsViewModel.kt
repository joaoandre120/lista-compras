package com.joaoandre120.listadecompras.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestoreException
import com.joaoandre120.listadecompras.data.model.Group
import com.joaoandre120.listadecompras.data.repository.AuthRepository
import com.joaoandre120.listadecompras.data.repository.GroupRepository
import com.joaoandre120.listadecompras.data.repository.InviteException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Convite recém-criado, exibido para a pessoa copiar ou compartilhar.
 *
 * @property groupName nome do grupo do convite.
 * @property code código do convite.
 */
data class InviteInfo(val groupName: String, val code: String)

/**
 * Tudo o que a tela de grupos precisa para se desenhar.
 *
 * @property groups grupos do usuário, atualizados em tempo real.
 * @property isLoading `true` até a primeira resposta da lista.
 * @property isBusy `true` enquanto uma ação (criar, entrar, convidar) está em andamento.
 * @property invite convite a exibir; `null` quando não há nenhum.
 * @property message mensagem pontual para a tela mostrar; `null` quando não há.
 */
data class GroupsUiState(
    val groups: List<Group> = emptyList(),
    val isLoading: Boolean = true,
    val isBusy: Boolean = false,
    val invite: InviteInfo? = null,
    val message: String? = null
)

/**
 * Controla a lista de grupos e as ações de criar, convidar e entrar.
 *
 * @property authRepository garante o usuário logado e fornece o uid.
 * @property groupRepository acesso aos grupos e convites.
 */
class GroupsViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val groupRepository: GroupRepository = GroupRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(GroupsUiState())
    val state: StateFlow<GroupsUiState> = _state.asStateFlow()

    private var uid: String? = null

    init { observeGroups() }

    /** Obtém o uid e passa a observar os grupos do usuário. */
    private fun observeGroups() {
        viewModelScope.launch {
            try {
                val currentUid = authRepository.ensureSignedIn()
                uid = currentUid
                groupRepository.observeMyGroups(currentUid)
                    .catch { e -> showError(e) }   // falha no listener não derruba o app
                    .collect { list ->
                        _state.update { it.copy(groups = list, isLoading = false) }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showError(e)
            }
        }
    }

    /**
     * Cria um grupo em que o usuário é admin. A lista atualiza sozinha.
     *
     * @param name nome digitado; em branco gera uma mensagem de aviso.
     */
    fun createGroup(name: String) {
        val id = uid ?: return
        if (name.isBlank()) {
            _state.update { it.copy(message = "Dê um nome ao grupo") }
            return
        }
        runAction { groupRepository.createGroup(name, id) }
    }

    /**
     * Cria um convite para [group] e o publica em [GroupsUiState.invite].
     *
     * @param group grupo que receberá quem usar o convite.
     */
    fun createInvite(group: Group) {
        val id = uid ?: return
        runAction {
            val code = groupRepository.createInvite(group.id, id)
            _state.update { it.copy(invite = InviteInfo(group.name, code)) }
        }
    }

    /**
     * Entra em um grupo com o código de um convite. A lista atualiza sozinha.
     *
     * @param code código digitado; em branco gera uma mensagem de aviso.
     */
    fun joinWithCode(code: String) {
        val id = uid ?: return
        if (code.isBlank()) {
            _state.update { it.copy(message = "Digite o código do convite") }
            return
        }
        runAction {
            groupRepository.joinWithCode(code, id)
            _state.update { it.copy(message = "Você entrou no grupo") }
        }
    }

    /** Chamado pela tela depois de exibir a mensagem, para ela não reaparecer. */
    fun messageShown() = _state.update { it.copy(message = null) }

    /** Chamado pela tela ao fechar o diálogo do convite. */
    fun inviteDismissed() = _state.update { it.copy(invite = null) }

    /**
     * Executa uma ação marcando [GroupsUiState.isBusy] e traduzindo falhas em mensagens.
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
        e is InviteException -> e.message ?: "Convite inválido"
        e is FirebaseFirestoreException &&
                e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "Sem permissão. Se estava entrando num grupo, talvez você já seja membro."
        e is FirebaseFirestoreException &&
                e.code == FirebaseFirestoreException.Code.UNAVAILABLE ->
            "Sem conexão com o servidor."
        else -> e.message ?: "Algo deu errado"
    }
}
