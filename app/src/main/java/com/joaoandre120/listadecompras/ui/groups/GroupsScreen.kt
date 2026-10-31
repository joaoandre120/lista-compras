package com.joaoandre120.listadecompras.ui.groups

import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joaoandre120.listadecompras.data.model.Group
import com.joaoandre120.listadecompras.ui.theme.ListaDeComprasTheme

/**
 * Ponto de entrada da tela de grupos.
 *
 * Coleta o estado do [viewModel], guarda o texto dos campos (novo grupo, código
 * do convite e nome de exibição) e trata os eventos (criar, entrar, convidar,
 * compartilhar, salvar nome), repassando tudo ao [GroupsScreenContent].
 *
 * @param modifier modificador do contêiner (normalmente o padding do Scaffold).
 * @param viewModel fonte do estado e das ações da tela.
 */
@Composable
fun GroupsScreen(
    onGroupClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GroupsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Estado só de digitação: fica aqui, não no ViewModel. Sobrevive à rotação.
    var newGroupName by rememberSaveable { mutableStateOf("") }
    var joinCode by rememberSaveable { mutableStateOf("") }
    // Reinicia com o nome atual toda vez que o diálogo abre; sobrevive à rotação.
    var nameInput by rememberSaveable(state.showNameDialog) { mutableStateOf(state.displayName) }

    // Mostra a mensagem e avisa o ViewModel para apagá-la.
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    GroupsScreenContent(
        state = state,
        newGroupName = newGroupName,
        joinCode = joinCode,
        snackbarHostState = snackbarHostState,
        onNewGroupNameChange = { newGroupName = it },
        onJoinCodeChange = { joinCode = it },
        onCreateGroup = {
            viewModel.createGroup(newGroupName)
            newGroupName = ""
        },
        onJoinWithCode = {
            viewModel.joinWithCode(joinCode)
            joinCode = ""
        },
        onInviteClick = viewModel::createInvite,
        onShareInvite = { invite -> shareInvite(context, invite) },
        onDismissInvite = viewModel::inviteDismissed,
        nameInput = nameInput,
        onNameInputChange = { nameInput = it.take(40) },
        onEditName = viewModel::editName,
        onSaveDisplayName = { viewModel.saveDisplayName(nameInput) },
        onDismissNameDialog = viewModel::dismissNameDialog,
        onGroupClick = onGroupClick,
        onLeaveClick = viewModel::askLeaveGroup,
        onConfirmLeave = viewModel::confirmLeaveGroup,
        onDismissLeave = viewModel::dismissLeaveDialog,
        modifier = modifier
    )
}

/**
 * Conteúdo da tela de grupos, sem estado próprio: só desenha o que recebe.
 *
 * @param state estado da tela (grupos, carregando, ocupado, convite, nome e nomes dos membros).
 * @param newGroupName texto atual do campo "Novo grupo".
 * @param joinCode texto atual do campo "Código do convite".
 * @param nameInput texto atual do campo do diálogo de nome de exibição.
 * @param snackbarHostState hospedeiro das mensagens pontuais.
 * @param onNewGroupNameChange chamado a cada alteração do nome do novo grupo.
 * @param onJoinCodeChange chamado a cada alteração do código digitado.
 * @param onNameInputChange chamado a cada alteração do nome no diálogo.
 * @param onCreateGroup chamado ao tocar em "Criar".
 * @param onJoinWithCode chamado ao tocar em "Entrar".
 * @param onInviteClick chamado ao tocar em "Convidar", com o grupo escolhido.
 * @param onShareInvite chamado ao tocar em "Compartilhar" no diálogo do convite.
 * @param onDismissInvite chamado ao fechar o diálogo do convite.
 * @param onEditName chamado ao tocar no nome do topo para editá-lo.
 * @param onSaveDisplayName chamado ao tocar em "Salvar" no diálogo de nome.
 * @param onDismissNameDialog chamado ao cancelar o diálogo de nome.
 * @param onGroupClick Chamado ao tocar em um grupo; recebe o id do grupo.
 * @param modifier modificador do contêiner.
 */
@Composable
fun GroupsScreenContent(
    state: GroupsUiState,
    newGroupName: String,
    joinCode: String,
    snackbarHostState: SnackbarHostState,
    onNewGroupNameChange: (String) -> Unit,
    onJoinCodeChange: (String) -> Unit,
    onCreateGroup: () -> Unit,
    onJoinWithCode: () -> Unit,
    onInviteClick: (Group) -> Unit,
    onShareInvite: (InviteInfo) -> Unit,
    onDismissInvite: () -> Unit,
    nameInput: String,
    onNameInputChange: (String) -> Unit,
    onEditName: () -> Unit,
    onSaveDisplayName: () -> Unit,
    onDismissNameDialog: () -> Unit,
    onGroupClick: (String) -> Unit,
    onLeaveClick: (Group) -> Unit,
    onConfirmLeave: () -> Unit,
    onDismissLeave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            //Text("Meus grupos", style = MaterialTheme.typography.headlineSmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Meus grupos",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onEditName) {
                    Text(state.displayName.ifBlank { "Definir nome" })
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newGroupName,
                    onValueChange = onNewGroupNameChange,
                    label = { Text("Novo grupo") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(enabled = !state.isBusy, onClick = onCreateGroup) { Text("Criar") }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = joinCode,
                    onValueChange = onJoinCodeChange,
                    label = { Text("Código do convite") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(enabled = !state.isBusy, onClick = onJoinWithCode) { Text("Entrar") }
            }

            when {
                state.isLoading -> CircularProgressIndicator()
                state.groups.isEmpty() -> Text("Você ainda não está em nenhum grupo.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.groups, key = { it.id }) { group ->
                        GroupCard(
                            group = group,
                            membersText = memberNamesText(group, state),
                            inviteEnabled = !state.isBusy,
                            onInviteClick = { onInviteClick(group) },
                            onClick = { onGroupClick(group.id) },
                            onLeaveClick = { onLeaveClick(group) },
                        )
                    }
                }
            }
        }

        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    state.invite?.let { invite ->
        InviteDialog(
            invite = invite,
            onShare = { onShareInvite(invite) },
            onDismiss = onDismissInvite
        )
    }

    if (state.showNameDialog) {
        DisplayNameDialog(
            name = nameInput,
            canDismiss = state.displayName.isNotBlank(),
            isBusy = state.isBusy,
            onNameChange = onNameInputChange,
            onSave = onSaveDisplayName,
            onDismiss = onDismissNameDialog
        )
    }

    state.groupToLeave?.let { group ->
        LeaveGroupDialog(
            groupName = group.name,
            warning = leaveWarning(group, state),
            onConfirm = onConfirmLeave,
            onDismiss = onDismissLeave
        )
    }

}

/**
 * Cartão de um grupo na lista.
 *
 * @param group grupo exibido.
 * @param membersText nomes dos membros, já formatados (ver [memberNamesText]).
 * @param inviteEnabled `false` desabilita o botão enquanto outra ação está em andamento.
 * @param onInviteClick chamado ao tocar em "Convidar".
 */
@Composable
private fun GroupCard(
    group: Group,
    membersText: String,
    inviteEnabled: Boolean,
    onInviteClick: () -> Unit,
    onClick: () -> Unit,
    onLeaveClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(group.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${group.memberIds.size} membro(s)",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(membersText, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(enabled = inviteEnabled, onClick = onInviteClick) { Text("Convidar") }
            IconButton(enabled = inviteEnabled, onClick = onLeaveClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Sair do grupo ${group.name}"
                )
            }
        }
    }
}

/**
 * Diálogo de confirmação para sair de um grupo.
 *
 * @param groupName nome do grupo, para o usuário saber de qual está saindo.
 * @param warning consequência da saída neste caso (ver [leaveWarning]).
 * @param onConfirm chamado ao tocar em "Sair".
 * @param onDismiss chamado ao cancelar.
 */
@Composable
private fun LeaveGroupDialog(
    groupName: String,
    warning: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sair de \"$groupName\"?") },
        text = { Text(warning) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Sair") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/**
 * Explica o que acontece ao sair de [group], conforme o papel do usuário.
 * Espelha a lógica do repositório (sucessor = primeiro outro membro), só para informar.
 *
 * @param group grupo que será deixado.
 * @param state estado com o uid logado e os nomes conhecidos.
 * @return texto de aviso para o diálogo.
 */
private fun leaveWarning(group: Group, state: GroupsUiState): String {
    val uid = state.currentUid
    val others = group.memberIds.filter { it != uid }
    val iAmAdmin = uid != null && group.roles[uid] == "admin"
    val otherAdminExists = others.any { group.roles[it] == "admin" }
    return when {
        others.isEmpty() ->
            "Você é o único membro. Ao sair, ninguém mais terá acesso ao grupo nem às listas dele."
        iAmAdmin && !otherAdminExists -> {
            val name = state.memberNames[others.first()] ?: "Outro membro"
            "$name será promovido(a) a administrador(a). Você perde o acesso ao grupo e a todas as listas dele."
        }
        else ->
            "Você perde o acesso ao grupo e a todas as listas dele. Elas continuam para os outros membros."
    }
}

/**
 * Diálogo que exibe o código do convite e oferece compartilhá-lo.
 *
 * @param invite convite a exibir.
 * @param onShare chamado ao tocar em "Compartilhar".
 * @param onDismiss chamado ao fechar o diálogo.
 */
@Composable
private fun InviteDialog(
    invite: InviteInfo,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Convite para ${invite.groupName}") },
        text = { Text("Código: ${invite.code}\nVálido por 7 dias.") },
        confirmButton = { TextButton(onClick = onShare) { Text("Compartilhar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

/**
 * Abre a folha de compartilhamento do Android com o texto do convite.
 *
 * @param context contexto usado para iniciar a Activity de compartilhamento.
 * @param invite convite a compartilhar.
 */
private fun shareInvite(context: Context, invite: InviteInfo) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            "Entre no grupo \"${invite.groupName}\" da Lista de Compras com o código: ${invite.code}"
        )
    }
    context.startActivity(Intent.createChooser(send, null))
}

/**
 * Monta o texto com os nomes dos membros de [group].
 *
 * @param group grupo cujos membros serão listados.
 * @param state estado com o uid logado e o mapa de nomes conhecidos.
 * @return nomes separados por vírgula; "Você" para o usuário logado e "Membro" quando o nome não é conhecido.
 */
private fun memberNamesText(group: Group, state: GroupsUiState): String =
    group.memberIds.joinToString(", ") { id ->
        if (id == state.currentUid) "Você" else state.memberNames[id] ?: "Membro"
    }

/**
 * Diálogo para definir ou editar o nome de exibição.
 *
 * @param name texto atual do campo.
 * @param canDismiss `false` na primeira vez, quando o nome é obrigatório.
 * @param isBusy `true` enquanto o nome está sendo salvo.
 * @param onNameChange chamado a cada alteração do texto.
 * @param onSave chamado ao tocar em "Salvar".
 * @param onDismiss chamado ao cancelar (só quando [canDismiss]).
 */
@Composable
private fun DisplayNameDialog(
    name: String,
    canDismiss: Boolean,
    isBusy: Boolean,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val dismissButton: (@Composable () -> Unit)? = if (canDismiss) {
        { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    } else null

    AlertDialog(
        onDismissRequest = { if (canDismiss) onDismiss() },
        title = { Text("Como você quer ser chamado?") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Seu nome") },
                singleLine = true,
                supportingText = { Text("${name.length}/40") }
            )
        },
        confirmButton = { TextButton(enabled = !isBusy, onClick = onSave) { Text("Salvar") } },
        dismissButton = dismissButton
    )
}

// ---------- Previews: consomem só o GroupsScreenContent, com dados falsos ----------

private val previewGroups = listOf(
    Group(id = "1", name = "Família", memberIds = listOf("a", "b", "c")),
    Group(id = "2", name = "República", memberIds = listOf("a", "d"))
)

@Preview(showBackground = true, name = "Com grupos")
@Composable
private fun GroupsScreenContentPreview() {
    ListaDeComprasTheme {
        GroupsScreenContent(
            //state = GroupsUiState(groups = previewGroups, isLoading = false),
            state = GroupsUiState(
                groups = previewGroups,
                isLoading = false,
                currentUid = "a",
                displayName = "João",
                memberNames = mapOf("a" to "João", "b" to "Maria", "c" to "Ana", "d" to "Pedro")
            ),
            newGroupName = "",
            joinCode = "",
            snackbarHostState = remember { SnackbarHostState() },
            onNewGroupNameChange = {},
            onJoinCodeChange = {},
            onCreateGroup = {},
            onJoinWithCode = {},
            onInviteClick = {},
            onShareInvite = {},
            onDismissInvite = {},
            nameInput = "",
            onNameInputChange = {},
            onEditName = {},
            onSaveDisplayName = {},
            onDismissNameDialog = {},
            onGroupClick = {},
            onLeaveClick = {},
            onConfirmLeave = {},
            onDismissLeave = {},
        )
    }
}

@Preview(showBackground = true, name = "Vazia")
@Composable
private fun GroupsScreenContentEmptyPreview() {
    ListaDeComprasTheme {
        GroupsScreenContent(
            state = GroupsUiState(isLoading = false),
            newGroupName = "Mercado",
            joinCode = "",
            snackbarHostState = remember { SnackbarHostState() },
            onNewGroupNameChange = {},
            onJoinCodeChange = {},
            onCreateGroup = {},
            onJoinWithCode = {},
            onInviteClick = {},
            onShareInvite = {},
            onDismissInvite = {},
            nameInput = "",
            onNameInputChange = {},
            onEditName = {},
            onSaveDisplayName = {},
            onDismissNameDialog = {},
            onGroupClick = {},
            onLeaveClick = {},
            onConfirmLeave = {},
            onDismissLeave = {},
        )
    }
}

@Preview(showBackground = true, name = "Com convite aberto")
@Composable
private fun GroupsScreenContentInvitePreview() {
    ListaDeComprasTheme {
        GroupsScreenContent(
            state = GroupsUiState(
                groups = previewGroups,
                isLoading = false,
                invite = InviteInfo("Família", "A6YG6EKZ")
            ),
            newGroupName = "",
            joinCode = "",
            snackbarHostState = remember { SnackbarHostState() },
            onNewGroupNameChange = {},
            onJoinCodeChange = {},
            onCreateGroup = {},
            onJoinWithCode = {},
            onInviteClick = {},
            onShareInvite = {},
            onDismissInvite = {},
            nameInput = "",
            onNameInputChange = {},
            onEditName = {},
            onSaveDisplayName = {},
            onDismissNameDialog = {},
            onGroupClick = {},
            onLeaveClick = {},
            onConfirmLeave = {},
            onDismissLeave = {},
        )
    }
}

@Preview(showBackground = true, name = "Pedindo o nome")
@Composable
private fun GroupsScreenContentNameDialogPreview() {
    ListaDeComprasTheme {
        GroupsScreenContent(
            state = GroupsUiState(isLoading = false, showNameDialog = true),
            newGroupName = "",
            joinCode = "",
            snackbarHostState = remember { SnackbarHostState() },
            onNewGroupNameChange = {},
            onJoinCodeChange = {},
            onCreateGroup = {},
            onJoinWithCode = {},
            onInviteClick = {},
            onShareInvite = {},
            onDismissInvite = {},
            nameInput = "Jo",
            onNameInputChange = {},
            onEditName = {},
            onSaveDisplayName = {},
            onDismissNameDialog = {},
            onGroupClick = {},
            onLeaveClick = {},
            onConfirmLeave = {},
            onDismissLeave = {},
        )
    }
}

@Preview(showBackground = true, name = "Confirmando saída")
@Composable
private fun GroupsScreenContentLeavePreview() {
    ListaDeComprasTheme {
        GroupsScreenContent(
            state = GroupsUiState(
                groups = previewGroups.map { it.copy(roles = mapOf("a" to "admin", "b" to "member", "c" to "member")) },
                isLoading = false,
                currentUid = "a",
                memberNames = mapOf("a" to "João", "b" to "Maria", "c" to "Ana"),
                groupToLeave = previewGroups.first().copy(roles = mapOf("a" to "admin", "b" to "member", "c" to "member"))
            ),
            newGroupName = "", joinCode = "",
            snackbarHostState = remember { SnackbarHostState() },
            onNewGroupNameChange = {}, onJoinCodeChange = {},
            onCreateGroup = {}, onJoinWithCode = {},
            onInviteClick = {}, onShareInvite = {}, onDismissInvite = {},
            nameInput = "", onNameInputChange = {}, onEditName = {},
            onSaveDisplayName = {}, onDismissNameDialog = {},
            onGroupClick = {},
            onLeaveClick = {}, onConfirmLeave = {}, onDismissLeave = {},
        )
    }
}
