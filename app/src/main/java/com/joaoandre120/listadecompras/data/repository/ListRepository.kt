package com.joaoandre120.listadecompras.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.joaoandre120.listadecompras.data.model.ShoppingList
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Acesso às listas de compras (`lists/{listId}`) no Firestore.
 *
 * @property firestore instância do Firestore (injetável para testes).
 */
class ListRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val lists = firestore.collection("lists")

    /**
     * Observa em tempo real as listas de um grupo.
     *
     * O filtro `groupId` é obrigatório: as regras só permitem ler listas de grupo
     * se o usuário for membro, e elas precisam do `groupId` na consulta para provar isso.
     *
     * @param groupId id do grupo.
     * @return fluxo que emite a lista atualizada a cada mudança (sem ordem definida);
     * falhas fecham o fluxo com a exceção.
     */
    fun observeGroupLists(groupId: String): Flow<List<ShoppingList>> =
        observe(lists.whereEqualTo("groupId", groupId))

    /**
     * Observa em tempo real as listas privadas de [uid].
     *
     * Os dois filtros são obrigatórios: a regra de lista privada exige
     * `groupId == null` e `ownerId == uid`.
     *
     * @param uid uid do usuário logado.
     * @return fluxo que emite a lista atualizada a cada mudança (sem ordem definida);
     * falhas fecham o fluxo com a exceção.
     */
    fun observePrivateLists(uid: String): Flow<List<ShoppingList>> =
        observe(lists.whereEqualTo("ownerId", uid).whereEqualTo("groupId", null))

    /**
     * Cria uma lista. Grava `groupId` sempre, com `null` para lista privada.
     *
     * @param name nome da lista (1 a 60 caracteres, validado também nas regras).
     * @param ownerId uid de quem cria (precisa ser o usuário logado).
     * @param groupId grupo dono da lista; `null` cria uma lista privada.
     * @return o id da lista criada.
     */
    suspend fun createList(name: String, ownerId: String, groupId: String?): String {
        val ref = lists.document()   // id gerado localmente
        val data = mapOf(
            "name" to name.trim(),
            "groupId" to groupId,
            "ownerId" to ownerId,
            "createdAt" to FieldValue.serverTimestamp()
        )
        ref.set(data).await()
        return ref.id
    }

    /** Transforma uma consulta em fluxo; o listener é removido quando ninguém observa mais. */
    private fun observe(query: Query): Flow<List<ShoppingList>> = callbackFlow {
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObjects(ShoppingList::class.java).orEmpty())
        }
        awaitClose { registration.remove() }
    }
}