package com.joaoandre120.listadecompras.data.model

import com.google.firebase.Timestamp

/**
 * Convite para entrar em um grupo. O código é o id do documento.
 *
 * @property groupId grupo ao qual o convite dá acesso.
 * @property createdBy uid de quem criou o convite.
 * @property expiresAt data de expiração (no máximo 7 dias após a criação).
 * @property maxUses limite de usos; `null` significa sem limite.
 * @property uses quantas vezes o convite já foi usado; só sobe via lote de entrada.
 */
data class Invite(
    val groupId: String = "",
    val createdBy: String = "",
    val expiresAt: Timestamp? = null,
    val maxUses: Int? = null,
    val uses: Int = 0
)