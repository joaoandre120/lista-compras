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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
 * Coleta o estado do [viewModel], guarda o texto dos campos e trata os eventos
 * (criar, entrar, convidar, compartilhar), repassando tudo ao [GroupsScreenContent].
 *
 * @param modifier modificador do contêiner (normalmente o padding do Scaffold).
 * @param viewModel fonte do estado e das ações da tela.
 */
@Composable
fun GroupsScreen(
    modifier: Modifier = Modifier,
    viewModel: GroupsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Estado só de digitação: fica aqui, não no ViewModel. Sobrevive à rotação.
    var newGroupName by rememberSaveable { mutableStateOf("") }
    var joinCode by rememberSaveable { mutableStateOf("") }

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
        modifier = modifier
    )
}

/**
 * Conteúdo da tela de grupos, sem estado próprio: só desenha o que recebe.
 *
 * @param state estado da tela (lista, carregando, ocupado, convite).
 * @param newGroupName texto atual do campo "Novo grupo".
 * @param joinCode texto atual do campo "Código do convite".
 * @param snackbarHostState hospedeiro das mensagens pontuais.
 * @param onNewGroupNameChange chamado a cada alteração do nome do novo grupo.
 * @param onJoinCodeChange chamado a cada alteração do código digitado.
 * @param onCreateGroup chamado ao tocar em "Criar".
 * @param onJoinWithCode chamado ao tocar em "Entrar".
 * @param onInviteClick chamado ao tocar em "Convidar", com o grupo escolhido.
 * @param onShareInvite chamado ao tocar em "Compartilhar" no diálogo do convite.
 * @param onDismissInvite chamado ao fechar o diálogo do convite.
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
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Meus grupos", style = MaterialTheme.typography.headlineSmall)

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
                            inviteEnabled = !state.isBusy,
                            onInviteClick = { onInviteClick(group) }
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
}

/**
 * Cartão de um grupo na lista.
 *
 * @param group grupo exibido.
 * @param inviteEnabled `false` desabilita o botão enquanto outra ação está em andamento.
 * @param onInviteClick chamado ao tocar em "Convidar".
 */
@Composable
private fun GroupCard(
    group: Group,
    inviteEnabled: Boolean,
    onInviteClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
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
            }
            TextButton(enabled = inviteEnabled, onClick = onInviteClick) { Text("Convidar") }
        }
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
            state = GroupsUiState(groups = previewGroups, isLoading = false),
            newGroupName = "",
            joinCode = "",
            snackbarHostState = remember { SnackbarHostState() },
            onNewGroupNameChange = {},
            onJoinCodeChange = {},
            onCreateGroup = {},
            onJoinWithCode = {},
            onInviteClick = {},
            onShareInvite = {},
            onDismissInvite = {}
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
            onDismissInvite = {}
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
            onDismissInvite = {}
        )
    }
}