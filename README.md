# Lista de Compras

App Android de lista de compras **compartilhada** entre família e amigos, com atualização em tempo real e funcionamento offline.

> Projeto de estudo em desenvolvimento: o objetivo é aprender Android moderno e arquitetura de software (MVVM) construindo um app real. Algumas telas ainda são provisórias.

## Funcionalidades

**Já funcionando**

- Login anônimo (Firebase Auth), sem cadastro.
- Perfil com nome de exibição, obrigatório na primeira abertura e editável depois.
- Grupos (família, amigos): uma pessoa pode estar em vários.
- Convite por código de 8 caracteres (válido por 7 dias), compartilhável pela folha de compartilhamento do Android.
- Entrada em grupo pelo código, em operação atômica (sem Cloud Functions).
- Listas de cada grupo e listas privadas, em tempo real.
- Navegação com abas (Grupos / Privadas) e telas internas, usando Navigation 3.

**Em desenvolvimento / planejado**

- Tela de itens da lista (marcar, editar, remover) em tempo real.
- Catálogo de produtos embutido, com sugestões ao digitar (ignora acentos e maiúsculas).
- Excluir lista, sair do grupo na interface e tratamento do "último admin".
- Convite por link (Deep Links), preços e histórico de compras, backup.

## Stack

| Área | Tecnologia |
|---|---|
| Linguagem | Kotlin |
| Interface | Jetpack Compose + Material 3 |
| Arquitetura | MVVM, State Hoisting, repositórios sobre o SDK |
| Navegação | Navigation 3 (`navigation3-ui`, `navigation3-runtime`) |
| Backend | Firebase (plano gratuito Spark): Auth anônimo + Firestore |
| Assíncrono | Coroutines e Flow (`callbackFlow` para dados ao vivo) |
| Build | Gradle (Kotlin DSL) com catálogo de versões `libs.versions.toml` |

Requisitos: Android Studio recente, JDK 21 (toolchain do Gradle) e `minSdk 24`.

## Arquitetura

```
app/src/main/java/com/joaoandre120/listadecompras/
├── data/
│   ├── catalog/       # ProductCatalog (catálogo embutido)
│   ├── model/         # Group, ShoppingList, ShoppingItem, Invite, UserProfile...
│   ├── repository/    # Auth, User, Group e List repositories (escondem o Firebase)
│   └── util/          # InviteCodeGenerator
└── ui/
    ├── groups/        # GroupsScreen + GroupsViewModel
    ├── lists/         # ListsScreen, ListItemsScreen + ViewModels
    ├── navigation/    # Route, NavigationState, Navigator, AppNavDisplay, barras
    └── theme/
```

Padrões seguidos:

- **Telas em três partes:** `XScreen` (ViewModel, coleta de estado e eventos), `XScreenContent` (stateless, só dados e callbacks) e `@Preview` que usa apenas o `XScreenContent`.
- **ViewModel:** estado único e imutável em `StateFlow`; mensagens pontuais consumidas com `messageShown()`.
- **Repositório:** esconde o SDK do Firebase; `Flow` para dados ao vivo e `suspend` para ações pontuais.

### Modelo de dados (Firestore)

```
users/{uid}                    displayName, createdAt
groups/{groupId}               name, createdBy, memberIds, roles, joinCode, createdAt
lists/{listId}                 name, groupId (null = privada), ownerId, store, purchaseDate, createdAt
lists/{listId}/items/{itemId}  name, productId, checked, addedBy, createdAt
invites/{code}                 groupId, createdBy, expiresAt, maxUses, uses
```

Decisões importantes:

- Cada item é um documento próprio (subcoleção), para edições simultâneas não se sobrescreverem.
- `memberIds` e `roles` são desnormalizados de propósito e mudam sempre na mesma operação.
- Sair do grupo significa perder o acesso a tudo dele; as listas continuam para quem ficou.
- Sem Cloud Functions: a entrada em grupo é um `writeBatch` validado pelas regras com `getAfter()`.

As regras de segurança estão em [`firestore.rules`](firestore.rules). Lembre que **regras não são filtros**: toda consulta precisa trazer o filtro que prova o acesso (por exemplo, `whereArrayContains("memberIds", uid)`).

## Como rodar (crie o seu próprio Firebase)

O arquivo `app/google-services.json` **não** está no repositório. Para rodar o app, você usa o seu próprio projeto Firebase:

1. Clone o repositório:
   ```bash
   git clone https://github.com/joaoandre120/lista-compras.git
   ```
2. No [console do Firebase](https://console.firebase.google.com), crie um projeto (plano Spark, Analytics é opcional).
3. Registre um app Android com o `applicationId` **`com.joaoandre120.listadecompras`** (ou troque o `applicationId` e o `namespace` em `build.gradle.kts` pelo seu).
4. Baixe o `app/google-services.json` e coloque em ``, no mesmo nível do `build.gradle.kts` do módulo.
5. Em **Authentication**, ative o provedor **Anônimo**.
6. Em **Firestore Database**, crie o banco (a região não pode ser alterada depois).
7. Publique as regras: copie o conteúdo de `firestore.rules` para a aba **Regras** do Firestore (ou use a Firebase CLI: `firebase deploy --only firestore:rules`).
8. Abra o projeto no Android Studio, sincronize o Gradle e execute em um emulador ou aparelho.

> Dica: cada consulta combinada (por exemplo, `whereArrayContains` com `orderBy`) pode pedir um índice composto. Se isso acontecer, o Logcat mostra um link para criá-lo.

## Limites do plano gratuito

O plano Spark oferece cota diária de leituras e gravações (50 mil e 20 mil, respectivamente, quando este README foi escrito; confira os valores atuais na documentação do Firebase). Cada `get()` dentro das regras conta como uma leitura. Não use Cloud Storage nem Cloud Functions, pois exigem o plano Blaze.

## Contribuindo / fluxo de trabalho

- Commits pequenos e atômicos, no padrão [Conventional Commits](https://www.conventionalcommits.org/pt-br/): `feat:`, `fix:`, `docs:`, `style:`, `chore:`.
- KDoc em tudo que for criado ou alterado.
- O projeto é espelhado no GitHub e no Codeberg.

## Licença

Defina aqui a licença do projeto (por exemplo, MIT) e adicione o arquivo `LICENSE`. Enquanto não houver licença, o código é de uso reservado por padrão, mesmo sendo público.