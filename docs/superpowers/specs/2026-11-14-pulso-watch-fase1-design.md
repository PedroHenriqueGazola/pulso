# Pulso — App do relógio (Fase 1) — Design

## Intenção

O Pedro trocou um Galaxy S21 FE por um iPhone 17 e o Galaxy Watch 6 parou de receber
notificações: a Samsung não suporta iPhone no Wear OS. Produtos como o Merge Watch
resolvem isso por assinatura; o WearBridge (open source, MIT) resolve parcialmente.

O Pulso é o projeto pessoal do Pedro para isso: uso próprio (sideload, sem loja), publicado
no GitHub como projeto de portfólio. O objetivo de longo prazo é chegar perto do Merge;
esta fase cobre só o que dá para fazer **sem app no iPhone**.

## Contexto técnico

- O iPhone expõe dois serviços BLE de sistema para acessórios pareados:
  - **ANCS** (Apple Notification Center Service): todas as notificações do iPhone,
    com ações positiva/negativa.
  - **AMS** (Apple Media Service): estado da mídia tocando e comandos (play/pause,
    próxima, anterior, volume).
- O relógio atua como **BLE central/cliente GATT**, faz bond com o iPhone e se inscreve
  nesses serviços. Nenhum app iOS é necessário.
- O iOS pede "Compartilhar notificações do sistema?" no primeiro bond.

## Metas (Fase 1)

1. **Notificações**: toda notificação do iPhone aparece no relógio com app de origem,
   título e texto, vibra e aparece como pop-up (heads-up).
2. **Dispensar**: dispensar no relógio remove a notificação no iPhone (ação negativa do ANCS).
   Notificação removida no iPhone some do relógio.
3. **Ligações**: ligação recebida abre tela cheia no relógio com nome/número, botões
   atender e recusar (ações do ANCS). Ao atender no iPhone, a tela muda para
   "em ligação no iPhone"; ao terminar, some.
4. **Música (AMS)**: tela/tile no relógio com faixa, artista, play/pause, próxima,
   anterior e volume, refletindo o que toca no iPhone.
5. **Conexão resiliente**: reconecta sozinho quando o iPhone sai e volta do alcance,
   e sobe sozinho depois de reiniciar o relógio.
6. **Tela de status**: estado da conexão, nome do iPhone, botão de parear/esquecer.

## Não-metas (Fase 1)

- Resposta rápida a mensagens (ANCS não suporta; depende de app iOS/Atalhos).
- Contatos, saúde → Apple Saúde, câmera remota, "encontrar iPhone" — Fase 2, com app iOS.
- Áudio da ligação no relógio.
- Publicar na Play Store; múltiplos iPhones; múltiplos relógios.

## Anti-metas (falha mesmo se "funcionar")

- Vibrar em rajada ao reconectar (reenvio de notificações antigas — gotcha conhecido
  do WearBridge).
- Drenar a bateria do relógio de forma perceptível em um dia normal.
- Notificações chegando sem pop-up / silenciosas.
- Código "Deus": um serviço de 800+ linhas que faz tudo.

## Restrições

- Hardware: Galaxy Watch 6 (Wear OS 4 / One UI Watch), iPhone 17 (iOS 26).
- O relógio continua configurado com o S21 FE; **não resetar** (setup inicial exige Android).
- Instalação via `adb` (Wi‑Fi debugging). `adb` e Android SDK já existem no Mac do Pedro.
- Kotlin, Jetpack Compose for Wear OS, minSdk 33, Java 17.
- Estilo de código do Pedro (`~/.claude/CLAUDE.md`): nomes sem abreviação, funções
  pequenas, guard clauses, sem `else` encadeado, imutabilidade, sem comentários de "o quê".
- Licença MIT; crédito ao WearBridge no README pelo código de protocolo reaproveitado.

## Abordagens consideradas

| Abordagem | Trade-off |
|---|---|
| A) Fork do WearBridge | Mais rápido, mas vira "fork" e herda um `AncsService` de 858 linhas fora do estilo do Pedro. |
| B) Do zero, só lendo o WearBridge | 100% próprio, mas reescreve parsers já testados e arrisca repetir bugs resolvidos. |
| **C) Projeto novo + reaproveitar só o protocolo** ✅ | Escolhida pelo Pedro. Repo próprio e limpo; traz os parsers ANCS testados e os 16 gotchas como checklist. |

## Design

### O que vem do WearBridge (adaptado ao estilo)

- `NotificationSourceParser`, `DataSourceAssembler`, `ControlPointWriter` + seus testes
  (~420 linhas). Renomear pacotes/nomes conforme o estilo, manter comportamento.
- `.claude/rules/gotchas.md` vira `docs/gotchas.md` e checklist de aceite. Os mais
  relevantes: `MessagingStyle` para o pop-up da Samsung; group key única por notificação;
  canais versionados; não usar `setNotificationSilent`; pular notificações pré-existentes
  ao reconectar; MODIFIED (não REMOVED) quando a ligação é atendida;
  `USE_FULL_SCREEN_INTENT` via `appops`.

### Módulos (pacotes dentro de um único módulo Gradle `app`)

- `bluetooth/` — scan, bond, conexão GATT, reconexão com backoff. Expõe estado como `StateFlow`.
  Não sabe nada de ANCS/AMS.
- `ancs/` — UUIDs, parsers, montagem de atributos, envio de ações. Entrada: bytes;
  saída: eventos de domínio (`NotificationAdded`, `NotificationRemoved`, `NotificationModified`).
  Puro Kotlin, testável sem Android.
- `ams/` — UUIDs, registro de atributos de mídia, parser de Entity Update, comandos remotos.
  Puro Kotlin, testável sem Android.
- `notifications/` — transforma eventos ANCS em notificações Wear OS (canais, ícone por app,
  `MessagingStyle`, vibração por categoria, ação "dispensar").
- `calls/` — tela de ligação recebida e seus estados (tocando → em ligação no iPhone → encerrada).
- `media/` — tela de música (e, se simples, um Tile) consumindo o estado do AMS.
- `service/` — foreground service (`connectedDevice`) que só orquestra: liga `bluetooth`
  a `ancs`/`ams` e entrega eventos para `notifications`/`calls`/`media`. Boot receiver.
- `ui/` — tela de status/pareamento.

Regra: lógica de protocolo nunca importa APIs do Android; o serviço não contém regra de negócio.

### Testes

- Unitários (JUnit) para `ancs/` e `ams/`: parsing, montagem de atributos fragmentados,
  comandos gerados. Nomes no formato `should ... when ...`.
- Validação no hardware real é o critério de aceite de cada meta (roteiro manual em
  `docs/manual-test.md`): notificação de WhatsApp, iMessage, e-mail; dispensar dos dois lados;
  ligação recebida/atendida/recusada; Spotify/Apple Music; desligar Bluetooth do iPhone e
  religar; reiniciar o relógio.

### Ordem sugerida de entrega

1. Esqueleto do projeto + build + instalar "hello" no relógio via adb.
2. `bluetooth/` + bond com o iPhone + tela de status.
3. ANCS → notificações no relógio (meta 1).
4. Dispensar (meta 2) e reconexão sem rajada (meta 5).
5. Ligações (meta 3).
6. AMS / música (meta 4).

## Decisões do Pedro

- **Dispensar**: só o botão "Dispensar" da notificação no relógio envia a ação negativa ao
  iPhone. Arrastar/limpar a lista do relógio afeta só o relógio.
- **Foco / Não Perturbe**: seguir o iPhone. Notificação com a flag `Silent` do ANCS aparece no
  relógio, mas sem vibrar e sem pop-up.
- **Ligações de WhatsApp/FaceTime**: tratar como ligação (tela cheia + vibração), seguindo
  a mesma ideia do WearBridge, mas com a detecção isolada e dirigida por dados:
  - ligação = categoria ANCS `IncomingCall` **ou** (app em lista de apps de chamada **e**
    título/texto casa com um padrão de chamada);
  - padrões em **português e inglês** (o iPhone do Pedro está em PT-BR; o WearBridge só
    tem padrões em inglês), num único arquivo de configuração, com teste unitário por padrão;
  - no início, registrar em log as notificações dos apps de chamada para capturar o texto
    real que o WhatsApp/FaceTime mandam em PT-BR e ajustar os padrões.
  - Ação "atender" para WhatsApp/FaceTime pode não funcionar via ANCS (depende de o iOS
    expor ação positiva); nesse caso a tela mostra só "recusar" / "ver no iPhone".

## Decisões tomadas pelo Claude (revisáveis)

- **Pré-requisito de ambiente**: antes do primeiro teste, desligar o Bluetooth do S21 FE
  (o relógio continua configurado, só sem o celular Android conectado) e garantir que o
  WearBridge não está instalado no relógio nem pareado no iPhone. O ANCS só entrega eventos
  para quem se inscreveu por último (gotcha 15), e o S21 duplicaria notificações.
- **Bateria**: reconexão via `connectGatt(autoConnect = true)` (o rádio espera o iPhone
  voltar, sem scan ativo); scan só no pareamento. Alvo: no máximo ~10% de bateria extra por
  dia em uso normal; o roteiro manual inclui medir um dia com e sem o app.
- **Ligações**: `ConnectionService` self-managed (Telecom), como o WearBridge, para a tela
  aparecer sobre a tela de carregamento e de bloqueio.

## Deixado para quem implementar

- Versões exatas de dependências (usar as estáveis atuais).
- Ícones por app (pode começar com fallback por categoria do ANCS).
- Visual das telas, desde que legível em tela redonda.
- Persistência (DataStore vs Room) para o iPhone pareado e configurações.

## Repositório

`~/code/pulso` — nome provisório, escolhido pelo Claude; o Pedro pode trocar.
Repo público no GitHub quando o Pedro decidir publicar.
