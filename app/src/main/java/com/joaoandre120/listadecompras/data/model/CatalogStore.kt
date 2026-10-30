package com.joaoandre120.listadecompras.data.model

/**
 * Supermercado pré-cadastrado no catálogo embutido no app.
 *
 * @property id identificador estável, reservado para uma futura referência nos dados.
 * @property name nome exibido e gravado na lista (cópia, como nos produtos).
 */
data class CatalogStore(
    val id: String,
    val name: String
)