# **Lista de Compras: convite por código e por link**

Como oferecer os dois modos de convite no mesmo modelo, o que cada um exige e em que ordem implementar.

## **Como funciona o modelo**

Os dois modos usam o mesmo mecanismo por baixo: **o link é só o código embrulhado numa URL**. Basta um documento de convite, por exemplo invites/\{code\}, com groupId, createdBy, expiresAt e, se quiser, maxUses. O que muda é só **como o código chega ao app**:

- **Código digitado:** a pessoa digita K7M2QX, o app busca invites/K7M2QX, descobre o grupo e adiciona o uid dela em members.

- **Link:** https://seudominio/convite/K7M2QX. Ao tocar, o Android abre o app, que extrai o código do final da URL e faz a **mesma busca**.

A lógica de entrar no grupo é escrita uma vez e reaproveitada.

## **O que cada modo exige**

**Código** é simples: um campo de texto e uma consulta. Escolha um alfabeto sem caracteres ambíguos (sem 0/O, 1/I) para facilitar a digitação.

**Link** traz um conceito novo de Android: **Deep Links / App Links**. É preciso declarar um intent-filter na Activity e, para o link abrir direto no app (sem perguntar), publicar um arquivo assetlinks.json num domínio próprio, provando que o domínio e o app são seus. Dois cuidados:

- **Precisa de um domínio.** Uma saída gratuita é o Firebase Hosting no plano Spark, que dá um endereço \*.web.app.

- **Se a pessoa não tem o app instalado**, o link cai numa página web. Hoje ela só poderia mostrar "baixe o app", porque o Firebase Dynamic Links, que resolvia isso, foi descontinuado (confira a situação atual na documentação).

Para **compartilhar** o link ou o código, use a folha de compartilhamento do Android (Intent.ACTION\_SEND), que já envia por WhatsApp e afins sem integração extra.

## **Ponto de segurança ligado ao plano gratuito**

No plano Spark não há Cloud Functions (exigem o Blaze). Por isso, "entrar no grupo" precisa ser permitido pelas **regras de segurança** do Firestore: um usuário só pode adicionar a si mesmo em members se existir um convite válido. É viável, mas é a parte mais delicada das regras e merece atenção quando chegarmos nela.

## **Ordem sugerida**

1. **MVP:** modelar invites já pensando nos dois modos, mas implementar só o **código** (tela mais simples, sem domínio nem assetlinks.json).

2. **Depois:** acrescentar o link por cima, sem mexer no modelo.

Assim nenhuma porta fica fechada, e Deep Links são aprendidos com o app já funcionando.

## **Pendências**

- Atualizar o "Próximo passo" do documento de grupos para refletir convite por código e por link, com o código primeiro.

- Decidir se um domínio \*.web.app do Firebase Hosting serve para o futuro link de convite.
