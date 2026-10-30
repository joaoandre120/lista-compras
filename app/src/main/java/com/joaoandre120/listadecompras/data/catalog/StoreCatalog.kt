package com.joaoandre120.listadecompras.data.catalog

import com.joaoandre120.listadecompras.data.model.CatalogStore
import com.joaoandre120.listadecompras.data.util.normalizedForSearch

/**
 * Fonte do catálogo de supermercados embutido no app.
 */
object StoreCatalog {

    val stores: List<CatalogStore> = listOf(
        CatalogStore("carrefour", "Carrefour"),
        CatalogStore("atacadao", "Atacadão"),
        CatalogStore("assai", "Assaí Atacadista"),
        CatalogStore("casagrande", "Casagrande"),
        CatalogStore("falqueto", "Falqueto"),
        CatalogStore("extrabom", "Extrabom"),
        CatalogStore("bh", "BH"),
        CatalogStore("atacado-vem", "Atacado Vem"),
        // TODO: acrescentar os mercados e redes da sua região
    )

    /**
     * Busca supermercados cujo nome contém o texto, ignorando maiúsculas e acentos.
     * Nomes que começam com o texto vêm primeiro.
     *
     * @param query texto digitado; em branco devolve todos (são poucos).
     * @return supermercados que combinam.
     */
    fun search(query: String): List<CatalogStore> {
        val q = query.normalizedForSearch()
        return stores
            .filter { it.name.normalizedForSearch().contains(q) }
            .sortedBy { if (it.name.normalizedForSearch().startsWith(q)) 0 else 1 }
    }

}