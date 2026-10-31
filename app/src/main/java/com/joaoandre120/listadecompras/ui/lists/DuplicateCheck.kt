package com.joaoandre120.listadecompras.ui.lists

import com.joaoandre120.listadecompras.data.model.CatalogProduct
import com.joaoandre120.listadecompras.data.model.ShoppingItem
import com.joaoandre120.listadecompras.data.util.normalizedForSearch

/**
 * Indica se [product] já aparece em [items].
 *
 * Itens vindos do catálogo são comparados pelo `productId`, que sobrevive a renomeações.
 * Itens digitados à mão (`productId` nulo) são comparados pelo nome, ignorando
 * maiúsculas e acentos. Serve só para avisar: nunca bloqueia a adição.
 *
 * @param product produto sugerido.
 * @param items itens atuais da lista.
 * @return `true` se já existe uma linha equivalente.
 */
fun isProductInList(product: CatalogProduct, items: List<ShoppingItem>): Boolean {
    val productName = product.name.normalizedForSearch()
    return items.any { item ->
        if (item.productId != null) item.productId == product.id
        else item.name.normalizedForSearch() == productName
    }
}

/**
 * Indica se já existe um item com exatamente este nome (ignorando maiúsculas e acentos).
 *
 * @param name texto digitado no campo de novo item.
 * @param items itens atuais da lista.
 * @return `true` se algum item tem o mesmo nome; `false` para texto em branco.
 */
fun isNameInList(name: String, items: List<ShoppingItem>): Boolean {
    val query = name.normalizedForSearch()
    return query.isNotEmpty() && items.any { it.name.normalizedForSearch() == query }
}
