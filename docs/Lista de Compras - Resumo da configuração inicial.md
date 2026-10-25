# **Lista de Compras: resumo da configuração inicial**

Guia de consulta com tudo o que foi feito para preparar o projeto, com comentários sobre o *porquê* de cada passo. Pensado para reler quando bater a dúvida.


## **1. Decisões do projeto**

- **App:** Android (Kotlin + Jetpack Compose), lista de compras compartilhada entre família e amigos.

- **Backend:** Firebase (plano gratuito **Spark**, sem cartão de crédito) com **Firestore** (banco NoSQL de documentos) e login anônimo.

- **Código:** Git no computador, com cópias no **GitHub** (origin) e no **Codeberg** (codeberg).

- **Dois computadores:** casa e trabalho. Cada um tem sua própria chave SSH e sua própria configuração local do Git.

**Por que Firestore?** Já traz modo offline e atualização em tempo real, e o plano gratuito sobra para o nosso uso. Se um dia sentir falta de SQL, Supabase (Postgres) é a alternativa. Os dados do app são simples, então a troca seria viável.

**Limites do plano gratuito (conferidos em out/2026, podem mudar):** 50.000 leituras e 20.000 gravações por dia, 1 GiB de dados. Se estourar, o Firebase **desliga o produto** até o fim do mês em vez de cobrar. O risco de cobrança só existe se você migrar para o plano Blaze. Evite o Cloud Storage, que exige o Blaze.


## **2. Conceitos de Git (glossário rápido)**

- **Repositório:** a pasta do projeto com todo o histórico, guardado dentro da pasta oculta .git.

- **Commit:** uma "foto" do projeto em um momento, com autor, data e mensagem.

- **Branch:** uma linha de desenvolvimento. É só uma etiqueta que aponta para um commit. A principal se chama main.

- **HEAD:** ponteiro que indica onde você está agora. (HEAD -\> main) significa "estou na branch main".

- **Remoto:** um endereço de repositório na internet (GitHub, Codeberg). O apelido padrão é origin.

- **Push / Pull:** enviar seus commits para o remoto / trazer commits do remoto.

- **.gitignore:** lista do que o Git **não** deve versionar (pasta build/, local.properties etc.).

**Git x GitHub:** Git é a ferramenta no seu computador (funciona offline). GitHub e Codeberg são sites que guardam uma cópia online.

**Fluxo básico, sempre o mesmo:**

git add .                          \# escolhe o que entra no commit  
git commit -m "O que foi feito"    \# grava a "foto"  
git push origin main               \# envia ao GitHub  
git push codeberg main             \# envia ao Codeberg

O -m significa *message* (mensagem do commit). Sem ele, o Git abre um editor de texto.


## **3. Configurar o Git**

O Git tem três níveis de configuração. O mais específico vence: **local** (só o repositório) \> **global** (seu usuário do Windows) \> **system** (o computador todo).

\# Preferência pessoal, pode ir no global:  
git config --global init.defaultBranch main  
  
\# Identidade DENTRO da pasta do projeto (sem --global):  
git config user.name "Seu Nome"  
git config user.email "seu@email.com"  
  
\# Ver de onde vem cada valor:  
git config --list --show-origin

**Por que usar a configuração local?** Nome e e-mail ficam gravados em **cada commit para sempre** e vão para o GitHub. Se o global tiver o e-mail da empresa, os commits do projeto pessoal sairiam com ele. A configuração local resolve isso.

**Atenção:** a configuração local fica em .git/config e **não viaja** com o clone. Em cada computador/clone, refaça o user.name e o user.email.

**Por que o init.defaultBranch só funciona no global?** Porque a config local mora dentro do .git, e essa pasta é criada pelo próprio git init. Na hora do init, ela ainda não existe. Alternativas sem global: git init -b main ou, depois, git branch -M main (o -M renomeia a branch atual à força).

**Dica:** o e-mail dos commits fica público no GitHub. Em *Settings → Emails* dá para usar o endereço noreply do GitHub e ocultar o real.


## **4. Chave SSH (autenticação sem senha)**

A chave é um **par de arquivos**: a **privada** (id\_ed25519) nunca sai do seu computador; a **pública** (id\_ed25519.pub) é a que você cadastra nos sites.

ssh-keygen -t ed25519 -C "joao-casa"     \# o rótulo identifica a máquina  
eval "$(ssh-agent -s)"                   \# Git Bash: inicia o agente  
ssh-add ~/.ssh/id\_ed25519                \# carrega a chave no agente

Copiar a pública:

- Git Bash: clip \< ~/.ssh/id\_ed25519.pub

- PowerShell: Get-Content ~/.ssh/id\_ed25519.pub | clip

Cadastre em **GitHub → Settings → SSH and GPG keys → New SSH key** e em **Codeberg → Settings → SSH / GPG Keys**. A mesma pública pode ir nos dois.

Teste:

ssh -T git@github.com      \# esperado: "You've successfully authenticated..."  
ssh -T git@codeberg.org

Na primeira conexão, confira a impressão digital (*fingerprint*) do servidor com a publicada na documentação oficial antes de responder yes.

**O "randomart image"** que aparece ao gerar a chave é só um desenho da impressão digital. Pode ignorar.

**Uma chave por computador.** Em casa e no trabalho, gere chaves **diferentes** e cadastre as duas. Nunca copie a privada de um para o outro. Assim, se um computador for comprometido, você revoga só aquela.

### **Passphrase: manter ou tirar?**

A passphrase protege o **arquivo** da chave caso alguém o copie (a senha do Windows protege só o login). Opções:

1. **Remover:** ssh-keygen -p -f ~/.ssh/id\_ed25519 e deixar a nova em branco. Mais simples, menos seguro.

2. **Manter e digitar uma vez só:** ativar o agente do Windows (PowerShell como administrador):

Get-Service ssh-agent | Set-Service -StartupType Automatic  
Start-Service ssh-agent

Depois, em um PowerShell normal: ssh-add $env:USERPROFILE\\.ssh\\id\_ed25519. Se o Git continuar pedindo, aponte-o para o OpenSSH do Windows: git config --global core.sshCommand "C:/Windows/System32/OpenSSH/ssh.exe"


## **5. Criar o projeto no Android Studio**

- **File → New → New Project → Empty Activity** (Kotlin, Jetpack Compose).

- **Package name** (ex.: com.seunome.listacompras): vira o identificador do app e é usado no Firebase. Escolha com calma.

- **Pasta do projeto sem acentos nem espaços** (ex.: G:\\Projects\\Joao\\ListaCompras). Ferramentas de build do Android às vezes falham com caracteres especiais no caminho.

- O Android Studio já cria um .gitignore adequado.


## **6. Versionar e enviar ao GitHub**

Na pasta do projeto:

git init -b main  
git config user.name "Seu Nome"  
git config user.email "seu@email.com"  
git add .  
git status          \# CONFIRA: não deve listar build/, .gradle/ nem local.properties  
git commit -m "Cria projeto inicial"

No GitHub: **New repository**, privado, e **sem** marcar README, .gitignore ou licença (o repositório precisa nascer vazio, senão o histórico diverge e o primeiro push é recusado).

git remote add origin git@github.com:SEU-USUARIO/lista-compras.git  
git push -u origin main

Use o endereço da aba **SSH**. O -u liga a main local à do remoto, então depois basta git push. O origin é só um apelido.


## **7. Codeberg como segundo remoto**

Crie o repositório vazio no Codeberg e:

git remote add codeberg git@codeberg.org:SEU-USUARIO/lista-compras.git  
git remote -v                     \# deve listar origin e codeberg  
git push -u codeberg main

O Codeberg é voltado a projetos de código aberto: confira os termos de uso se o repositório for privado.

**Se aparecer Connection reset by ... port 22:** o problema é de **rede** (firewall da empresa, roteador ou instabilidade do servidor), e não de chave ou permissão. A conexão caiu antes da autenticação. Diagnóstico: ssh -vT git@codeberg.org, comparar com ssh -T git@github.com e testar outra rede (hotspot do celular). Alternativa: usar HTTPS com **token de acesso** (Codeberg → Settings → Applications → Generate New Token, com permissão de escrita em repositórios):

git remote set-url codeberg https://codeberg.org/SEU-USUARIO/lista-compras.git

No prompt, o usuário é o seu e a "senha" é o **token**. (No seu caso, depois voltou a funcionar.)


## **8. Firebase**

1. Em console.firebase.google.com: **Criar projeto**, Analytics desativado, plano **Spark**.

2. **Registrar app Android** com o applicationId **idêntico** ao de app/build.gradle.kts. SHA-1 pode ficar em branco por ora (só é exigido para login com Google).

3. Baixar o **google-services.json** e colocar na pasta **app/** (no mesmo nível do build.gradle.kts do módulo). No Android Studio, use a visualização **Project** para ver a posição certa.

4. Seguir os trechos do console para o Gradle (os números de versão mudam, copie os atuais):

   - raiz: declarar o plugin com.google.gms.google-services;

   - módulo app: aplicar o plugin e adicionar o **Firebase BoM** + firebase-auth + firebase-firestore;

   - clicar em **Sync Now**.

O **BoM** (Bill of Materials) mantém as versões das bibliotecas do Firebase compatíveis entre si.

### **O google-services.json e o Git**

Ele contém identificadores do projeto. Pela documentação do Firebase, não são segredos como uma senha: a proteção real vem das **regras de segurança do Firestore**. Mesmo assim, no seu caso ele está no .gitignore.

- Em qual .gitignore? Escolha **um**. Na raiz: app/google-services.json. Na pasta app/: só google-services.json.

- **Armadilha:** o .gitignore só vale para arquivos ainda **não rastreados**. Para ver se o Git ainda rastreia: git ls-files app/google-services.json. Se imprimir o caminho, pare de rastrear com git rm --cached app/google-services.json e faça um commit. Atenção: **ele continua nos commits antigos**.

- **Repositório público:** quem clonar precisa criar o próprio projeto Firebase, registrar o app e colocar o próprio google-services.json. Boas práticas: um google-services.json.example, um README com o passo a passo e o arquivo firestore.rules versionado. Antes de abrir o repositório, revise o histórico.

- Quem só **usa o app** (um APK que você gerou) não precisa de nada: conversa com o **seu** backend.


## **9. Rotina entre dois computadores**

O que não foi enviado não aparece na outra máquina. A regra:

- **Ao começar:** git pull antes de mexer em qualquer coisa.

- **Ao terminar:** commit e git push para os **dois** remotos.

Se esquecer o push, o código fica preso no outro computador. Se editar nos dois sem sincronizar, o Git pedirá que você resolva um conflito.


## **10. Preparar outro computador (ex.: casa)**

1. Instalar **Git** e **Android Studio** (o local.properties é gerado sozinho na primeira abertura).

2. Gerar uma **nova chave SSH** e cadastrar a pública no GitHub e no Codeberg, com títulos diferentes.

3. Clonar e adicionar o segundo remoto (o clone configura só **um** remoto):

git clone git@github.com:SEU-USUARIO/lista-compras.git  
cd lista-compras  
git remote add codeberg git@codeberg.org:SEU-USUARIO/lista-compras.git  
git config user.name "Seu Nome"  
git config user.email "seu@email.com"

4. Baixar de novo o **google-services.json** no Firebase (Configurações do projeto → Seus apps) e colocar em app/, porque está no .gitignore e não vem no clone.

5. **Mais adiante**, se usar login com Google: cadastrar o **SHA-1** do certificado de debug deste computador no Firebase (cada máquina tem o seu).


## **11. Erros comuns que apareceram**

- **ls -a falha no PowerShell:** ls é apelido de Get-ChildItem. Use ls -Force. Comandos do Git funcionam igual em PowerShell, cmd e Git Bash. Os que **não** são do Git mudam: eval "$(ssh-agent -s)" e clip \< arquivo são do Git Bash.

- **Acentos quebrados em arquivo criado com echo no cmd:** o cmd grava na página de código do console (CP850), e o editor lê em outra codificação. O Git não tem culpa: guarda os bytes como estão. No projeto real, use sempre **UTF-8** (o Android Studio já faz isso). Dá para checar a página de código com chcp.

- **Aspas dentro do arquivo:** no cmd, o echo grava as aspas junto com o texto.

- **ssh -T com "does not provide shell access":** parece erro, mas é **sucesso**.

- **git diff vazio:** não há mudanças desde o último commit (ou já foram para o git add).

**Como ler um diff** (git log -p): + linha adicionada, - removida, linha sem sinal é contexto; a/ é a versão antiga e b/ a nova; @@ -1 +1,2 @@ indica as linhas afetadas.


## **12. Referência rápida**

git status                  \# o que mudou  
git log --oneline           \# histórico resumido  
git log -p                  \# histórico com as mudanças  
git diff                    \# mudanças ainda não adicionadas  
git remote -v               \# remotos configurados  
git pull origin main        \# trazer novidades  
git push origin main        \# enviar ao GitHub  
git push codeberg main      \# enviar ao Codeberg


## **13. Próximos passos**

1. Ativar o **login anônimo** (Authentication) no Firebase.

2. Criar o **Firestore**. Atenção: a **região** do banco não pode ser alterada depois.

3. Modelar as coleções: users/\{uid\}, lists/\{listId\} (nome, dono, members) e lists/\{listId\}/items/\{itemId\}. Cada item é um **documento separado**, para edições simultâneas não conflitarem.

4. Escrever as **regras de segurança** (só quem está em members lê e escreve).

5. Construir as telas (listas e itens em tempo real) e o **compartilhamento** por código ou link de convite.

6. Backup: no plano gratuito, um script simples com o Admin SDK que exporta as coleções para JSON (confira a documentação sobre exportação gerenciada).
