# Planejamento do App: Fluxo de Compartilhamento e Fases do Produto

## 1. O Fluxo de Compartilhamento ("Sempre Juntos, mas com Privacidade")

A ideia central é que, após o convite, as listas sejam compartilhadas por padrão (com opção de criar listas privadas).

### Como isso funciona na prática e no Firebase:
* **O "Vínculo de Amizade":** Quando você convida alguém, o Firebase precisa registrar que o `Usuário A` e o `Usuário B` estão conectados.
* **Criação de Listas:** Quando o usuário clica em "Nova Lista", o app já deixa marcado um botão do tipo *"Compartilhar com [Nome do Amigo]"*. Se ele desmarcar, a lista nasce privada.
* **Impacto na Arquitetura:** Cada lista no Firestore precisará de uma lista de membros (ex: `members: [uid1, uid2]`). Se for privada, terá apenas o `uid` do criador. Isso deixa o app pronto para o compartilhamento desde o dia um, mesmo que você use o login anônimo primeiro.

---

## 2. A "Escalada" do Produto (MVP vs. Futuro)

Para não sobrecarregar o aprendizado simultâneo de desenvolvimento Android, Firebase, Arquitetura MVVM e Telas Complexas, o aplicativo será dividido em fases estratégicas:

### 🚀 Fase 1: O Mínimo Viável (MVP)
* **Objetivo:** Fazer o básico funcionar perfeitamente de ponta a ponta.
* **O Item da Lista:** Terá apenas os campos `id`, `nome` e `comprado (boolean)`.
* **A Lista:** Terá os campos `id`, `nome`, `criador_id` e `membros`.
* **Fluxo de Navegação:** Tela com todas as suas listas -> Tela interna da lista -> Adicionar item (apenas nome) -> Marcar item como feito.

### 📈 Fase 2: Inteligência e Detalhes (Preço, Quantidade e Histórico)
* **Objetivo:** Adicionar os recursos avançados sem quebrar a estrutura que já foi construída.
* **O Item da Lista:** Ganha novos campos como `quantidade`, `preco_unitario`, `categoria` e `quem_comprou`.
* **Histórico de Compras:** Quando uma lista for marcada como "Finalizada", ela sai da tela principal e vai para uma coleção de `historico` no Firebase, permitindo gerar relatórios de gastos futuros.

---

## 3. Decisões do Primeiro Passo: Identidade e Login Anônimo

Como os usuários vão compartilhar listas, eles precisam se identificar de alguma forma para evitar uma confusão de códigos técnicos na interface. Para organizar a estrutura do banco de dados (Firestore), definimos os seguintes pontos:

### Questões de Modelagem:
1. **Identificação na Lista:** Quando duas pessoas estiverem na mesma lista, deve aparecer o nome/apelido de quem adicionou ou marcou o item? *(Ex: "Leite (Adicionado por Carlos)")*
2. **Abordagem de Autenticação:** Pensando na limitação do login anônimo (se o usuário apagar o app, ele perde a conta e o acesso às listas compartilhadas), qual caminho seguir para o aprendizado?

### ✨ Decisão Fechada do Projeto:
**Começar com login anônimo, com a possibilidade futura de o usuário realizar um login completo (E-mail ou Google) e “sincronizar/vincular” os dados anônimos existentes.**
