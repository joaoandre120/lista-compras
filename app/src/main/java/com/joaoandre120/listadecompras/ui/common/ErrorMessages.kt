package com.joaoandre120.listadecompras.ui.common

import com.google.firebase.firestore.FirebaseFirestoreException

/**
 * Indica se [error] é uma recusa das regras de segurança (`PERMISSION_DENIED`).
 *
 * Existe para que ViewModels não precisem conhecer a exceção do SDK.
 *
 * @param error exceção capturada.
 * @return `true` se o acesso foi negado pelas regras.
 */
fun isPermissionDenied(error: Throwable): Boolean =
    error is FirebaseFirestoreException &&
            error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED

/**
 * Converte exceções técnicas em texto próprio para o usuário.
 *
 * @param error exceção capturada.
 * @param permissionDenied texto para `PERMISSION_DENIED`, que depende do contexto da tela.
 * @return mensagem pronta para exibir.
 */
fun friendlyErrorMessage(error: Throwable, permissionDenied: String): String = when {
    isPermissionDenied(error) -> permissionDenied
    error is FirebaseFirestoreException &&
            error.code == FirebaseFirestoreException.Code.UNAVAILABLE ->
        "Sem conexão com o servidor."
    else -> error.message ?: "Algo deu errado"
}