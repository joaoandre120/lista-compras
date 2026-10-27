# Lista de Compras: modelo de dados e estrutura de pacotes

Documento vivo. Atualizar sempre que o modelo do Firestore ou a organização do código mudar. Última revisão: 27/10/2026.

## 1. Modelo de dados (já com o catálogo)

```
users/{uid}
    displayName: String
    createdAt: Timestamp

groups/{groupId}
    name: String
    createdBy: String            // uid
    createdAt: Timestamp
    memberIds: List<String>      // para consultar "meus grupos"
    roles: Map<String, String>   // uid -> "admin" | "member"
    joinCode: String?            // último código usado numa entrada (só serve às regras)

lists/{listId}
    name: String
    groupId: String?             // null = lista privada
    ownerId: String
    createdAt: Timestamp

lists/{listId}/items/{itemId}
    name: String
    productId: String?           // id do catálogo; null = item digitado à mão
    checked: Boolean
    addedBy: String              // só o uid; o nome vem de users/{uid}
    createdAt: Timestamp

invites/{code}
    groupId: String
    createdBy: String
    expiresAt: Timestamp         // no máximo 7 dias após a criação
    maxUses: Int?
    uses: Int                    // contador de usos, começa em 0
```

## 2. Decisões de arquitetura

- **`memberIds` + `roles` (desnormalização consciente).** O Firestore não consulta chaves de um mapa, mas faz `array-contains` em listas. `memberIds` responde "em quais grupos eu estou?" e `roles` guarda o papel de cada um (porta aberta para restringir convites depois). Entrar ou sair de um grupo deve alterar os dois **na mesma operação**.
- **Itens como subcoleção.** Cada item é um documento separado, então edições simultâneas em itens diferentes não se sobrescrevem.
- **Lista privada = `groupId` nulo.** Sem tipo separado de lista, não há telas nem lógica duplicadas.
- **Catálogo embutido no app, não no Firestore.** É pequeno, só leitura e igual para todos. No Firestore gastaria leituras da cota (50.000/dia) e exigiria regras e tela de administração. O repositório esconde a origem, então migrar depois muda só ele.
- **`name` e `productId` juntos no item.** O nome é copiado, então a lista continua legível se o catálogo mudar. O `productId` abre caminho para preços e histórico. O `id` do catálogo precisa ser **estável** (`"arroz"`, nunca a posição na lista), pois fica gravado para sempre.
- **Sair do grupo = perder acesso a tudo do grupo.** As listas pertencem ao grupo, não a quem as criou.
- **Convites são do grupo.** Qualquer membro convida no MVP. Código primeiro; link (Deep Links) depois, sem mudar o modelo.
- **Custo nas regras de segurança.** Checar o acesso a um item exige `get()` da lista e depois do grupo, e cada `get()` conta como leitura na cota.
- **Coleções nascem sozinhas.** O Firestore não tem esquema: elas aparecem na primeira gravação. Este documento é o contrato, não algo a cadastrar no console.

* **Entrada em grupo com convite (sem Cloud Functions).** A entrada é um lote atômico (`writeBatch`): atualiza `memberIds`, `roles` e `joinCode` no grupo e incrementa `uses` no convite. Cada regra confere a gravação da outra com `getAfter()`, então ninguém consome convite sem entrar, nem entra sem consumir.
* **`joinCode` e `uses`.** O `joinCode` diz às regras qual convite consultar; o `uses` permite que o servidor garanta o `maxUses`. Se um dia migrar para o plano Blaze, uma Cloud Function substitui os dois.
* **Regras de segurança.** Ficam versionadas em `firestore.rules`, na raiz do projeto. Cobrem `users`, `groups`, `lists`, `items` e `invites`.

## 3. Estrutura de pacotes (sugestão)

```
com.seunome.listacompras/
├── data/
│   ├── model/        Group.kt, ShoppingList.kt, ShoppingItem.kt, CatalogProduct.kt, Invite.kt
│   ├── catalog/      ProductCatalog.kt
│   └── repository/   AuthRepository.kt
└── ui/
    └── auth/         AuthViewModel.kt
```

Separar `data` (de onde vêm os dados) de `ui` (o que a tela mostra) é a base do MVVM: a interface nunca importa classes do Firebase diretamente. Fluxo: Compose → ViewModel → Repository → Firebase.

## 4. Pendências do modelo

- `User` (modelo de `users/{uid}`) ainda não tem data class.
- Repositórios de grupos, listas e itens ainda não existem.
- Regras de segurança escritas em firestore.rules; falta validá-las pelo app, com o GroupRepository.
- Preços e histórico de compras ficam para depois.

* Último admin saindo: o grupo pode ficar sem admin; decidir entre bloquear a saída ou promover alguém.
* Excluir grupo ou lista: não há `delete` de grupo no MVP; ao excluir uma lista, o app apaga os itens antes, em lote, porque a subcoleção não some sozinha.
* Confirmar na prática que dois lotes simultâneos com `maxUses = 1` não passam os dois.

## 5. Histórico de alterações

- **26/10/2026:** versão inicial, com o campo `productId` nos itens e o catálogo embutido no app.

* **27/10/2026:** regras de segurança do Firestore escritas; adicionados `joinCode` em `groups`, `uses` em `invites` e o modelo `Invite`.
