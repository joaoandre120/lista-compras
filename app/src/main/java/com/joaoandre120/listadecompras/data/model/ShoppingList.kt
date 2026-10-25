package com.joaoandre120.listadecompras.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Lista de compras, pertencente a um grupo ou privada.
 *
 * @property groupId id do grupo dono da lista; `null` indica lista privada.
 * @property ownerId uid de quem criou a lista.
 */
data class ShoppingList(
    @DocumentId val id: String = "",
    val name: String = "",
    val groupId: String? = null,
    val ownerId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)