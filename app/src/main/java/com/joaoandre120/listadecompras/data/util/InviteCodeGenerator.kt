package com.joaoandre120.listadecompras.data.util

import java.security.SecureRandom

/**
 * Gera códigos de convite fáceis de digitar e difíceis de adivinhar.
 *
 * O alfabeto exclui caracteres ambíguos (0/O, 1/I/L). Com 8 posições e
 * 31 símbolos, há cerca de 8,5 × 10^11 combinações
 * (31^8 ≈ 8,5 × 10^11 ≈ 852 bilhões de combinações),
 * o que importa porque as regras permitem `get` de qualquer convite cujo
 * código se conheça.
 */
object InviteCodeGenerator {

    private const val ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
    private const val LENGTH = 8
    private val random = SecureRandom()

    /**
     * @return um código novo com [LENGTH] caracteres do [ALPHABET].
     */
    fun generate(): String =
        buildString(LENGTH) { repeat(LENGTH) { append(ALPHABET[random.nextInt(ALPHABET.length)]) } }
}
