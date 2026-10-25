package com.joaoandre120.listadecompras.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joaoandre120.listadecompras.data.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Estados possíveis do login, observados pela tela. */
sealed interface AuthState {
    data object Loading : AuthState
    data class SignedIn(val uid: String) : AuthState
    data class Error(val message: String) : AuthState
}

/**
 * Controla o login anônimo e expõe o resultado como [state].
 *
 * @property repository fonte de autenticação.
 */
class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init { signIn() }

    /** Tenta entrar (ou reaproveitar a sessão) e atualiza [state]. Também serve de "tentar de novo". */
    fun signIn() {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            _state.value = try {
                AuthState.SignedIn(repository.ensureSignedIn())
            } catch (e: CancellationException) {
                throw e   // nunca engula o cancelamento de corrotinas
            } catch (e: Exception) {
                AuthState.Error(e.message ?: "Falha ao entrar")
            }
        }
    }
}