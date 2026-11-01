package com.joaoandre120.listadecompras.data.repository

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.cancellation.CancellationException

/**
 * Encapsula o acesso ao Firebase Auth, para que telas e ViewModels
 * não dependam diretamente do SDK.
 *
 * @property auth instância do Firebase Auth (injetável para testes).
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    /**
     * Garante que existe um usuário logado, entrando anonimamente se necessário.
     *
     * Se já houver sessão, reaproveita o mesmo uid (não cria usuário novo
     * a cada abertura do app). O login novo tem prazo de [SIGN_IN_TIMEOUT_MS]:
     * sem ele, um Play Services com defeito pode deixar a chamada pendurada
     * sem erro nenhum.
     *
     * @return o uid do usuário atual.
     * @throws AuthException se o login falhar; a mensagem já é própria para exibir.
     * O erro técnico original fica no Logcat (tag `AuthRepository`) e na `cause`.
     */
    suspend fun ensureSignedIn(): String {
        auth.currentUser?.let { return it.uid }
        try {
            return withTimeout(SIGN_IN_TIMEOUT_MS) {
                auth.signInAnonymously().await().user?.uid
                    ?: throw IllegalStateException("Login anônimo não retornou usuário")
            }
        } catch (e: TimeoutCancellationException) {
            // Vem antes de CancellationException, de quem é subclasse.
            Log.w(TAG, "signInAnonymously excedeu o prazo", e)
            throw AuthException(
                "O login demorou demais. Verifique a internet e o Google Play Services do aparelho.",
                e
            )
        } catch (e: CancellationException) {
            throw e   // cancelamento "de verdade" (tela fechada) nunca vira erro de negócio
        } catch (e: FirebaseNetworkException) {
            Log.w(TAG, "signInAnonymously sem rede", e)
            throw AuthException("Sem internet para entrar. Verifique a conexão e reabra o app.", e)
        } catch (e: Exception) {
            Log.w(TAG, "signInAnonymously falhou", e)
            throw AuthException(
                "Não foi possível entrar. Atualize o Google Play Services e confira a data e a hora do aparelho.",
                e
            )
        }
    }

    private companion object {
        const val TAG = "AuthRepository"

        /** Prazo para o login anônimo responder. */
        const val SIGN_IN_TIMEOUT_MS = 15_000L
    }
}

/**
 * Falha no login; a mensagem já é própria para exibir.
 *
 * @param message texto para o usuário.
 * @param cause erro técnico original, útil para diagnóstico.
 */
class AuthException(message: String, cause: Throwable? = null) : Exception(message, cause)
