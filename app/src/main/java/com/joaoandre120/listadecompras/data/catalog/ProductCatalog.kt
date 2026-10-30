package com.joaoandre120.listadecompras.data.catalog

import com.joaoandre120.listadecompras.data.model.CatalogProduct
import com.joaoandre120.listadecompras.data.util.normalizedForSearch

/**
 * Fonte do catálogo de produtos embutido no app.
 */
object ProductCatalog {

    val products: List<CatalogProduct> = listOf(
        // Hortifruti
        CatalogProduct("banana", "Banana", "Hortifruti"),
        CatalogProduct("maca", "Maçã", "Hortifruti"),
        CatalogProduct("laranja", "Laranja", "Hortifruti"),
        CatalogProduct("limao", "Limão", "Hortifruti"),
        CatalogProduct("tomate", "Tomate", "Hortifruti"),
        CatalogProduct("cebola", "Cebola", "Hortifruti"),
        CatalogProduct("alho", "Alho", "Hortifruti"),
        CatalogProduct("batata", "Batata", "Hortifruti"),
        CatalogProduct("cenoura", "Cenoura", "Hortifruti"),
        CatalogProduct("alface", "Alface", "Hortifruti"),
        // Mercearia
        CatalogProduct("arroz", "Arroz", "Mercearia"),
        CatalogProduct("feijao", "Feijão", "Mercearia"),
        CatalogProduct("macarrao", "Macarrão", "Mercearia"),
        CatalogProduct("acucar", "Açúcar", "Mercearia"),
        CatalogProduct("sal", "Sal", "Mercearia"),
        CatalogProduct("farinha-de-trigo", "Farinha de trigo", "Mercearia"),
        CatalogProduct("oleo-de-soja", "Óleo de soja", "Mercearia"),
        CatalogProduct("cafe", "Café", "Mercearia"),
        CatalogProduct("molho-de-tomate", "Molho de tomate", "Mercearia"),
        CatalogProduct("biscoito", "Biscoito", "Mercearia"),
        // Laticínios
        CatalogProduct("leite", "Leite", "Laticínios"),
        CatalogProduct("queijo-mussarela", "Queijo mussarela", "Laticínios"),
        CatalogProduct("manteiga", "Manteiga", "Laticínios"),
        CatalogProduct("iogurte", "Iogurte", "Laticínios"),
        CatalogProduct("requeijao", "Requeijão", "Laticínios"),
        // Padaria
        CatalogProduct("pao-frances", "Pão francês", "Padaria"),
        CatalogProduct("pao-de-forma", "Pão de forma", "Padaria"),
        // Carnes e frios
        CatalogProduct("frango", "Frango", "Carnes e frios"),
        CatalogProduct("carne-moida", "Carne moída", "Carnes e frios"),
        CatalogProduct("presunto", "Presunto", "Carnes e frios"),
        CatalogProduct("ovos", "Ovos", "Carnes e frios"),
        // Bebidas
        CatalogProduct("agua-mineral", "Água mineral", "Bebidas"),
        CatalogProduct("suco", "Suco", "Bebidas"),
        CatalogProduct("refrigerante", "Refrigerante", "Bebidas"),
        // Limpeza
        CatalogProduct("detergente", "Detergente", "Limpeza"),
        CatalogProduct("sabao-em-po", "Sabão em pó", "Limpeza"),
        CatalogProduct("papel-higienico", "Papel higiênico", "Limpeza"),
        CatalogProduct("agua-sanitaria", "Água sanitária", "Limpeza"),
        // Higiene
        CatalogProduct("sabonete", "Sabonete", "Higiene"),
        CatalogProduct("creme-dental", "Creme dental", "Higiene"),
        CatalogProduct("shampoo", "Shampoo", "Higiene"),
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