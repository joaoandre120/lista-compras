package com.joaoandre120.listadecompras.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Grupo de pessoas (família/amigos) que compartilham listas.
 *
 * @property id identificador do documento (preenchido pelo Firestore).
 * @property name nome exibido do grupo.
 * @property createdBy uid de quem criou o grupo.
 * @property memberIds uids dos membros; usado em consultas `array-contains`.
 * @property roles papel de cada membro: "admin" ou "member".
 * @property createdAt data de criação, preenchida pelo servidor.
 * @property joinCode último código de convite usado numa entrada; existe só
 * para que as regras de segurança identifiquem o convite. O app não o exibe.
 *
 */
data class Group(
    @DocumentId val id: String = "",
    val name: String = "",
    val createdBy: String = "",
    val memberIds: List<String> = emptyList(),
    val roles: Map<String, String> = emptyMap(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    val joinCode: String? = null,
)