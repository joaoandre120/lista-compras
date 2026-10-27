# Guia de Referência do Git

## 1. Configuração e Identidade

### Configurar Identidade Global
```bash
git config --global user.name "O Teu Nome"
git config --global user.email "email@exemplo.com"
```

### Configurar Identidade Local (Apenas neste repositório)
*Nota: Deve ser executado dentro da pasta do repositório/projeto.*
```bash
git config --local user.email "email@empresa.com"
```

### Outras Configurações Úteis
```bash
git config --global init.defaultBranch main
git config --global core.editor "code --wait"
git config --global alias.st status
```

### Verificar Configurações
```bash
git config --list                 # Tudo (merge de todos os níveis)
git config --global --list        # Só nível global
git config --local --list         # Só nível local
git config user.email             # Mostra o valor efetivo atual
```

### Remover ou Editar Configurações
```bash
git config --global --unset user.name
git config --global --edit
```

---

## 2. Rastreamento Avançado de Origem

Lista todas as configurações ativas do Git e mostra, à frente de cada uma, o ficheiro de origem. É o comando ideal para perceber de onde vem um valor (global, local, system).

```bash
git config --list --show-origin
```

### Variações Úteis
```bash
git config --list --show-origin --global    # Só o ficheiro global
git config --list --show-origin --local     # Só o ficheiro local
git config --list --show-origin --system    # Só o ficheiro system
git config --show-origin user.email         # Origem de uma chave específica
git config --show-origin --get-all user.email # Todas as ocorrências dessa chave
git config --list --show-scope              # Mostra o nível (system/global/local) em vez do ficheiro
git config --list --show-origin --show-scope # Mostra ambos (origem e escopo)
```

**Dica:** Como o output costuma ser grande, filtre utilizando o comando `grep`:
```bash
git config --list --show-origin | grep user
```

---

## 3. Criar e Clonar Repositórios

```bash
git init            # Inicializa um novo repositório local
git clone <url>     # Clona um repositório existente
git clone <url> pasta # Clona o repositório para uma pasta específica
```

---

## 4. Ciclo de Trabalho do Dia a Dia

```bash
git status                  # Verifica o estado dos arquivos
git add ficheiro            # Adiciona um arquivo específico ao stage
git add .                   # Adiciona todas as modificações ao stage
git commit -m "mensagem"    # Cria um novo commit com a mensagem
git commit -am "mensagem"   # Faz o add e o commit juntos (apenas para arquivos já rastreados)
git commit --amend          # Altera a mensagem ou o conteúdo do último commit
```

### Desfazer o Git Add (Unstage)
Para tirar um arquivo da Área de Preparação (Stage) sem perder as suas alterações:
```bash
git restore --staged ficheiro  # Forma moderna
git reset HEAD ficheiro        # Forma tradicional
```

### Enviar para os Repositórios Online (Fluxo Duplicado)
```bash
git push origin main        # Envia para o GitHub
git push codeberg main      # Envia para o Codeberg
```

---

## 5. Sincronização e Resolução de Conflitos

### Atualizar o Repositório Local
```bash
git pull     # Baixa as alterações do servidor e atualiza seus arquivos locais imediatamente
git fetch    # Baixa o histórico novo do servidor, mas NÃO mexe nos seus arquivos locais
git merge    # Junta o histórico baixado pelo fetch na sua branch atual
```

*Nota: Após rodar o `git fetch`, o `git status` atualizará o contador informando se o seu branch está atrás (`behind`) do servidor. Para aplicar as mudanças, execute `git pull` ou `git merge`.*

### Entendendo Conflitos ("Incompatibilidades")

#### 1. Se você der `git pull` e houver conflito:
O Git **não vai apagar** o seu trabalho local. Ele travará o processo automaticamente e exibirá um aviso em vermelho:
> *CONFLICT (content): Merge conflict in nome_do_arquivo.txt. Automatic merge failed; fix conflicts and then commit the result.*

Os arquivos em conflito serão marcados visualmente pelo Git para você decidir o que manter.

#### 2. Como inspecionar antes de juntar (Usando `git fetch`):
Após rodar o `git fetch`, você pode analisar as diferenças entre a sua branch local (`main`) e o servidor (`origin/main`) antes de fazer o merge:
```bash
git diff main origin/main --name-status # Lista apenas os arquivos que mudaram online
git diff main origin/main               # Mostra linha por linha o que mudou
```
*Se você notar modificações nas mesmas linhas de arquivos que você alterou localmente, haverá conflito.*

#### 3. Como o arquivo em conflito se parece:
O Git delimitará os blocos incompatíveis com estas marcações:
```text
<<<<<<< HEAD
Aqui está o texto que você escreveu na MÁQUINA 1 hoje.
=======
Aqui está o texto que você escreveu na MÁQUINA 2 ontem e subiu pro GitHub.
>>>>>>> origin/main
```

#### 4. Como resolver o conflito:
1. Abra o arquivo no seu editor de código (como o VS Code).
2. Apague as linhas de marcação (`<<<<<<<`, `=======`, `>>>>>>>`).
3. Deixe apenas o texto final desejado (o da máquina 1, o da máquina 2, ou uma mistura de ambos).
4. Salve o arquivo e finalize criando o commit de resolução:
```bash
git add .
git commit -m "fix: resolve conflitos de merge"
```

---

## 6. Padrão Conventional Commits

Todas as mensagens de commit devem seguir o padrão estruturado: `tipo: descrição curta`.

### Principais Tipos Utilizados:
* **`feat:`** Adiciona uma nova funcionalidade, tela ou nova regra de segurança no `firestore.rules`.
    * *Exemplo:* `feat: adiciona botão de modo escuro`
* **`fix:`** Corrige um bug, erro de compilação, incompatibilidade de dependências (Gradle/Kotlin) ou regras bloqueadas no `firestore.rules`.
    * *Exemplo:* `fix: corrige alinhamento do texto no celular`
* **`docs:`** Alterações exclusivas em documentações, arquivos markdown ou KDocs.
    * *Exemplo:* `docs: atualiza instruções de como rodar o projeto`
* **`style:`** Formatações de código que não alteram seu comportamento (espaços, formatação, ponto e vírgula).
    * *Exemplo:* `style: remove espaços em branco duplicados`

### Outros Tipos Úteis:
* **`refactor:`** Mudanças no código que melhoram sua estrutura/performance sem alterar seu comportamento final.
* **`test:`** Adição ou modificação de testes automáticos.
* **`chore:`** Atualizações de rotina em arquivos de configuração locais ou ferramentas do projeto.

---

## 7. Histórico e Diferenças

```bash
git log                         # Histórico completo de commits
git log --oneline --graph --all # Histórico resumido em formato de gráfico visual
git diff                        # Mostra alterações locais ainda não adicionadas ao stage
git diff --staged               # Mostra alterações que já estão no stage prontas para o commit
git show <commit>               # Detalhes de um commit específico usando o seu hash
git blame ficheiro              # Mostra quem alterou cada linha de um arquivo e quando
git log -p                      # Histórico mostrando as alterações de código (patch) de cada commit
```

### Filtrar e Limitar Resultados do Log
```bash
git log -p -3                       # Apenas os últimos 3 commits
git log -p -n 5                     # Apenas os últimos 5 commits
git log -p --stat                   # Exibe o patch acompanhado do resumo de arquivos alterados
git log -p ficheiro.txt             # Filtra apenas commits que mexeram neste arquivo
git log -p -- src/                  # Filtra apenas commits que mexeram nesta pasta específica
git log -p --author="Nome"          # Filtra por autor do commit
git log -p --since="2 weeks ago"    # Commits das últimas 2 semanas
git log -p --until="2026-01-01"     # Commits feitos até esta data
git log -p --grep="bug"             # Filtra commits pela mensagem de texto
git log -p -S"minhaFuncao"          # Busca commits que adicionaram ou removeram este texto exato
git log -p abc123..def456           # Mostra o intervalo entre dois commits específicos
git log -p main..minha-branch       # Commits da branch atual que ainda não estão na main
git log -p --no-merges              # Ignora commits automáticos de merge do histórico
```

---

## 8. Gerenciamento de Branches

```bash
git branch                  # Lista as branches locais
git branch -a               # Lista as branches locais e remotas (servidores)
git branch nome             # Cria uma nova branch com o nome especificado
git switch nome             # Muda para a branch especificada
git switch -c nome          # Cria uma nova branch e muda para ela imediatamente
git branch -d nome          # Apaga a branch (apenas se ela já tiver passado por merge)
git branch -D nome          # Apaga a branch à força, mesmo com commits não salvos
git merge nome              # Traz e junta o histórico da branch "nome" para a branch atual
git rebase main             # Reaplica os seus commits locais sobre o topo da branch main
```

---

## 9. Gerenciamento de Remotos

```bash
git remote -v                 # Lista os servidores remotos configurados e suas URLs
git remote add origin <url>   # Adiciona um novo servidor remoto padrão
git fetch                     # Busca o histórico dos servidores sem alterar seus arquivos locais
git pull                      # Executa o fetch e o merge automaticamente
git pull --rebase             # Executa o fetch e o rebase automaticamente
git push                      # Envia seus commits para o servidor upstream padrão
git push -u origin main       # Envia os commits e define o servidor/branch padrão (upstream)
git push origin --delete nome # Apaga uma branch diretamente no servidor remoto
```

---

## 10. Desfazer Alterações e Recuperação

```bash
git restore ficheiro            # Descarta as alterações locais e restaura o arquivo ao último commit
git restore --staged ficheiro   # Tira o arquivo da Área de Preparação (Stage)
git reset --soft HEAD~1         # Desfaz o último commit, mantendo os arquivos prontos no stage
git reset --mixed HEAD~1        # Desfaz o commit e o stage, mantendo as alterações nos arquivos (padrão)
git reset --hard HEAD~1         # Desfaz o commit, o stage e DELETA todas as alterações nos arquivos ⚠️
git revert <commit>             # Cria um novo commit que anula as mudanças de um commit anterior (seguro)
git clean -fd                   # Deleta permanentemente arquivos e pastas não rastreados ⚠️
```

---

## 11. Stash (Armazenamento Temporário) e Tags

```bash
git stash                   # Guarda o trabalho inacabado temporariamente para limpar a pasta de trabalho
git stash list              # Lista todos os stashes salvos
git stash pop               # Recupera o último trabalho guardado e o remove da lista do stash
git stash apply             # Recupera o trabalho guardado, mas o mantém salvo na lista do stash

git tag v1.0                # Cria uma tag leve no commit atual
git tag -a v1.0 -m "v1.0"   # Cria uma tag anotada com uma mensagem descritiva
git push origin --tags      # Envia todas as suas tags locais para o servidor remoto
```

---

## 12. Ignorar Arquivos (Não monitorar no Git)

* **Local ao repositório:** Configurado via arquivo `.gitignore` (compartilhado com o projeto) ou no arquivo `.git/info/exclude` (regras locais apenas para a sua máquina).
* **Global para a máquina:** Configurado através do comando:
```bash
git config --global core.excludesfile ~/.gitignore_global
```

---

## 13. Navegação Avançada no Histórico (Detached HEAD)

Para mudar temporariamente para um commit antigo apenas para inspecionar o código, sem alterar ou apagar o histórico atual:

```bash
git log --oneline                   # Descobre o hash do commit desejado
git switch --detach abc123          # Move-se para o commit específico (Forma moderna)
git checkout abc123                 # Move-se para o commit específico (Forma tradicional)
```

### Voltar para o Presente
```bash
git switch main                     # Retorna para a sua branch principal
```

### Criar uma Branch a partir do passado
Se quiser salvar ou trabalhar a partir desse commit antigo, crie uma branch nova:
```bash
git switch -c nova-branch abc123    # Cria a nova branch diretamente apontando para o commit antigo
git switch -c nova-branch           # Se já estiver navegando no commit em detached HEAD
```

---

## 14. Gerenciamento Avançado de Arquivos Rastreados

### Parar de Rastrear um Arquivo (Manter no Disco)
Remove o arquivo da Área de Preparação e do índice do Git, mas mantém o arquivo intacto na sua máquina. Muito útil para arquivos que entraram no Git por erro antes de irem para o `.gitignore`.
```bash
git rm --cached .idea/misc.xml
git commit -m "chore: remove rastreamento do arquivo .idea/misc.xml"
```

### Descartar Alterações Locais de um Arquivo
Descarta as modificações locais de um arquivo específico e restaura exatamente a versão do último commit.
```bash
git restore .idea/misc.xml          # Forma moderna
git checkout .idea/misc.xml         # Forma tradicional
```
*⚠️ Atenção: Este comando destruirá de forma irreversível qualquer alteração não salva neste arquivo.*
