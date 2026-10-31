package com.joaoandre120.listadecompras.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joaoandre120.listadecompras.data.model.Group
import com.joaoandre120.listadecompras.data.repository.AuthRepository
import com.joaoandre120.listadecompras.data.repository.GroupException
import com.joaoandre120.listadecompras.data.repository.GroupRepository
import com.joaoandre120.listadecompras.data.repository.UserRepository
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
 * @property isBusy `true` enquanto uma ação (criar, entrar, convidar, salvar nome) está em andamento.
 * @property invite convite a exibir; `null` quando não há nenhum.
 * @property message mensagem pontual para a tela mostrar; `null` quando não há.
 * @property currentUid uid do usuário logado; `null` até o login terminar.
 * @property displayName nome do usuário logado; vazio enquanto não definido.
 * @property showNameDialog `true` quando a tela deve pedir ou editar o nome.
 * @property memberNames mapa uid -> nome dos membros já conhecidos.
 * @property groupToLeave grupo aguardando confirmação de saída; `null` sem diálogo.
 */
data class GroupsUiState(
    val groups: List<Group> = emptyList(),
    val isLoading: Boolean = true,
    val isBusy: Boolean = false,
    val invite: InviteInfo? = null,
    val message: String? = null,
    val currentUid: String? = null,
    val displayName: String = "",
    val showNameDialog: Boolean = false,
    val memberNames: Map<String, String> = emptyMap(),
    val groupToLeave: Group? = null,
)

/**
 * Controla a lista de grupos e as ações de criar, convidar e entrar.
 *
 * @property authRepository garante o usuário logado e fornece o uid.
 * @property groupRepository acesso aos grupos e convites.
 */
class GroupsViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val groupRepository: GroupRepository = GroupRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(GroupsUiState())
    val state: StateFlow<GroupsUiState> = _state.asStateFlow()

    private var uid: String? = null

    /** uids cujo nome já foi buscado (ou está sendo); evita repetir leituras. */
    private val knownUids = mutableSetOf<String>()

    init { start() }

    /** Obtém o uid e passa a observar o perfil e os grupos do usuário. */
    private fun start() {
        viewModelScope.launch {
            try {
                val currentUid = authRepository.ensureSignedIn()
                uid = currentUid
                _state.update { it.copy(currentUid = currentUid) }
                observeProfile(currentUid)
                groupRepository.observeMyGroups(currentUid)
                    .catch { e -> showError(e) }
                    .collect { list ->
                        _state.update { it.copy(groups = list, isLoading = false) }
                        loadMissingNames(list)
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showError(e)
            }
        }
    }

    /**
     * Observa o perfil do usuário logado. Se o documento não existir, pede o nome.
     *
     * @param currentUid uid do usuário logado.
     */
    private fun observeProfile(currentUid: String) {
        viewModelScope.launch {
            userRepository.observeProfile(currentUid)
                .catch { e -> showError(e) }
                .collect { profile ->
                    _state.update {
                        it.copy(
                            displayName = profile?.displayName.orEmpty(),
                            // Só abre sozinho quando falta o perfil; fechar é por ação explícita.
                            showNameDialog = it.showNameDialog || profile == null,
                            memberNames = if (profile != null)
                                it.memberNames + (currentUid to profile.displayName)
                            else it.memberNames
                        )
                    }
                }
        }
    }

    /**
     * Busca o nome dos membros que ainda não conhecemos, numa única consulta em blocos.
     * Se falhar, os uids voltam à fila para uma nova tentativa na próxima atualização da lista.
     *
     * @param groups grupos cujos membros devem ter nome.
     */
    private fun loadMissingNames(groups: List<Group>) {
        val missing = groups.flatMap { it.memberIds }.toSet() - knownUids - setOfNotNull(uid)
        if (missing.isEmpty()) return
        knownUids += missing
        viewModelScope.launch {
            try {
                val names = userRepository.getDisplayNames(missing)
                _state.update { it.copy(memberNames = it.memberNames + names) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                knownUids -= missing
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
     * Pede confirmação para sair de [group].
     *
     * @param group grupo que o usuário quer deixar.
     */
    fun askLeaveGroup(group: Group) = _state.update { it.copy(groupToLeave = group) }

    /** Fecha o diálogo de saída sem sair. */
    fun dismissLeaveDialog() = _state.update { it.copy(groupToLeave = null) }

    /**
     * Sai do grupo confirmado. A lista de grupos atualiza sozinha (o listener
     * `array-contains` deixa de casar com o documento).
     */
    fun confirmLeaveGroup() {
        val id = uid ?: return
        val group = _state.value.groupToLeave ?: return
        _state.update { it.copy(groupToLeave = null) }
        runAction {
            try {
                groupRepository.leaveGroup(group.id, id)
            } catch (e: Exception) {
                // Recusa das regras aqui é quase sempre corrida entre admins.
                if (isPermissionDenied(e)) {
                    throw GroupException("Não foi possível sair agora. Tente novamente.")
                }
                throw e   // inclui CancellationException: runAction a relança
            }
            _state.update { it.copy(message = "Você saiu do grupo \"${group.name}\"") }
        }
    }

    /**
     * Salva o nome de exibição e fecha o diálogo.
     *
     * @param name nome digitado; em branco ou acima de 40 caracteres gera aviso.
     */
    fun saveDisplayName(name: String) {
        val id = uid ?: return
        val clean = name.trim()
        if (clean.isEmpty()) {
            _state.update { it.copy(message = "Digite seu nome") }
            return
        }
        if (clean.length > 40) {
            _state.update { it.copy(message = "Use no máximo 40 caracteres") }
            return
        }
        runAction {
            userRepository.saveDisplayName(id, clean)
            _state.update { it.copy(showNameDialog = false) }
        }
    }

    /** Abre o diálogo para editar o nome. */
    fun editName() = _state.update { it.copy(showNameDialog = true) }

    /** Fecha o diálogo, exceto se ainda não há nome (o nome é obrigatório). */
    fun dismissNameDialog() = _state.update {
        if (it.displayName.isBlank()) it else it.copy(showNameDialog = false)
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
    private fun friendlyMessage(e: Throwable): String =
        friendlyErrorMessage(e, "Sem permissão para acessar estas listas.")

}
