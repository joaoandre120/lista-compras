package com.joaoandre120.listadecompras.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.joaoandre120.listadecompras.data.util.InviteCodeGenerator
import com.joaoandre120.listadecompras.data.model.Group
import com.joaoandre120.listadecompras.data.model.Invite
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Acesso aos grupos e convites no Firestore.
 *
 * @property firestore instância do Firestore (injetável para testes).
 */
class GroupRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val groups = firestore.collection("groups")
    private val invites = firestore.collection("invites")

    /**
     * Observa em tempo real os grupos dos quais [uid] é membro.
     *
     * A consulta `whereArrayContains("memberIds", uid)` é obrigatória: as regras
     * só permitem ler grupos que contenham o uid, e o Firestore recusa consultas
     * que poderiam retornar documentos não permitidos.
     *
     * @param uid uid do usuário logado.
     * @return fluxo que emite a lista atualizada a cada mudança; falhas fecham o fluxo com a exceção.
     */
    fun observeMyGroups(uid: String): Flow<List<Group>> = callbackFlow {
        val registration = groups
            .whereArrayContains("memberIds", uid)
            .orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Group::class.java).orEmpty())
            }
        awaitClose { registration.remove() }   // cancela o listener quando ninguém observa mais
    }

    /**
     * Cria um grupo em que [uid] é o único membro e admin.
     *
     * @param name nome do grupo (1 a 60 caracteres, validado também nas regras).
     * @param uid uid de quem cria.
     * @return o id do grupo criado.
     */
    suspend fun createGroup(name: String, uid: String): String {
        val ref = groups.document()   // gera o id localmente, sem ir ao servidor
        val data = mapOf(
            "name" to name.trim(),
            "createdBy" to uid,
            "memberIds" to listOf(uid),
            "roles" to mapOf(uid to "admin"),
            "createdAt" to FieldValue.serverTimestamp()
        )
        ref.set(data).await()
        return ref.id
    }

    /**
     * Cria um convite de 7 dias para o grupo.
     *
     * @param groupId grupo que receberá quem usar o convite.
     * @param uid uid de quem cria (precisa ser membro do grupo).
     * @param maxUses limite de usos; `null` para ilimitado.
     * @return o código do convite.
     */
    suspend fun createInvite(groupId: String, uid: String, maxUses: Int? = null): String {
        val code = InviteCodeGenerator.generate()
        Log.i("GroupRepository", "createInvite: $code")
        val expires = Timestamp(Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7) - TimeUnit.MINUTES.toMillis(5)))
        val data = mapOf(
            "groupId" to groupId,
            "createdBy" to uid,
            "expiresAt" to expires,
            "maxUses" to maxUses,
            "uses" to 0
        )
        invites.document(code).set(data).await()
        return code
    }

    /**
     * Entra em um grupo usando o código de um convite.
     *
     * A entrada é um lote atômico: adiciona o uid ao grupo e incrementa `uses` no
     * convite. As regras exigem os dois juntos (via `getAfter()`); por isso nunca
     * deve ser feita com duas gravações separadas.
     *
     * @param code código digitado pela pessoa (aceita minúsculas e espaços nas pontas).
     * @param uid uid de quem entra.
     * @return o id do grupo em que a pessoa entrou.
     * @throws InviteException se o código não existir, estiver expirado/esgotado ou a pessoa já for membro.
     */
    suspend fun joinWithCode(code: String, uid: String): String {
        val normalized = code.trim().uppercase()
        val invite = invites.document(normalized).get().await().toObject(Invite::class.java)
            ?: throw InviteException("Convite não encontrado")

        if (invite.expiresAt == null || invite.expiresAt.toDate().before(Date()))
            throw InviteException("Convite expirado")
        if (invite.maxUses != null && invite.uses >= invite.maxUses)
            throw InviteException("Convite esgotado")

        val groupRef = groups.document(invite.groupId)
        val batch = firestore.batch()
        batch.update(
            groupRef,
            mapOf(
                "memberIds" to FieldValue.arrayUnion(uid),
                "roles.$uid" to "member",
                "joinCode" to normalized
            )
        )
        batch.update(invites.document(normalized), "uses", FieldValue.increment(1))
        batch.commit().await()
        return invite.groupId
    }

    /**
     * Remove [uid] do grupo, promovendo um sucessor se ele for o último admin.
     *
     * Usa transação para ler o grupo atual no servidor antes de decidir (um snapshot
     * antigo poderia achar que ainda existe outro admin). `memberIds` e `roles` mudam
     * na mesma gravação, como as regras exigem. Se [uid] for admin único e houver
     * outros membros, o mais antigo (primeiro de `memberIds`) vira admin.
     * Se for o único membro, o grupo fica vazio (órfão).
     *
     * @param groupId id do grupo.
     * @param uid uid de quem sai (precisa ser o usuário logado).
     * @throws FirebaseFirestoreException `UNAVAILABLE` sem rede; `PERMISSION_DENIED`
     * se as regras recusarem (ex.: corrida entre dois admins saindo).
     */
    suspend fun leaveGroup(groupId: String, uid: String) {
        val ref = groups.document(groupId)
        firestore.runTransaction { tx ->
            val group = tx.get(ref).toObject(Group::class.java)
            if (group == null || uid !in group.memberIds) return@runTransaction Unit

            val updates = mutableMapOf<String, Any>(
                "memberIds" to FieldValue.arrayRemove(uid),
                "roles.$uid" to FieldValue.delete()
            )
            val others = group.memberIds - uid
            val iAmAdmin = group.roles[uid] == "admin"
            val otherAdminExists = others.any { group.roles[it] == "admin" }
            if (iAmAdmin && others.isNotEmpty() && !otherAdminExists) {
                updates["roles.${others.first()}"] = "admin"
            }
            tx.update(ref, updates)
            Unit
        }.await()
    }

}

/** Erro de negócio ao usar um convite; a mensagem já é própria para exibir. */
class InviteException(message: String) : Exception(message)

/** Erro de negócio em operações de grupo; a mensagem já é própria para exibir. */
class GroupException(message: String) : Exception(message)
