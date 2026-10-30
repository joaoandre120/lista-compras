package com.joaoandre120.listadecompras.data.util

import java.text.Normalizer

/**
 * Normaliza um texto para comparação em buscas: minúsculas, sem espaços nas pontas e sem acentos.
 *
 * O NFD separa a letra do acento (ã vira a + ~), e o regex remove as marcas (\p{Mn}).
 *
 * @return texto pronto para comparar com `contains` e `startsWith`.
 */
fun String.normalizedForSearch(): String =
    Normalizer.normalize(trim().lowercase(), Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
