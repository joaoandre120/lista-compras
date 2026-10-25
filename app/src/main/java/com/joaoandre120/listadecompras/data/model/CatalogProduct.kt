package com.joaoandre120.listadecompras.data.model

/**
 * Produto pré-cadastrado no catálogo embutido no app.
 *
 * @property id identificador estável; é gravado nos itens, então nunca deve mudar.
 * @property name nome exibido ao usuário.
 * @property category categoria usada para agrupar as sugestões (ex.: "Hortifruti").
 */
data class CatalogProduct(
    val id: String,
    val name: String,
    val category: String
)