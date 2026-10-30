package com.joaoandre120.listadecompras.ui.lists

import com.joaoandre120.listadecompras.data.model.ShoppingList
import com.joaoandre120.listadecompras.data.util.IsoDate

/**
 * Resume supermercado e data da compra em uma linha.
 *
 * @param list lista a resumir.
 * @return texto como "Supermercado: Extra · Data: 30/10/2026"; `null` se nada foi informado.
 */
fun purchaseSummary(list: ShoppingList): String? =
    listOfNotNull(
        list.store?.let { "Supermercado: $it" },
        list.purchaseDate?.let { "Data: ${IsoDate.display(it)}" }
    ).joinToString(" · ").ifEmpty { null }
