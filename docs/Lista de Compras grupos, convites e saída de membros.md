# **Lista de Compras: grupos, convites e saída de membros**

Respostas às dúvidas sobre múltiplos grupos, convites e saída de membros, com as decisões fechadas até aqui.

## **Vários grupos no MVP ou na V2?**

**O modelo de dados já nasce preparado, e o custo no MVP é pequeno.** Deixar para a V2 só compensa se a modelagem inicial for feita pensando em um único grupo, e aí o retrabalho é grande. Por isso:

- **No banco:** o grupo guarda a lista de membros (groups/\{groupId\} com members), e cada lista aponta para um grupo (groupId). Uma pessoa estar em vários grupos é só aparecer em vários documentos groups. Não há custo extra de modelagem.

- **Na interface:** no MVP, se a pessoa tem só um grupo, o app nem pergunta "para qual grupo?". A tela de escolha só aparece quando houver dois ou mais.

**Conceito envolvido:** pensar em *cardinalidade* (um-para-um versus um-para-muitos) desde o começo. Mudar a cardinalidade depois é o tipo de alteração que mais dói em banco de dados.

Lista privada, nesse modelo, é só uma lista sem grupo (groupId nulo) e com um dono (ownerId).

## **Quem pode convidar**

**Para o MVP, qualquer membro convida.** É a escolha mais simples e combina com o uso familiar. Para não fechar a porta, cada membro pode ter um **papel** (role: "admin" ou "membro") já desde o início, mesmo que todos sejam iguais por enquanto. Mais tarde, restringir convites vira só uma regra nova, sem mudar a estrutura.

## **Quando alguém sai do grupo: aqui há um conflito**

A ideia inicial era que as listas antigas continuassem aparecendo, mas sem acesso às novas. Isso é possível, porém **custa caro**:

- As regras de segurança do Firestore decidem "pode ler?" olhando quem é membro. Se a pessoa sai do grupo, ela deixa de ser membro, e o acesso some para todas as listas dele de uma vez.

- Para manter só as antigas, cada lista precisaria guardar sua própria lista de membros (cópia, ou seja, desnormalização) e seria preciso decidir se aquela pessoa continua recebendo atualizações. Complexidade alta para um MVP.

- Há também um ponto de **privacidade**: se alguém saiu (ou foi removido) de um grupo familiar, ver a lista de compras da casa continuar atualizando seria estranho.

**Sugestão para o MVP:** sair do grupo = perder o acesso a tudo do grupo. As listas **pertencem ao grupo, não a quem as criou**, então continuam existindo para quem ficou. Se um dia houver "histórico para quem saiu", seria um recurso novo, como exportar ou copiar uma lista.

## **Decisões fechadas até aqui**

- **Grupos** (família/amigos), com uma pessoa podendo estar em vários.

- **Listas** pertencem a um grupo, ou são privadas.

- **Convites** são do grupo (não da lista), e qualquer membro pode convidar.

- **Nome:** displayName em users/\{uid\}, e os itens guardam só o uid.

- **Preços e histórico:** ficam para depois, sem fechar portas.

## **Próximo passo**

Confirmar a regra "sair do grupo = perder o acesso a tudo dele" para o MVP. Se confirmada, o próximo tema é **como alguém entra no grupo** (código digitado ou link), que define como o convite será modelado.
