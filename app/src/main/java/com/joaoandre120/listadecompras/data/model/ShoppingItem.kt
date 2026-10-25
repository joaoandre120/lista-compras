package com.joaoandre120.listadecompras.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Item de uma lista de compras.
 *
 * @property name nome exibido (cópia do catálogo ou texto digitado).
 * @property productId id do [CatalogProduct]; `null` indica item digitado livremente.
 * @property checked `true` quando o item já foi comprado.
 * @property addedBy uid de quem adicionou o item.
 */
data class ShoppingItem(
    @DocumentId val id: String = "",
    val name: String = "",
    val productId: String? = null,
    val checked: Boolean = false,
    val addedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)