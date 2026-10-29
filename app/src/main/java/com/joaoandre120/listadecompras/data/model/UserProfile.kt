package com.joaoandre120.listadecompras.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Perfil público de um usuário, guardado em `users/{uid}`.
 *
 * O nome evita confusão com `FirebaseUser`, que representa a conta de login.
 *
 * @property uid identificador do documento (é o uid do Firebase Auth).
 * @property displayName nome exibido para os outros membros (1 a 40 caracteres).
 * @property createdAt data de criação, preenchida pelo servidor.
 */
data class UserProfile(
    @DocumentId val uid: String = "",
    val displayName: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
