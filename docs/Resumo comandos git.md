# Identidade

git config --global user.name "O Teu Nome"

git config --global user.email "email@exemplo.com" 

# Identidade diferente só num repositório (ex.: trabalho)

Tem que estar dentro da pasta do repositório/projeto

git config --local user.email "email@empresa.com" 

# Outras configurações úteis

git config --global init.defaultBranch main

git config --global core.editor "code –wait"

git config --global alias.st status 

# Ver configuração

git config --list \# tudo (merge de todos os níveis)

git config --global --list \# só global

git config --local --list \# só local

git config user.email \# valor efetivo 

# Remover / editar

git config --global --unset user.name

git config --global --edit


Lista **todas as configurações ativas** do Git e mostra, à frente de cada uma, **o ficheiro de onde vem**. É o comando ideal para perceber *porque é que* um valor está a ser usado (global, local, system...).

```
`git config --list –show-origin`


### **Variações úteis**

```
`git config --list --show-origin --global    \# só o ficheiro global`

`git config --list --show-origin --local     \# só o local`

`git config --list --show-origin --system    \# só o system`


`git config --show-origin user.email         \# origem de uma chave específica`

`git config --show-origin --get-all user.email   \# todas as ocorrências dessa chave`


`git config --list --show-scope              \# mostra o nível (system/global/local) em vez do ficheiro`

`git config --list --show-origin --show-scope    \# ambos`
```


**Dica:** como o output costuma ser grande, filtra com `grep`:

```
`git config --list --show-origin | grep user`
```


# Criar e clonar 

git init \# novo repositório

git clone \<url\> \# clonar

git clone \<url\> pasta \# clonar para uma pasta 


# Ciclo do dia a dia

```
`git status                  \# estado dos ficheiros`

`git add ficheiro            \# adicionar ao stage`

`git add .                   \# adicionar tudo`

`git commit -m "mensagem"    \# criar commit`

`git commit -am "mensagem"   \# add + commit (ficheiros já rastreados)`

`git commit --amend          \# alterar o último commit`
```


### **Ciclo do dia a dia (baixar do repositório online)**

```
`git pull                    \# vai até o servidor online, baixa as alterações que a outra máquina enviou e já atualiza os arquivos da sua pasta de trabalho de uma vez só. `


`git fetch                   \# baixa o histórico novo do servidor, mas ~~**não mexe** nos seus arquivos `

`git merge                   \# junta o histórico localmente`
```

`Depois de rodar o ~~**git fetch**, se você executar o ~~**git status**, ele finalmente vai deixar de mentir e dirá algo como: ~~***"Your branch is behind by X commits"*** (Sua branch está atrás por X commits). Aí sim, para aplicar as mudanças, você digita ~~**git pull** ou ~~**git merge**.`


~~**`1. Se você der git pull e houver "incompatibilidade"`**

~~**O Git não vai substituir ou apagar o seu trabalho local. Ele vai tentar juntar as coisas, vai perceber o conflito, vai travar o processo e te dará um aviso em vermelho na tela dizendo algo como:**

> ~~***CONFLICT (content): Merge conflict in nome\_do\_arquivo.txt. Automatic merge failed; fix conflicts and then commit the result.***

~~**Os arquivos em conflito serão modificados pelo Git e ganharão algumas "marcas" visuais no próprio texto para você decidir o que fica e o que sai.**

~~**`2. Se você der git fetch, como ver as incompatibilidades antes?`**

~~**O `git fetch` é o jeito mais seguro de investigar. Após rodar o `git fetch`, o seu Git local passa a conhecer o histórico do servidor (que chamamos geralmente de `origin/main`).**

~~**Para ver o que mudou e se há riscos antes de juntar, você pode usar os seguintes comandos:**

- ~~**Para ver a lista de arquivos que mudaram online:**


> ~~***git diff main origin/main --name-status ***


- ~~**Para ver linha por linha o que mudou em relação ao seu código:**


> `git diff main origin/main`


- ~~***(Se você ver modificações nas mesmas linhas de arquivos que você mexeu localmente, você já sabe que haverá conflito).***



~~**`3. O que o git merge faz? `**

~~**Quando você faz um `git pull`, o que o Git faz por trás dos panos é exatamente um `git fetch` seguido de um `git merge`.**


Se não houver conflitos, o ~~**`git merge`** cria um novo commit automaticamente (chamado "Merge commit") unindo os caminhos das duas máquinas. Se houver conflito, ele pausa o merge e pede a sua ajuda.



~~**Como é um arquivo com conflito e como resolver?**

~~**Se ocorrer o conflito (seja via `pull` ou via `merge`), o Git vai abrir o arquivo e colocar essas marcas textuais exatamente onde está a incompatibilidade:**


> `\<\<\<\<\<\<\< HEAD`

> `Aqui está o texto que você escreveu na MÁQUINA 1 hoje.`

> `=======`

> `Aqui está o texto que você escreveu na MÁQUINA 2 ontem e subiu pro GitHub.`

> `\>\>\>\>\>\>\> origin/main`


~~**`Para resolver, você só precisa:`**

1. ~~**Abrir o arquivo no seu editor de código (como o VS Code, que facilita muito mostrando botões coloridos para clicar).**

2. ~~**Apagar as linhas de marcação (`\<\<\<\<\<\<\<`, `=======`, `\>\>\>\>\>\>\>`).**

3. ~~**Deixar apenas o texto final que você realmente deseja manter (pode ser o da máquina 1, o da máquina 2, ou uma mistura dos dois).**

4. ~~**Salvar o arquivo, fazer um novo commit (`git add .` e `git commit -m "resolvendo conflitos"`) e enviar.**



> ~~**`Conventional Commits** (um padrão para escrever mensagens de commit como ~~**feat:**, ~~**fix:**, ~~**docs:**) `


~~**`A Estrutura Básica`**

~~**A regra principal é começar a mensagem com um tipo (uma palavra-chave em inglês) seguido de dois pontos e o que você fez:**


> `tipo: descrição curta em poucas palavras  
`

~~**Os Principais Tipos que Você Vai Usar**

~~**Você não precisa decorar dezenas de termos. No dia a dia, mais de 90% dos seus commits vão usar apenas estes 4 tipos:**

- ~~**`feat:` (de *feature*) – Quando você está adicionando uma nova funcionalidade ou algo inédito no projeto.**

> ~~***Exemplo:* `feat: adiciona botão de modo escuro`**

- ~~**`fix:` – Quando você está corrigindo um bug ou um erro que estava quebrando o código.**

> ~~***Exemplo:* `fix: corrige alinhamento do texto no celular`**

- ~~**`docs:` – Quando você mexe apenas em documentações, como o arquivo `README.md` ou comentários explicativos.**

> ~~***Exemplo:* `docs: atualiza instruções de como rodar o projeto`**

- ~~**`style:` – Alterações que não mudam o comportamento do código (espaços em branco, formatação, ponto e vírgula faltando, etc.). Não confunda com mudar o CSS/estilo visual, isso geralmente entra em `feat` ou `fix`.**

> ~~***Exemplo:* `style: remove espaços em branco duplicados`**


~~**Outros Tipos Úteis (Para o Futuro)**

~~**Conforme seu projeto crescer entre suas duas máquinas, estes outros três podem te ajudar:**

- ~~**`refactor:` – Quando você muda o código para deixá-lo mais bonito ou mais rápido, mas ele continua fazendo exatamente a mesma coisa que fazia antes.**

- ~~**`test:` – Quando você adiciona ou modifica testes automáticos.**

- ~~**`chore:` – Modificações em arquivos de configuração que não mexem no código principal (como atualizar as ferramentas do projeto)**


~~**Por que vale a pena usar?**

1. ~~**Organização Mental: Ao se obrigar a escolher um tipo (`feat`, `fix`, etc.), você automaticamente se lembra de fazer commits pequenos. Se você tentar escrever `feat: cria tela de login e fix: corrige erro do rodapé`, você vai perceber que deveria ter dividido isso em dois commits.**

2. ~~**Histórico Visual Impecável: Quando você olhar o seu GitHub ou Codeberg daqui a 6 meses, você vai bater o olho e entender toda a evolução do projeto em segundos.**

3. ~~**Padrão de Mercado: É o padrão mais usado por grandes empresas e projetos de código aberto (Open Source). Usar isso no seu projeto pessoal já te treina para o mercado de trabalho**


~~**Um exemplo prático de histórico**

~~**Olha a diferença de como fica um histórico de commits:**

~~**Sem padrão:**

- ~~**ajustes na tela**

- ~~**bug arrumado**

- ~~**atualizando o read me**

- ~~**esqueci uma coisa**


~~**Conventional:**

- ~~**`feat: adiciona campo de telefone no formulário`**

- ~~**`fix: impede que o usuário envie o formulário em branco`**

- ~~**`docs: adiciona o link do codeberg no readme`**

- ~~**`style: formata o arquivo index.html`**



### **Histórico e diferenças**

```
`git log`

`git log --oneline --graph --all`

`git diff                    \# alterações não staged`

`git diff --staged           \# alterações staged`

`git show \<commit\>`

`git blame ficheiro`
```


```
`git log -p`
```

### `Limitar o resultado`

bash

```
`git log -p -3                       \# só os últimos 3 commits`

`git log -p -n 5                     \# igual, últimos 5`

`git log -p --stat                   \# patch + resumo de ficheiros alterados`

`git log -p ficheiro.txt             \# só commits que mexeram nesse ficheiro`

`git log -p -- src/                  \# só commits que mexeram nessa pasta`

`git log -p --author="Nome"          \# por autor`

`git log -p --since="2 weeks ago"    \# por data`

`git log -p --until="2026-01-01"`

`git log -p --grep="bug"             \# pela mensagem do commit`

`git log -p -S"minhaFuncao"          \# commits que adicionaram/removeram esse texto`

`git log -p abc123..def456           \# intervalo entre dois commits`

`git log -p main..minha-branch       \# commits da branch que não estão na main`

`git log -p --no-merges              \# ignora commits de merge`



### **Branches**

```
`git branch                  \# listar locais`

`git branch -a               \# incluir remotas`

`git branch nome             \# criar`

`git switch nome             \# mudar de branch`

`git switch -c nome          \# criar e mudar`

`git branch -d nome          \# apagar (merged)`

`git branch -D nome          \# apagar à força`

`git merge nome              \# juntar nome na branch atual`

`git rebase main             \# reaplicar commits sobre main`
```


### **Remotos**

```
`git remote -v`

`git remote add origin \<url\>`

`git fetch                   \# buscar sem juntar`

`git pull                    \# fetch + merge`

`git pull --rebase           \# fetch + rebase`

`git push`

`git push -u origin main     \# definir upstream`

`git push origin --delete nome`
```


### **Desfazer alterações**

```
`git restore ficheiro            \# descartar alterações locais`

`git restore --staged ficheiro   \# tirar do stage`

`git reset --soft HEAD~1         \# desfaz commit, mantém stage`

`git reset --mixed HEAD~1        \# desfaz commit, mantém ficheiros (por defeito)`

`git reset --hard HEAD~1         \# desfaz commit e apaga alterações ⚠️`

`git revert \<commit\>             \# novo commit que anula outro (seguro p/ partilhados)`

`git clean -fd                   \# apagar ficheiros não rastreados ⚠️`
```


### **Stash e tags**

```
`git stash                   \# guardar trabalho temporariamente`

`git stash list`

`git stash pop               \# recuperar e remover`

`git stash apply             \# recuperar e manter`


`git tag v1.0`

`git tag -a v1.0 -m "versão 1.0"`

`git push origin --tags`
```


### **Ignorar ficheiros (Não monitorar no git)**

- **Local ao repositório:** `.gitignore` (partilhado) ou `.git/info/exclude` (só tu). 

- **Global:** `git config --global core.excludesfile ~/.gitignore\_global`


### **Mudar para um commit sem desfazer os outros**

Usa `git switch --detach` (ou `git checkout`). Isto **só te move** para lá, não apaga nem altera nada no histórico.

```
`git log --oneline                   \# descobre o hash do commit`

`git switch --detach abc123          \# vai para esse commit`

`\# ou, na forma antiga:`

`git checkout abc123`
```


**Para voltar ao presente:**

```
`git switch main                     \# ou a branch onde estavas`


**Se quiseres trabalhar a partir desse commit**, cria uma branch:

```
`git switch -c nova-branch abc123    \# cria branch já nesse commit`

`\# ou, estando em detached HEAD:`

`git switch -c nova-branch`
```


Remove o ficheiro **só do índice (stage)** do Git, ou seja, deixa de o **rastrear**. O ficheiro **continua no disco**.

```
`git rm --cached .idea/misc.xml`

`git commit -m "Para de rastrear .idea/misc.xml"`
```


### **`git checkout .idea/misc.xml`**

Descarta as alterações locais do ficheiro e **restaura a versão do último commit** (ou do índice). Na forma moderna:

```
`git restore .idea/misc.xml`
```

⚠️ Perde as alterações não commitadas nesse ficheiro.


