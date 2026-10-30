package com.joaoandre120.listadecompras.data.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.joaoandre120.listadecompras.data.model.ShoppingItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Acesso aos itens de uma lista (`lists/{listId}/items/{itemId}`) no Firestore.
 *
 * @property firestore instância do Firestore (injetável para testes).
 */
class ItemRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /** Subcoleção de itens da lista [listId]. */
    private fun items(listId: String): CollectionReference =
        firestore.collection("lists").document(listId).collection("items")

    /**
     * Observa em tempo real os itens de uma lista.
     *
     * Não precisa de filtro: as regras decidem o acesso pela lista pai, e o
     * `listId` já faz parte do caminho da consulta.
     *
     * @param listId id da lista.
     * @return fluxo que emite os itens a cada mudança (sem ordem definida);
     * falhas fecham o fluxo com a exceção.
     */
    fun observeItems(listId: String): Flow<List<ShoppingItem>> = callbackFlow {
        val registration = items(listId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObjects(ShoppingItem::class.java).orEmpty())
        }
        awaitClose { registration.remove() }
    }

    /**
     * Adiciona um item, desmarcado, à lista.
     *
     * @param listId id da lista.
     * @param name nome do item (1 a 100 caracteres, validado também nas regras).
     * @param addedBy uid de quem adiciona (precisa ser o usuário logado).
     * @param productId id do produto do catálogo; `null` para item digitado à mão.
     * @return o id do item criado.
     */
    suspend fun addItem(
        listId: String,
        name: String,
        addedBy: String,
        productId: String? = null
    ): String {
        val ref = items(listId).document()   // id gerado localmente
        val data = mapOf(
            "name" to name.trim(),
            "productId" to productId,        // chave sempre presente, mesmo nula
            "checked" to false,
            "addedBy" to addedBy,
            "createdAt" to FieldValue.serverTimestamp()
        )
        ref.set(data).await()
        return ref.id
    }

    /**
     * Marca ou desmarca um item. As regras só permitem alterar `checked` e `name`.
     *
     * @param listId id da lista.
     * @param itemId id do item.
     * @param checked novo valor.
     */
    suspend fun setChecked(listId: String, itemId: String, checked: Boolean) {
        items(listId).document(itemId).update("checked", checked).await()
    }

    /**
     * Corrige o nome de um item.
     *
     * @param listId id da lista.
     * @param itemId id do item.
     * @param name novo nome (1 a 100 caracteres).
     */
    suspend fun renameItem(listId: String, itemId: String, name: String) {
        items(listId).document(itemId).update("name", name.trim()).await()
    }

    /**
     * Remove um item da lista.
     *
     * @param listId id da lista.
     * @param itemId id do item.
     */
    suspend fun deleteItem(listId: String, itemId: String) {
        items(listId).document(itemId).delete().await()
    }
}