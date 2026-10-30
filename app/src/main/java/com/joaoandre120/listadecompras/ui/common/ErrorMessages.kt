package com.joaoandre120.listadecompras.ui.common

import com.google.firebase.firestore.FirebaseFirestoreException

/**
 * Converte exceções técnicas em texto próprio para o usuário.
 *
 * @param error exceção capturada.
 * @param permissionDenied texto para `PERMISSION_DENIED`, que depende do contexto da tela.
 * @return mensagem pronta para exibir.
 */
fun friendlyErrorMessage(error: Throwable, permissionDenied: String): String = when {
    error is FirebaseFirestoreException &&
            error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED -> permissionDenied
    error is FirebaseFirestoreException &&
            error.code == FirebaseFirestoreException.Code.UNAVAILABLE ->
        "Sem conexão com o servidor."
    else -> error.message ?: "Algo deu errado"
}