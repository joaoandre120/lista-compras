package com.joaoandre120.listadecompras.ui.groups

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Tela de grupos: cria grupo, entra com código e lista os grupos do usuário.
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

    // Estado só de digitação: fica na tela, não no ViewModel.
    var newGroupName by rememberSaveable { mutableStateOf("") }
    var joinCode by rememberSaveable { mutableStateOf("") }

    // Mostra a mensagem e avisa o ViewModel para apagá-la.
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Meus grupos", style = MaterialTheme.typography.headlineSmall)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newGroupName,
                    onValueChange = { newGroupName = it },
                    label = { Text("Novo grupo") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    enabled = !state.isBusy,
                    onClick = { viewModel.createGroup(newGroupName); newGroupName = "" }
                ) { Text("Criar") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = joinCode,
                    onValueChange = { joinCode = it },
                    label = { Text("Código do convite") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    enabled = !state.isBusy,
                    onClick = { viewModel.joinWithCode(joinCode); joinCode = "" }
                ) { Text("Entrar") }
            }

            when {
                state.isLoading -> CircularProgressIndicator()
                state.groups.isEmpty() -> Text("Você ainda não está em nenhum grupo.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.groups, key = { it.id }) { group ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(group.name, style = MaterialTheme.typography.titleMedium)
                                    Text("${group.memberIds.size} membro(s)",
                                        style = MaterialTheme.typography.bodySmall)
                                }
                                TextButton(
                                    enabled = !state.isBusy,
                                    onClick = { viewModel.createInvite(group) }
                                ) { Text("Convidar") }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    state.invite?.let { invite ->
        AlertDialog(
            onDismissRequest = viewModel::inviteDismissed,
            title = { Text("Convite para ${invite.groupName}") },
            text = { Text("Código: ${invite.code}\nVálido por 7 dias.") },
            confirmButton = {
                TextButton(onClick = {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT,
                            "Entre no grupo \"${invite.groupName}\" da Lista de Compras com o código: ${invite.code}")
                    }
                    context.startActivity(Intent.createChooser(send, null))
                }) { Text("Compartilhar") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::inviteDismissed) { Text("Fechar") }
            }
        )
    }
}
