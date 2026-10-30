package com.joaoandre120.listadecompras.data.catalog

import com.joaoandre120.listadecompras.data.model.CatalogProduct
import com.joaoandre120.listadecompras.data.util.normalizedForSearch

/**
 * Fonte do catálogo de produtos embutido no app.
 */
object ProductCatalog {

    val products: List<CatalogProduct> = listOf(
        CatalogProduct("arroz", "Arroz", "Mercearia"),
        CatalogProduct("feijao", "Feijão", "Mercearia"),
        CatalogProduct("leite", "Leite", "Laticínios"),
        // ... demais produtos
    )

    /**
     * Busca produtos cujo nome contém o texto digitado, ignorando
     * maiúsculas/minúsculas e acentos ("feijao" encontra "Feijão").
     * Nomes que começam com o texto vêm primeiro.
     *
     * @param query texto digitado pelo usuário.
     * @return produtos que combinam; lista vazia se a consulta estiver em branco.
     */
    fun search(query: String): List<CatalogProduct> {
        val q = query.normalizedForSearch()
        if (q.isBlank()) return emptyList()
        return products
            .filter { it.name.normalizedForSearch().contains(q) }
            .sortedBy { if (it.name.normalizedForSearch().startsWith(q)) 0 else 1 }
    }

}