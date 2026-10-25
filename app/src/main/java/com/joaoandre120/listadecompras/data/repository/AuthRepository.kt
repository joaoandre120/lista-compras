package com.joaoandre120.listadecompras.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Encapsula o acesso ao Firebase Auth, para que telas e ViewModels
 * não dependam diretamente do SDK.
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    /**
     * Garante que existe um usuário logado, entrando anonimamente se necessário.
     *
     * Se já houver sessão, reaproveita o mesmo uid (não cria usuário novo
     * a cada abertura do app).
     *
     * @return o uid do usuário atual.
     * @throws Exception se a autenticação falhar (ex.: sem rede na primeira vez).
     */
    suspend fun ensureSignedIn(): String {
        auth.currentUser?.let { return it.uid }
        return auth.signInAnonymously().await().user?.uid
            ?: throw IllegalStateException("Login anônimo não retornou usuário")
    }
}