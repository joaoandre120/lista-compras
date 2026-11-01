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

        CatalogProduct("abobrinha", "Abobrinha", "Hortifruti"),
        CatalogProduct("acelga", "Acelga", "Hortifruti"),
        CatalogProduct("pepino", "Pepino", "Hortifruti"),
        CatalogProduct("pimentao", "Pimentão", "Hortifruti"),
        CatalogProduct("brocolis", "Brócolis", "Hortifruti"),
        CatalogProduct("couve", "Couve", "Hortifruti"),
        CatalogProduct("repolho", "Repolho", "Hortifruti"),
        CatalogProduct("batata-doce", "Batata-doce", "Hortifruti"),
        CatalogProduct("mandioca", "Mandioca", "Hortifruti"),
        CatalogProduct("cheiro-verde", "Cheiro-verde", "Hortifruti"),
        CatalogProduct("mamao", "Mamão", "Hortifruti"),
        CatalogProduct("abacaxi", "Abacaxi", "Hortifruti"),
        CatalogProduct("melancia", "Melancia", "Hortifruti"),
        CatalogProduct("uva", "Uva", "Hortifruti"),

        // Mercearia
        CatalogProduct("arroz", "Arroz", "Mercearia"),
        CatalogProduct("feijao", "Feijão", "Mercearia"),
        CatalogProduct("macarrao", "Macarrão", "Mercearia"),
        CatalogProduct("acucar", "Açúcar", "Mercearia"),
        CatalogProduct("sal", "Sal", "Mercearia"),
        CatalogProduct("farinha-de-trigo", "Farinha de trigo", "Mercearia"),
        CatalogProduct("oleo-de-soja", "Óleo de soja", "Mercearia"),
        CatalogProduct("oleo-de-girassol", "Óleo de girassol", "Mercearia"),
        CatalogProduct("oleo-de-coco", "Óleo de coco", "Mercearia"),
        CatalogProduct("cafe", "Café", "Mercearia"),
        CatalogProduct("molho-de-tomate", "Molho de tomate", "Mercearia"),
        CatalogProduct("biscoito", "Biscoito", "Mercearia"),

        CatalogProduct("cha-em-sache", "Chá em sachê", "Mercearia"),
        CatalogProduct("cha-em-folhas_ervas", "Chá em folhas (erva)", "Mercearia"),
        CatalogProduct("sal-grosso", "Sal grosso", "Mercearia"),
        CatalogProduct("sal-rosa", "Sal rosa", "Mercearia"),
        CatalogProduct("amendoim", "Amendoim", "Mercearia"),
        CatalogProduct("farinha-de-mandioca", "Farinha de mandioca", "Mercearia"),
        CatalogProduct("fuba", "Fubá", "Mercearia"),
        CatalogProduct("amido-de-milho", "Amido de milho", "Mercearia"),
        CatalogProduct("fermento-em-po", "Fermento em pó", "Mercearia"),
        CatalogProduct("aveia", "Aveia", "Mercearia"),
        CatalogProduct("azeite", "Azeite", "Mercearia"),
        CatalogProduct("vinagre", "Vinagre", "Mercearia"),
        CatalogProduct("maionese", "Maionese", "Mercearia"),
        CatalogProduct("ketchup", "Ketchup", "Mercearia"),
        CatalogProduct("milho-verde", "Milho verde", "Mercearia"),
        CatalogProduct("atum", "Atum", "Mercearia"),
        CatalogProduct("achocolatado", "Achocolatado", "Mercearia"),
        CatalogProduct("pimenta-do-reino", "Pimenta-do-reino", "Mercearia"),
        CatalogProduct("chocolate-em-po", "Chocolate em pó", "Mercearia"),

        // Laticínios
        CatalogProduct("leite", "Leite", "Laticínios"),
        CatalogProduct("queijo-mussarela", "Queijo mussarela", "Laticínios"),
        CatalogProduct("queijo-zero-lactose", "Queijo zero lactose", "Laticínios"),
        CatalogProduct("manteiga", "Manteiga", "Laticínios"),
        CatalogProduct("iogurte", "Iogurte", "Laticínios"),
        CatalogProduct("requeijao", "Requeijão", "Laticínios"),

        CatalogProduct("leite-em-po", "Leite em pó", "Laticínios"),
        CatalogProduct("leite-zero-lactose", "Leite zero lactose", "Laticínios"),
        CatalogProduct("leite-condensado", "Leite condensado", "Laticínios"),
        CatalogProduct("creme-de-leite", "Creme de leite", "Laticínios"),
        CatalogProduct("queijo", "Queijo", "Laticínios"),
        CatalogProduct("queijo-zero-lactose", "Queijo zero lactose", "Laticínios"),
        CatalogProduct("queijo-parmesao", "Queijo parmesão", "Laticínios"),
        CatalogProduct("margarina", "Margarina", "Laticínios"),
        CatalogProduct("requeijao-zero-lactose", "Requeijão zero lactose", "Laticínios"),

        // Padaria
        CatalogProduct("pao-frances", "Pão francês", "Padaria"),
        CatalogProduct("pao-de-forma", "Pão de forma", "Padaria"),
        CatalogProduct("pao-doce", "Pão doce", "Padaria"),

        // Carnes e frios
        CatalogProduct("frango", "Frango", "Carnes e frios"),
        CatalogProduct("carne-moida", "Carne moída", "Carnes e frios"),
        CatalogProduct("presunto", "Presunto", "Carnes e frios"),
        CatalogProduct("ovos", "Ovos", "Carnes e frios"),

        CatalogProduct("carne-bovina", "Carne bovina", "Carnes e frios"),
        CatalogProduct("alcatra", "Alcatra", "Carnes e frios"),
        CatalogProduct("patinho", "Patinho", "Carnes e frios"),
        CatalogProduct("acem", "Acém", "Carnes e frios"),
        CatalogProduct("costela", "Costela", "Carnes e frios"),
        CatalogProduct("peixe", "Peixe", "Carnes e frios"),
        CatalogProduct("peito-de-frango", "Peito de frango", "Carnes e frios"),
        CatalogProduct("coxa-e-sobrecoxa", "Coxa e sobrecoxa", "Carnes e frios"),
        CatalogProduct("linguica", "Linguiça", "Carnes e frios"),
        CatalogProduct("bacon", "Bacon", "Carnes e frios"),
        CatalogProduct("salsicha", "Salsicha", "Carnes e frios"),

        // Congelados
        CatalogProduct("file-de-peixe-empanado", "Filé de peixe empanado", "Congelados"),
        CatalogProduct("file-de-frango-empanado", "Filé de frango empanado", "Congelados"),
        CatalogProduct("pao-de-queijo", "Pão de queijo", "Congelados"),
        CatalogProduct("sorvete", "Sorvete", "Congelados"),

        // Bebidas
        CatalogProduct("agua-mineral", "Água mineral", "Bebidas"),
        CatalogProduct("suco", "Suco", "Bebidas"),
        CatalogProduct("refrigerante", "Refrigerante", "Bebidas"),

        CatalogProduct("agua-de-coco", "Água de coco", "Bebidas"),
        CatalogProduct("cha-pronto", "Chá pronto", "Bebidas"),
        CatalogProduct("cerveja", "Cerveja", "Bebidas"),
        CatalogProduct("agua-com-gas", "Água com gas", "Bebidas"),
        CatalogProduct("vinho", "Vinho", "Bebidas"),

        // Limpeza
        CatalogProduct("detergente", "Detergente", "Limpeza"),
        CatalogProduct("sabao-em-po", "Sabão em pó", "Limpeza"),
        CatalogProduct("papel-higienico", "Papel higiênico", "Limpeza"),
        CatalogProduct("agua-sanitaria", "Água sanitária", "Limpeza"),

        CatalogProduct("cloro", "Cloro", "Limpeza"),
        CatalogProduct("desinfetante-para-roupa", "Desinfetante para roupa", "Limpeza"),
        CatalogProduct("desinfetante-para-chao", "Desinfetante para chão", "Limpeza"),
        CatalogProduct("amaciante", "Amaciante", "Limpeza"),
        CatalogProduct("sabao-em-barra", "Sabão em barra", "Limpeza"),
        CatalogProduct("multiuso", "Multiuso", "Limpeza"),
        CatalogProduct("alcool", "Álcool", "Limpeza"),
        CatalogProduct("esponja", "Esponja", "Limpeza"),
        CatalogProduct("saco-de-lixo", "Saco de lixo", "Limpeza"),
        CatalogProduct("papel-toalha", "Papel toalha", "Limpeza"),

        // Higiene
        CatalogProduct("sabonete", "Sabonete", "Higiene"),
        CatalogProduct("creme-dental", "Creme dental", "Higiene"),
        CatalogProduct("shampoo", "Shampoo", "Higiene"),

        CatalogProduct("pasta-de-dente", "Pasta de dente", "Higiene"),
        CatalogProduct("sabao-de-coco", "Sabão de coco", "Higiene"),
        CatalogProduct("desodorante", "Desodorante", "Higiene"),
        CatalogProduct("condicionador", "Condicionador", "Higiene"),
        CatalogProduct("escova-de-dente", "Escova de dente", "Higiene"),
        CatalogProduct("fio-dental", "Fio dental", "Higiene"),
        CatalogProduct("absorvente", "Absorvente", "Higiene"),
        CatalogProduct("cotonete", "Cotonete", "Higiene"),

        // Descartáveis
        CatalogProduct("guardanapo", "Guardanapo", "Descartáveis"),
        CatalogProduct("palito-de-dente", "Palito de dente", "Descartáveis"),
        CatalogProduct("papel-aluminio", "Papel-alumínio", "Descartáveis"),
        CatalogProduct("filme-plastico", "Filme plástico", "Descartáveis"),
        CatalogProduct("copo-descartavel", "Copo descartável", "Descartáveis"),

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