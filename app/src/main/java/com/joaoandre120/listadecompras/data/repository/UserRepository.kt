package com.joaoandre120.listadecompras.data.repository

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.joaoandre120.listadecompras.data.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Acesso aos perfis de usuário (`users/{uid}`) no Firestore.
 *
 * @property firestore instância do Firestore (injetável para testes).
 */
class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val users = firestore.collection("users")

    /**
     * Observa em tempo real o perfil de [uid].
     *
     * @param uid uid do usuário.
     * @return fluxo que emite o perfil, ou `null` enquanto o documento não existir.
     */
    fun observeProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        val registration = users.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(UserProfile::class.java))
        }
        awaitClose { registration.remove() }
    }

    /**
     * Grava o nome de exibição, criando o perfil na primeira vez.
     *
     * Criar e atualizar seguem regras diferentes: só a criação envia `createdAt`,
     * e a atualização só pode alterar `displayName`.
     *
     * @param uid uid do dono do perfil (precisa ser o usuário logado).
     * @param name nome já validado (1 a 40 caracteres, sem espaços nas pontas).
     */
    suspend fun saveDisplayName(uid: String, name: String) {
        val ref = users.document(uid)
        if (ref.get().await().exists()) {
            ref.update("displayName", name).await()
        } else {
            ref.set(
                mapOf(
                    "displayName" to name,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    /**
     * Busca os nomes de vários usuários de uma vez.
     *
     * Usa `whereIn` sobre o id do documento, que aceita um número limitado de
     * valores por consulta (hoje 30; confira na documentação), então a lista é
     * dividida em blocos. Cada documento lido conta como 1 leitura na cota.
     *
     * @param uids uids a buscar.
     * @return mapa uid -> nome; uids sem perfil não aparecem no resultado.
     */
    suspend fun getDisplayNames(uids: Collection<String>): Map<String, String> {
        val result = mutableMapOf<String, String>()
        uids.chunked(30).forEach { chunk ->
            val snapshot = users.whereIn(FieldPath.documentId(), chunk).get().await()
            snapshot.documents.forEach { doc ->
                doc.getString("displayName")?.let { result[doc.id] = it }
            }
        }
        return result
    }
}
