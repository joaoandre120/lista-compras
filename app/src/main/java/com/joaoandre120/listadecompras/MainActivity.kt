package com.joaoandre120.listadecompras

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joaoandre120.listadecompras.data.repository.GroupRepository
import com.joaoandre120.listadecompras.ui.ListaDeComprasApp
import com.joaoandre120.listadecompras.ui.auth.AuthState
import com.joaoandre120.listadecompras.ui.auth.AuthViewModel
import com.joaoandre120.listadecompras.ui.groups.GroupsScreen
import com.joaoandre120.listadecompras.ui.theme.ListaDeComprasTheme
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.cancellation.CancellationException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ListaDeComprasTheme {
                ListaDeComprasApp()
                if (false) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        GroupsScreen(
                            onGroupClick = {},
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
                if (false) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                        val vm: AuthViewModel = viewModel()
                        val state by vm.state.collectAsState()

                        // Se o estado for SignedIn, extrai o uid. Se for Loading ou Error, fica null.
                        val uid = (state as? AuthState.SignedIn)?.uid

                        val groupRepository =
                            remember { GroupRepository() }   // não recriar a cada recomposição
                        var groupId by remember { mutableStateOf("") }
                        var inviteCode by remember { mutableStateOf("") }
                        var testError by remember { mutableStateOf<String?>(null) }

                        LaunchedEffect(uid) {
                            if (uid == null) return@LaunchedEffect
                            try {
                                withTimeout(10_000) {
                                    groupId = groupRepository.createGroup("Família", uid)
                                    inviteCode = groupRepository.createInvite(groupId, uid)
                                }
                            } catch (e: TimeoutCancellationException) {
                                testError =
                                    "Sem resposta do Firestore em 10s (banco não criado, rede ou regras?)"
                            } catch (e: CancellationException) {
                                throw e   // nunca engula o cancelamento de corrotinas
                            } catch (e: Exception) {
                                testError = "${e::class.simpleName}: ${e.message}"
                            }
                        }

                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
//                        .fillMaxSize()
                        ) {
                            Text(text = state.toString())
                            // Tratando cada estado individualmente
                            /*
                        when (val currentState = state) {
                            is AuthState.Loading -> {
                                Text(text = "Carregando...")
                            }
                            is AuthState.SignedIn -> {
                                // Aqui dentro o Kotlin faz "smart cast" e libera o acesso ao currentState.uid
                                Text(text = "UID do Usuário: ${currentState.uid}")
                            }
                            is AuthState.Error -> {
                                Text(text = "Erro: ${currentState.message}")
                            }
                        }
                         */

                            Column() {
                                Text(text = "uid: $uid" ?: "Usuário não identificado")
                            }

                            Text("")
                            Text("Grupo criado: $groupId")
                            Text("Convite: $inviteCode")
                            Text("Erro: ${testError ?: "nenhum"}")
                            Text("")

                            Greeting(
                                name = "Android",
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ListaDeComprasTheme {
        Greeting("Android")
    }
}