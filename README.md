# Lista de Compras

App Android de lista de compras **compartilhada** entre família e amigos, com atualização em tempo real e funcionamento offline.

> Projeto de estudo em desenvolvimento: o objetivo é aprender Android moderno e arquitetura de software (MVVM) construindo um app real, e também praticar o **Git** no dia a dia (commits atômicos, Conventional Commits, dois remotos e trabalho em dois computadores). Algumas telas ainda são provisórias.

## Funcionalidades

**Já funcionando**

- Login anônimo (Firebase Auth), sem cadastro.
- Perfil com nome de exibição, obrigatório na primeira abertura e editável depois.
- Grupos (família, amigos): uma pessoa pode estar em vários.
- Convite por código de 8 caracteres (válido por 7 dias), compartilhável pela folha de compartilhamento do Android.
- Entrada em grupo pelo código com outro usuário, incluindo o limite de usos do convite (`maxUses`).
- Sair do grupo (membro comum): diálogo de confirmação e perda de acesso ao grupo e às listas dele; as listas continuam para quem ficou.
- Entrada em grupo pelo código, em operação atômica (sem Cloud Functions).
- Listas de cada grupo e listas privadas, em tempo real.
- Navegação com abas (Grupos / Privadas) e telas internas, usando Navigation 3.
- Tela de itens da lista (marcar, editar, remover) em tempo real.
- Catálogo de produtos embutido, com sugestões ao digitar (ignora acentos e maiúsculas).
  - A busca ignora acentos e maiúsculas e prioriza os nomes que começam com o texto digitado (e, entre eles, os mais curtos).
  - Aviso de item já presente na lista, sem bloquear duplicatas: você pode querer duas linhas do mesmo produto, como duas marcas/tipos de arroz.
- Supermercado e data da compra em cada lista, com sugestões de mercados (catálogo embutido) e seletor de data. Ao marcar o primeiro item, a data é preenchida com a de hoje se ainda estiver vazia.
- Indicador de conexão (online/offline) na barra superior.
- Excluir lista.

**Implementado, ainda não testado**

- Sair do grupo sendo **admin**: o código já existe (transação no repositório e regras de segurança). Se o único admin sair e houver outros membros, o membro mais antigo é promovido a admin automaticamente. Falta validar esse fluxo, inclusive a corrida entre dois admins saindo ao mesmo tempo (as regras recusam a gravação que deixaria o grupo sem admin).

**Limitações conhecidas**

- **Grupo órfão:** se o único membro sai, o grupo fica com `memberIds` vazio e não é excluído, mas as regras permitem que alguém entre nele por convite. A limpeza fica para depois, com um script (Admin SDK) ou uma exclusão em cascata.
- **Conta anônima:** ao desinstalar o app ou limpar os dados, o acesso aos grupos é perdido. A vinculação com o login do Google resolve isso, ou backup e restauração.

**Próximos passos**

- Preço, quantidade, marca e tamanho nos itens (campos opcionais no diálogo de edição) e total da lista.
- "Finalizar compra", que grava um retrato da compra no histórico.
- Tela de histórico de preços, comparando o mesmo produto entre mercados e ao longo do tempo.

**Ideias para depois**

- Convite por link (Deep Links), além do código.
- Vincular a conta anônima ao login do Google, para não perder o acesso ao reinstalar o app.
- Backup dos dados.

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

O arquivo `google-services.json` **não** está no repositório. Para rodar o app, você usa o seu próprio projeto Firebase:

1. Clone o repositório:
   ```bash
   git clone https://github.com/joaoandre120/lista-compras.git
   ```
2. No [console do Firebase](https://console.firebase.google.com), crie um projeto (plano Spark, Analytics é opcional).
3. Registre um app Android com o `applicationId` **`com.joaoandre120.listadecompras`** (ou troque o `applicationId` e o `namespace` em `app/build.gradle.kts` pelo seu).
4. Baixe o `google-services.json` e coloque em `app/`, no mesmo nível do `build.gradle.kts` do módulo.
5. Em **Authentication**, ative o provedor **Anônimo**.
6. Em **Firestore Database**, crie o banco (a região não pode ser alterada depois).
7. Publique as regras: copie o conteúdo de `firestore.rules` para a aba **Regras** do Firestore (ou use a Firebase CLI: `firebase deploy --only firestore:rules`).
8. Abra o projeto no Android Studio, sincronize o Gradle e execute em um emulador ou aparelho.

> Dica: cada consulta combinada (por exemplo, `whereArrayContains` com `orderBy`) pode pedir um índice composto. Se isso acontecer, o Logcat mostra um link para criá-lo.

## Limites do plano gratuito

O plano Spark oferece cota diária de leituras e gravações (50 mil e 20 mil, respectivamente, quando este README foi escrito; confira os valores atuais na documentação do Firebase). Cada `get()` dentro das regras conta como uma leitura. Não use Cloud Storage nem Cloud Functions, pois exigem o plano Blaze.

## Contribuindo / fluxo de trabalho

Além do Android, este projeto é um laboratório para aprender Git. Por isso o histórico segue algumas práticas de propósito:

- Commits pequenos e atômicos (um commit por tarefa resolvida), no padrão [Conventional Commits](https://www.conventionalcommits.org/pt-br/): `feat:`, `fix:`, `docs:`, `style:`, `chore:`, `refactor:`, `test:`.
- Dois remotos: o projeto é espelhado no GitHub (`origin`) e no Codeberg (`codeberg`), e cada alteração é enviada aos dois:
  ```bash
  git pull origin main
  git add .
  git commit -m "feat: descrição curta"
  git push origin main
  git push codeberg main
  ```
- Desenvolvimento em dois computadores (casa e trabalho), cada um com a sua chave SSH e a sua configuração local de nome e e-mail. Por isso a regra é: `git pull` antes de começar e `push` ao terminar.
- KDoc em tudo que for criado ou alterado.

## Licença

Defina aqui a licença do projeto (por exemplo, MIT) e adicione o arquivo `LICENSE`. Enquanto não houver licença, o código é de uso reservado por padrão, mesmo sendo público.