package com.joaoandre120.listadecompras.data.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Garante a integridade do catálogo embutido, cujos ids são gravados nos itens. */
class ProductCatalogTest {

    @Test
    fun `ids sao unicos`() {
        val repeated = ProductCatalog.products
            .groupBy { it.id }
            .filterValues { it.size > 1 }
            .keys
        assertTrue("Ids repetidos: $repeated", repeated.isEmpty())
    }

    @Test
    fun `ids usam so minusculas, numeros e hifens`() {
        val pattern = Regex("^[a-z0-9]+(-[a-z0-9]+)*$")
        val invalid = ProductCatalog.products.map { it.id }.filterNot(pattern::matches)
        assertTrue("Ids fora do padrão: $invalid", invalid.isEmpty())
    }

    @Test
    fun `busca ordena por comeca-com e depois alfabetica`() {
        val ids = ProductCatalog.search("leite").map { it.id }
        assertEquals(
            listOf(
                "leite",
                "leite-condensado",
                "leite-condensado-zero-lactose",
                "leite-em-po",
                "leite-zero-lactose",
                "creme-de-leite",
                "creme-de-leite-zero-lactose",
            ),
            ids
        )
    }

}