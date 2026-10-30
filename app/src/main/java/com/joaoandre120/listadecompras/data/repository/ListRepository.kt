package com.joaoandre120.listadecompras.data.repository

import com.google.firebase.firestore.DocumentReference
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

    /**
     * Observa em tempo real o documento de uma lista.
     *
     * Emite `null` quando o servidor confirma que a lista não existe (mais). Offline, um
     * documento fora do cache também aparece como inexistente, então nesse caso nada é
     * emitido: não dá para afirmar que foi excluído. Se as regras negarem a leitura
     * (lista apagada ou acesso perdido), o fluxo fecha com a exceção.
     *
     * @param listId id da lista.
     * @return fluxo com a lista atual, ou `null` se ela foi excluída.
     */
    fun observeList(listId: String): Flow<ShoppingList?> = callbackFlow {
        val registration = lists.document(listId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener
            if (!snapshot.exists() && snapshot.metadata.isFromCache) return@addSnapshotListener
            trySend(snapshot.toObject(ShoppingList::class.java))   // null se não existir
        }
        awaitClose { registration.remove() }
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

    /**
     * Exclui a lista e todos os seus itens.
     *
     * Os itens saem em lotes pequenos e a lista, no último lote. Isso importa por dois
     * motivos: (1) as regras de `items` precisam da lista ainda existente para
     * autorizar cada exclusão; (2) as regras fazem `get()` (lista e grupo) a cada item,
     * e um lote aceita no máximo 20 dessas chamadas, por isso [MAX_ITEMS_PER_DELETE_BATCH].
     * Se algo falhar no meio, a lista continua existindo com menos itens e dá para repetir.
     *
     * Itens criados por outros membros que ainda não chegaram a este aparelho (offline)
     * não são vistos aqui e ficam órfãos, inacessíveis pelas regras.
     *
     * @param listId id da lista a excluir (o usuário precisa ser o dono ou admin do grupo).
     */
    suspend fun deleteListWithItems(listId: String) {
        val listRef = lists.document(listId)
        val itemRefs = listRef.collection("items").get().await().documents.map { it.reference }
        val chunks: List<List<DocumentReference>> =
            itemRefs.chunked(MAX_ITEMS_PER_DELETE_BATCH).ifEmpty { listOf(emptyList()) }

        chunks.forEachIndexed { index, chunk ->
            val batch = firestore.batch()
            chunk.forEach { batch.delete(it) }
            if (index == chunks.lastIndex) batch.delete(listRef)   // a lista sai por último
            batch.commit().await()
        }
    }

    private companion object {
        /** Margem de segurança: no pior caso (lista de grupo) são 2 `get()` por item, mais 2 da lista. */
        const val MAX_ITEMS_PER_DELETE_BATCH = 8
    }

}