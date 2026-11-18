# Pulso — App do relógio (Fase 1) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** App Wear OS para o Galaxy Watch 6 que recebe notificações, ligações e controle de mídia do iPhone via ANCS/AMS, sem app no iPhone.

**Architecture:** Um módulo Gradle `app`. Protocolo (`ancs/`, `ams/`, `calls/CallClassifier`) é Kotlin puro, testado com JUnit. Tudo que toca BLE passa pela interface `GattClient`, para as sessões ANCS/AMS serem testáveis com um fake. Um foreground service só orquestra: conecta, cria as sessões e entrega eventos aos apresentadores (notificações, ligações, mídia).

**Tech Stack:** Kotlin, Jetpack Compose for Wear OS (Material 3), Coroutines/Flow, DataStore Preferences, JUnit 4 + kotlinx-coroutines-test, Android Telecom (`ConnectionService` self-managed).

**Spec:** `docs/superpowers/specs/2026-11-14-pulso-watch-fase1-design.md`

**Referência:** WearBridge (MIT): `git clone https://github.com/k97/WearBridge`. Arquivos de protocolo em `wearos-app/app/src/main/java/com/wearos/ancsbridge/ancs/`, regras em `.claude/rules/gotchas.md`.

## Global Constraints

- Raiz do repo: `~/code/pulso`; projeto Android em `~/code/pulso/watch/`.
- `applicationId`/namespace: `app.pulso.watch`. Pacotes abaixo são relativos a `watch/app/src/main/java/app/pulso/watch/` (testes em `watch/app/src/test/java/app/pulso/watch/`).
- minSdk 33, targetSdk 35, compileSdk 35, Java/JVM 17. Dependências: versões estáveis atuais (Compose for Wear Material3 estável, sem alpha se houver estável).
- Estilo (do `~/.claude/CLAUDE.md` do Pedro): 2 espaços, nomes sem abreviação, booleanos `is/has/should`, funções começam com verbo, guard clauses, evitar `else`/`else if` (usar `when` isolado ou mapa), `val` e dados imutáveis, sem comentários de "o quê", funções ≤ ~30 linhas, nenhum arquivo "Deus" (> ~250 linhas é sinal de dividir).
- Testes nomeados `should ... when ...`.
- Pacotes de protocolo (`ancs/`, `ams/`, `calls/CallClassifier.kt`, `bluetooth/GattOperationQueue.kt`) não importam `android.*`.
- Canais de notificação versionados (IDs terminam em `_v1`); nunca `setNotificationSilent()`; todo canal com som definido; group key única `ancs_<uid>`; mensagens com `MessagingStyle`.
- Dispensar: **só** a ação "Dispensar" envia ação negativa ao iPhone. Swipe no relógio não envia nada.
- Flag ANCS `Silent` (Foco/Não Perturbe): notificação aparece, sem vibração e sem heads-up.
- Reconexão por `connectGatt(autoConnect = true)`; scan só no pareamento.
- README com seção "Créditos" citando WearBridge (MIT) pelos parsers ANCS portados.
- Commits: só quando o Pedro pedir (regra do ambiente). Os passos "Commit" abaixo viram "pedir ao Pedro para commitar / commitar se já autorizado". Mensagens terminam com `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`.

## Review Focus

1. Reconectar com dezenas de notificações já existentes no iPhone → nenhuma vibração em rajada (eventos `PreExisting` não buscam atributos). Teste na Task 5.
2. Notificação removida no iPhone enquanto os atributos ainda estão sendo buscados → não pode aparecer notificação fantasma. Teste na Task 5.
3. Duas notificações chegando juntas → buscas de atributos serializadas, cada uma com seu texto correto. Teste na Task 5.
4. Texto longo com emoji/acentos quebrado entre fragmentos do Data Source (UTF-8 multibyte cortado no meio) → texto correto. Teste na Task 2.
5. Ligação de WhatsApp em PT-BR com acento ("Chamada de vídeo do WhatsApp") ou sem ("chamada de video") → classificada como ligação. Teste na Task 3.

---

### Task 1: Esqueleto do projeto instalável no relógio

**Files:**
- Create: `watch/settings.gradle.kts`, `watch/build.gradle.kts`, `watch/gradle.properties`, `watch/gradle/wrapper/*` (via `gradle wrapper`), `watch/app/build.gradle.kts`
- Create: `watch/app/src/main/AndroidManifest.xml`, `ui/MainActivity.kt`, `ui/theme/PulsoTheme.kt`
- Create: `README.md`, `LICENSE` (MIT, Pedro Gazola 2026), `.gitignore` (Android padrão), `docs/gotchas.md`, `docs/manual-test.md`
- Test: `watch/app/src/test/java/app/pulso/watch/SanityTest.kt`

**Interfaces:**
- Produces: projeto que compila; `MainActivity` com Compose mostrando "Pulso".

- [ ] **Step 1:** `git init` em `~/code/pulso` (a pasta já contém `docs/`).
- [ ] **Step 2:** Criar projeto Gradle conforme Global Constraints. Manifest: `<uses-feature android:name="android.hardware.type.watch" />`, `com.google.android.wearable.standalone = true` (meta-data), permissões: `BLUETOOTH_SCAN` (`neverForLocation`), `BLUETOOTH_CONNECT`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_CONNECTED_DEVICE`, `POST_NOTIFICATIONS`, `VIBRATE`, `RECEIVE_BOOT_COMPLETED`, `USE_FULL_SCREEN_INTENT`, `MANAGE_OWN_CALLS`, `WAKE_LOCK`.
- [ ] **Step 3:** `SanityTest.should run unit tests when project is built` com `assertEquals(2, 1 + 1)`.
- [ ] **Step 4:** Run: `cd watch && ./gradlew testDebugUnitTest assembleDebug` — Expected: `BUILD SUCCESSFUL`.
- [ ] **Step 5:** `docs/gotchas.md`: portar os 16 itens do WearBridge em PT-BR, uma linha de problema + uma de regra cada. `docs/manual-test.md`: roteiro com seções Pré-requisitos (Bluetooth do S21 FE desligado; WearBridge não instalado/pareado; `adb shell appops set app.pulso.watch USE_FULL_SCREEN_INTENT allow`), Pareamento, Notificações (WhatsApp, iMessage, e-mail, Foco ligado), Dispensar (botão vs swipe), Ligações (telefone, WhatsApp; atender, recusar, atender no iPhone), Música (Spotify, Apple Music), Reconexão (Bluetooth do iPhone off/on, reiniciar relógio), Bateria (1 dia com/sem app, alvo ≤ ~10% extra).
- [ ] **Step 6:** README: o que é, limitações (sem resposta rápida, sem saúde na Fase 1), como instalar (ativar depuração ADB por Wi‑Fi no relógio, `adb pair`, `adb connect`, `./gradlew installDebug`), seção Créditos.
- [ ] **Step 7 (manual, com o Pedro):** instalar no relógio e ver a tela "Pulso". Run: `adb devices` lista o relógio; `cd watch && ./gradlew installDebug` → `Installed on 1 device`.
- [ ] **Step 8:** Commit `chore: scaffold Pulso Wear OS app`.

---

### Task 2: Protocolo ANCS (port do WearBridge)

**Files:**
- Create: `ancs/AncsUuids.kt`, `ancs/AncsCategory.kt`, `ancs/NotificationSourceEvent.kt`, `ancs/NotificationSourceParser.kt`, `ancs/ControlPointCommands.kt`, `ancs/NotificationAttributesAssembler.kt`, `ancs/IphoneNotification.kt`
- Test: `ancs/NotificationSourceParserTest.kt`, `ancs/ControlPointCommandsTest.kt`, `ancs/NotificationAttributesAssemblerTest.kt`

**Interfaces:**
- Produces:
  - `object AncsUuids { SERVICE; NOTIFICATION_SOURCE; CONTROL_POINT; DATA_SOURCE: UUID }` (valores do `AncsConstants.kt` do WearBridge)
  - `enum class AncsCategory(val id: Int) { OTHER(0), INCOMING_CALL(1), MISSED_CALL(2), VOICEMAIL(3), SOCIAL(4), SCHEDULE(5), EMAIL(6), NEWS(7), HEALTH_AND_FITNESS(8), BUSINESS_AND_FINANCE(9), LOCATION(10), ENTERTAINMENT(11); companion fun fromId(id: Int): AncsCategory }` (desconhecido → `OTHER`)
  - `enum class NotificationEventKind { ADDED, MODIFIED, REMOVED }`
  - `data class NotificationSourceEvent(val kind: NotificationEventKind, val notificationUid: Long, val category: AncsCategory, val isSilent: Boolean, val isImportant: Boolean, val isPreExisting: Boolean, val hasPositiveAction: Boolean, val hasNegativeAction: Boolean)`
  - `object NotificationSourceParser { fun parse(bytes: ByteArray): NotificationSourceEvent? }`
  - `enum class NotificationAction(val id: Int) { POSITIVE(0), NEGATIVE(1) }`
  - `object ControlPointCommands { fun buildGetNotificationAttributes(notificationUid: Long, shouldIncludeActionLabels: Boolean): ByteArray; fun buildPerformNotificationAction(notificationUid: Long, action: NotificationAction): ByteArray }` — atributos pedidos, nesta ordem: AppIdentifier(0), Title(1, max 255), Subtitle(2, max 255), Message(3, max 255), Date(5); se `shouldIncludeActionLabels`, + PositiveActionLabel(6, 255), NegativeActionLabel(7, 255).
  - `data class IphoneNotification(val uid: Long, val appIdentifier: String, val title: String, val subtitle: String, val message: String, val category: AncsCategory, val isSilent: Boolean, val hasPositiveAction: Boolean, val hasNegativeAction: Boolean, val positiveActionLabel: String, val negativeActionLabel: String)`
  - `class NotificationAttributesAssembler { fun expect(event: NotificationSourceEvent, requestedAttributeIds: List<Int>); fun append(fragment: ByteArray): IphoneNotification?; fun reset() }`

- [ ] **Step 1:** Portar os testes do WearBridge (`NotificationSourceParserTest`, `ControlPointWriterTest`, `DataSourceAssemblerTest`) para as novas assinaturas, renomeando cada um para `should ... when ...` (ex.: `should parse added event when payload has 8 bytes`, `should return null when payload is shorter than 8 bytes`, `should encode uid little endian when building perform action`, `should assemble notification when response spans multiple fragments`, `should keep partial state when fragment ends inside length field`).
- [ ] **Step 2:** Adicionar `NotificationAttributesAssemblerTest.should decode multibyte text when utf8 character is split across fragments` — mensagem `"Olá 👋 tudo bem?"`, resposta completa montada em bytes e cortada no meio dos 4 bytes do emoji; `assertEquals("Olá 👋 tudo bem?", result!!.message)`.
- [ ] **Step 3:** Run: `./gradlew testDebugUnitTest --tests 'app.pulso.watch.ancs.*'` — Expected: FAIL (classes não existem).
- [ ] **Step 4:** Implementar portando a lógica do WearBridge, no estilo do Pedro. O assembler decodifica cada atributo só depois de ter todos os bytes dele (o tamanho vem no header), então o caso do Step 2 passa sem lógica extra — se não passar, acumular bytes crus e decodificar no fim.
- [ ] **Step 5:** Run o mesmo comando — Expected: PASS.
- [ ] **Step 6:** Commit `feat(ancs): port ANCS protocol parsers from WearBridge`.

---

### Task 3: Classificador de ligações

**Files:**
- Create: `calls/CallClassifier.kt`, `calls/CallPatterns.kt`
- Test: `calls/CallClassifierTest.kt`

**Interfaces:**
- Consumes: `IphoneNotification`, `AncsCategory` (Task 2)
- Produces: `object CallClassifier { fun isIncomingCall(notification: IphoneNotification): Boolean }`

`CallPatterns.kt` contém `val CALL_APP_IDENTIFIERS = setOf("com.apple.mobilephone", "com.apple.facetime", "net.whatsapp.WhatsApp", "net.whatsapp.WhatsAppSMB")` e `val CALL_TEXT_PATTERNS` com, no mínimo: `"chamada de voz"`, `"chamada de video"`, `"chamada recebida"`, `"esta ligando"`, `"ligando"`, `"incoming call"`, `"incoming voice"`, `"incoming video"`, `"audio call"`, `"video call"`, `"voice call"`, `"is calling"`, `"☎"`. Comparação: minúsculas e sem acentos (`java.text.Normalizer` NFD + remover `\p{Mn}`) sobre `"$title $subtitle $message"`.

Regra: `category == INCOMING_CALL` → true. Senão, app em `CALL_APP_IDENTIFIERS` **e** texto casa algum padrão → true. Senão false.

- [ ] **Step 1:** Testes:
  - `should classify as call when category is incoming call` (app qualquer, texto vazio) → true
  - `should classify as call when whatsapp message says chamada de vídeo with accent` (`message = "Chamada de vídeo do WhatsApp"`) → true
  - `should classify as call when whatsapp message says chamada de video without accent` → true
  - `should classify as call when facetime title says is calling` → true
  - `should not classify as call when whatsapp text message mentions nothing about calls` (`"Bora almoçar?"`) → false
  - `should not classify as call when non call app says chamada de voz` (`appIdentifier = "com.google.Gmail"`) → false
- [ ] **Step 2:** Run `--tests 'app.pulso.watch.calls.CallClassifierTest'` — FAIL.
- [ ] **Step 3:** Implementar.
- [ ] **Step 4:** Run — PASS.
- [ ] **Step 5:** Commit `feat(calls): classify incoming calls including whatsapp in pt-br`.

---

### Task 4: Fila de operações GATT e interface `GattClient`

**Files:**
- Create: `bluetooth/GattClient.kt`, `bluetooth/GattOperationQueue.kt`
- Test: `bluetooth/GattOperationQueueTest.kt`, `bluetooth/FakeGattClient.kt` (em `src/test`)

**Interfaces:**
- Produces:
  - `data class CharacteristicChange(val characteristic: UUID, val value: ByteArray)`
  - `interface GattClient { val characteristicChanges: SharedFlow<CharacteristicChange>; fun hasService(service: UUID): Boolean; suspend fun enableNotifications(service: UUID, characteristic: UUID); suspend fun write(service: UUID, characteristic: UUID, value: ByteArray) }` — `write`/`enableNotifications` lançam `GattOperationException(message)` em falha.
  - `class GattOperationQueue { suspend fun <T> run(operation: suspend () -> T): T }` — executa uma operação por vez, em ordem de chegada (Android só aceita uma operação GATT pendente). Implementação: `Mutex.withLock`.
  - `FakeGattClient` (teste): registra `writes: List<Pair<UUID, ByteArray>>` e `enabledNotifications: List<UUID>`; `suspend fun emit(characteristic: UUID, value: ByteArray)`; `services: Set<UUID>` configurável.

- [ ] **Step 1:** Teste `should run operations one at a time when called concurrently` — duas corrotinas chamam `run { log += "start-a"; delay(10); log += "end-a" }` e equivalente "b"; com `runTest`, `assertEquals(listOf("start-a","end-a","start-b","end-b"), log)`.
- [ ] **Step 2:** Teste `should release queue when operation throws` — primeira operação lança, segunda executa e retorna valor.
- [ ] **Step 3:** Run — FAIL. Implementar. Run — PASS.
- [ ] **Step 4:** Commit `feat(bluetooth): add serialized gatt operation queue`.

---

### Task 5: Sessão ANCS

**Files:**
- Create: `ancs/AncsSession.kt`, `ancs/AncsUpdate.kt`
- Test: `ancs/AncsSessionTest.kt`

**Interfaces:**
- Consumes: `GattClient`, `FakeGattClient` (Task 4); parsers e `ControlPointCommands` (Task 2)
- Produces:
  - `sealed interface AncsUpdate { data class NotificationPosted(val notification: IphoneNotification); data class NotificationChanged(val notification: IphoneNotification); data class NotificationRemoved(val uid: Long) }`
  - `class AncsSession(private val gattClient: GattClient, private val scope: CoroutineScope) { val updates: SharedFlow<AncsUpdate>; suspend fun start(); suspend fun performAction(notificationUid: Long, action: NotificationAction) }`

Comportamento:
- `start()`: habilita notificações em Data Source **antes** de Notification Source (sem isso, respostas se perdem); coleta `characteristicChanges`.
- Evento NS `ADDED`/`MODIFIED` e não `isPreExisting` → enfileira busca de atributos (uma de cada vez: escreve no Control Point, espera o assembler completar, timeout 5 s → descarta e segue).
- `REMOVED` → emite `NotificationRemoved(uid)` e, se esse uid estiver na fila ou em busca, cancela a busca.
- `isPreExisting` → ignora (sem busca, sem emissão).
- `MODIFIED` completo → `NotificationChanged`; `ADDED` completo → `NotificationPosted`.

- [ ] **Step 1:** Testes com `FakeGattClient` + `runTest` (helper `buildNotificationSourceBytes(kind, uid, category, flags)` e `buildAttributesResponse(uid, appIdentifier, title, message)` no próprio teste):
  - `should enable data source before notification source when session starts` → `enabledNotifications == listOf(DATA_SOURCE, NOTIFICATION_SOURCE)`
  - `should post notification when added event attributes arrive` → um `NotificationPosted` com title/message corretos
  - `should not request attributes when event is pre existing` (Review Focus 1) → 30 eventos pre-existing ⇒ `writes` vazio e nenhum update
  - `should not post notification when it is removed while attributes are being fetched` (Review Focus 2) → ADDED uid 7, REMOVED uid 7 antes da resposta, depois resposta chega ⇒ só `NotificationRemoved(7)`
  - `should fetch attributes one at a time when two notifications arrive together` (Review Focus 3) → ADDED uid 1 e 2; só 1 write até responder o uid 1; ao final, dois `NotificationPosted` com textos certos, na ordem
  - `should emit changed when modified event attributes arrive`
  - `should write negative action command when performing dismiss` → último write == `ControlPointCommands.buildPerformNotificationAction(9, NEGATIVE)` no `CONTROL_POINT`
  - `should skip notification and continue when attributes time out` (avançar tempo virtual 5 s)
- [ ] **Step 2:** Run — FAIL. Implementar (fila interna com `Channel<NotificationSourceEvent>` consumida por uma corrotina). Run — PASS.
- [ ] **Step 3:** Commit `feat(ancs): add ANCS session with serialized attribute fetching`.

---

### Task 6: Protocolo e sessão AMS

**Files:**
- Create: `ams/AmsUuids.kt`, `ams/MediaCommand.kt`, `ams/EntityUpdateParser.kt`, `ams/MediaState.kt`, `ams/AmsSession.kt`
- Test: `ams/EntityUpdateParserTest.kt`, `ams/MediaStateTest.kt`, `ams/AmsSessionTest.kt`

**Interfaces:**
- Consumes: `GattClient`, `FakeGattClient` (Task 4)
- Produces:
  - `object AmsUuids { SERVICE = 89D3502B-0F36-433A-8EF4-C502AD55F8DC; REMOTE_COMMAND = 9B3C81D8-57B1-4A8A-B8DF-0E56F7CA51C2; ENTITY_UPDATE = 2F7CABCE-808D-411F-9A0C-BB92BA96C102; ENTITY_ATTRIBUTE = C6B2F38C-23AB-46D8-A6AB-A3A870BBD5D7 }`
  - `enum class MediaCommand(val id: Int) { PLAY(0), PAUSE(1), TOGGLE_PLAY_PAUSE(2), NEXT_TRACK(3), PREVIOUS_TRACK(4), VOLUME_UP(5), VOLUME_DOWN(6) }`
  - `sealed interface EntityUpdate { data class PlayerName(val name: String); data class PlaybackInfo(val isPlaying: Boolean); data class Volume(val level: Float); data class TrackArtist(val artist: String); data class TrackTitle(val title: String); data class TrackAlbum(val album: String); data class TrackDuration(val seconds: Float) }`
  - `object EntityUpdateParser { fun parse(bytes: ByteArray): EntityUpdate? }` — formato `[entityId, attributeId, flags, utf8 value...]`; entidades Player=0 (Name 0, PlaybackInfo 1 = `"state,rate,elapsed"`, state 1 = playing, Volume 2), Track=2 (Artist 0, Album 1, Title 2, Duration 3). Desconhecido ou < 3 bytes → null.
  - `data class MediaState(val playerName: String = "", val title: String = "", val artist: String = "", val isPlaying: Boolean = false, val volume: Float = 0f) { fun apply(update: EntityUpdate): MediaState; val hasActivePlayer: Boolean get() = playerName.isNotEmpty() }`
  - `class AmsSession(gattClient: GattClient, scope: CoroutineScope) { val state: StateFlow<MediaState>; suspend fun start(); suspend fun send(command: MediaCommand) }` — `start()`: se `!hasService(SERVICE)` retorna sem erro; habilita ENTITY_UPDATE; escreve `[0, 0, 1, 2]` (Player: Name, PlaybackInfo, Volume) e `[2, 0, 2]` (Track: Artist, Title).

- [ ] **Step 1:** Testes: `should parse playing state when playback info starts with 1` (`"1,1.0,12.5"`), `should parse paused state when playback info starts with 0`, `should parse track title when entity is track and attribute is title`, `should return null when payload is shorter than 3 bytes`, `should keep other fields when applying title update`, `should report no active player when player name is empty` (Review: iPhone sem nada tocando), `should register player and track attributes when session starts` (writes exatos acima), `should write toggle command when sending play pause` (`[2]` em REMOTE_COMMAND), `should not fail when iphone has no ams service`.
- [ ] **Step 2:** Run — FAIL. Implementar. Run — PASS.
- [ ] **Step 3:** Commit `feat(ams): add Apple Media Service protocol and session`.

---

### Task 7: Conexão Bluetooth real e tela de pareamento

**Files:**
- Create: `bluetooth/ConnectionState.kt`, `bluetooth/PairedIphoneStore.kt`, `bluetooth/IphoneScanner.kt`, `bluetooth/AndroidGattClient.kt`, `bluetooth/IphoneConnection.kt`
- Create: `ui/StatusScreen.kt`, `ui/StatusViewModel.kt`; Modify: `ui/MainActivity.kt`

**Interfaces:**
- Consumes: `GattClient`, `GattOperationQueue` (Task 4)
- Produces:
  - `sealed interface ConnectionState { data object NotPaired; data object Scanning; data class Connecting(val iphoneName: String); data class Connected(val iphoneName: String, val gattClient: GattClient); data class WaitingForIphone(val iphoneName: String) }`
  - `class PairedIphoneStore(context: Context) { val pairedAddress: Flow<String?>; suspend fun save(address: String); suspend fun clear() }` (DataStore Preferences)
  - `class IphoneScanner(context: Context) { fun scan(): Flow<ScannedIphone> }`, `data class ScannedIphone(val address: String, val name: String)` — scan BLE filtrando por manufacturer data Apple (company id `0x004C`) ou nome contendo "iPhone"; timeout 20 s.
  - `class AndroidGattClient(...) : GattClient` — envolve `BluetoothGatt`, usa `GattOperationQueue`; CCCD `00002902-0000-1000-8000-00805f9b34fb`.
  - `class IphoneConnection(context: Context, store: PairedIphoneStore, scope: CoroutineScope) { val state: StateFlow<ConnectionState>; fun connect(address: String); fun forget() }` — `connectGatt(autoConnect = true, TRANSPORT_LE)`; bond com `createBond()` quando ANCS pede (aguardar `BOND_BONDED` via `BroadcastReceiver`); após bond, `discoverServices()` **uma vez** por conexão (gotcha 12); desconexão → `WaitingForIphone` e mantém `autoConnect`. Nome do iPhone: `device.name ?: "iPhone"`.
- `StatusScreen`: mostra estado; botão "Parear iPhone" (lista do scan), botão "Esquecer". Pede `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, `POST_NOTIFICATIONS` em runtime.

- [ ] **Step 1:** Implementar. Sem testes unitários (código de plataforma); a lógica testável já está nas Tasks 2–6.
- [ ] **Step 2:** Run `./gradlew testDebugUnitTest assembleDebug` — PASS / BUILD SUCCESSFUL.
- [ ] **Step 3 (manual):** `installDebug`; parear; iPhone mostra pedido de pareamento e "Compartilhar notificações do sistema?" → aceitar; tela mostra "Conectado a <nome>". Desligar Bluetooth do iPhone → "Aguardando iPhone"; religar → "Conectado" sem novo pareamento. Logs: `adb logcat -s Pulso`.
- [ ] **Step 4:** Commit `feat(bluetooth): pair and keep connection with iPhone`.

---

### Task 8: Serviço em primeiro plano + notificações no relógio

**Files:**
- Create: `service/PulsoService.kt`, `service/BootReceiver.kt`, `notifications/NotificationChannels.kt`, `notifications/WatchNotificationPresenter.kt`, `notifications/DismissActionReceiver.kt`, `notifications/AppLabels.kt`
- Modify: `AndroidManifest.xml` (service `foregroundServiceType="connectedDevice"`, receivers), `ui/MainActivity.kt` (inicia o serviço)

**Interfaces:**
- Consumes: `IphoneConnection`, `AncsSession`, `AncsUpdate`, `CallClassifier`, `NotificationAction`
- Produces:
  - `object NotificationChannels { const val ALERTS = "alerts_v1"; const val SILENT = "silent_v1"; const val SERVICE = "service_v1"; const val CALLS = "calls_v1"; fun register(context: Context) }` — ALERTS `IMPORTANCE_HIGH` com som padrão e vibração; SILENT `IMPORTANCE_LOW`; CALLS `IMPORTANCE_HIGH`.
  - `class WatchNotificationPresenter(context: Context) { fun show(notification: IphoneNotification); fun remove(uid: Long) }` — id Android = `uid.toInt()`; canal SILENT se `isSilent`, senão ALERTS; `MessagingStyle` com remetente = `title`, texto = `message`; `setGroup("ancs_$uid")`; título do app via `AppLabels.labelFor(appIdentifier)` (mapa bundle id → nome, fallback = último segmento do bundle id); ação "Dispensar" só se `hasNegativeAction`.
  - `DismissActionReceiver`: recebe uid, envia broadcast/intent ao `PulsoService`, que chama `ancsSession.performAction(uid, NEGATIVE)` e `presenter.remove(uid)`. Sem `setDeleteIntent` (swipe não afeta o iPhone).
  - `PulsoService`: observa `IphoneConnection.state`; em `Connected` cria `AncsSession` e `AmsSession`, chama `start()`; roteia `NotificationPosted`/`Changed` → se `CallClassifier.isIncomingCall` vai para chamadas (Task 9), senão `presenter.show`; `NotificationRemoved` → `presenter.remove` e chamadas. Expõe `AmsSession.state` para a UI (Task 10) via um singleton `PulsoSessions` com `StateFlow<AmsSession?>`.
  - `BootReceiver`: `BOOT_COMPLETED` → inicia `PulsoService` se houver iPhone pareado.

- [ ] **Step 1:** Implementar. Run `./gradlew testDebugUnitTest assembleDebug`.
- [ ] **Step 2 (manual, `docs/manual-test.md` seções Notificações e Dispensar):** WhatsApp, iMessage e e-mail chegam com pop-up e vibração; com Foco ligado no iPhone chega sem vibrar; botão "Dispensar" some do iPhone; swipe no relógio não mexe no iPhone; apagar no iPhone some do relógio; desligar/religar Bluetooth do iPhone **não** gera rajada; reiniciar o relógio → serviço volta sozinho.
- [ ] **Step 3:** Commit `feat(notifications): mirror iPhone notifications on the watch`.

---

### Task 9: Ligações

**Files:**
- Create: `calls/CallState.kt`, `calls/CallStateMachine.kt`, `calls/PulsoConnectionService.kt`, `calls/IphoneCallConnection.kt`, `calls/IncomingCallActivity.kt`, `calls/CallScreen.kt`
- Test: `calls/CallStateMachineTest.kt`
- Modify: `service/PulsoService.kt` (roteia chamadas), `AndroidManifest.xml`

**Interfaces:**
- Consumes: `AncsUpdate`, `CallClassifier`, `AncsSession.performAction`
- Produces:
  - `sealed interface CallState { data object Idle; data class Ringing(val uid: Long, val callerName: String, val appLabel: String, val canAnswer: Boolean, val canDecline: Boolean); data class ActiveOnIphone(val uid: Long, val callerName: String) }`
  - `class CallStateMachine { val state: StateFlow<CallState>; fun onCallPosted(notification: IphoneNotification); fun onCallChanged(notification: IphoneNotification); fun onRemoved(uid: Long); fun onDeclinedOnWatch() }` — `canAnswer = hasPositiveAction`, `canDecline = hasNegativeAction`.
  - `PulsoConnectionService` (self-managed `ConnectionService`), registrado via `PhoneAccount.CAPABILITY_SELF_MANAGED`; `IncomingCallActivity` mostra `CallScreen` e é aberta com `startActivity` direto do serviço + full-screen intent no canal CALLS (gotcha 5 e 8). Botões: Atender (se `canAnswer`) → `performAction(uid, POSITIVE)`; Recusar (se `canDecline`) → `NEGATIVE`; senão texto "Atenda no iPhone".
  - Durante `Ringing`, vibração em padrão repetido via `Vibrator` até sair de `Ringing`.

- [ ] **Step 1:** Testes: `should ring when call is posted`, `should move to active on iphone when ringing call is modified` (gotcha 16), `should return to idle when call is removed`, `should ignore removal when uid is not the current call`, `should hide answer when call has no positive action` (WhatsApp sem ação positiva).
- [ ] **Step 2:** Run — FAIL. Implementar máquina. Run — PASS.
- [ ] **Step 3:** Implementar Telecom/Activity/tela. Run `assembleDebug`.
- [ ] **Step 4 (manual, seção Ligações):** ligação de telefone (atender no relógio, recusar no relógio, atender no iPhone → tela "Em ligação no iPhone", desligar → some); ligação de WhatsApp em PT-BR → tela de chamada; com o relógio carregando, a tela aparece. Capturar via `adb logcat -s Pulso` o texto real da notificação de chamada do WhatsApp/FaceTime e, se algum não for reconhecido, adicionar o padrão em `CallPatterns.kt` + um teste em `CallClassifierTest`.
- [ ] **Step 5:** Commit `feat(calls): show incoming calls with answer and decline`.

---

### Task 10: Tela de música

**Files:**
- Create: `media/MediaScreen.kt`, `media/MediaViewModel.kt`
- Modify: `ui/MainActivity.kt` (navegação: Status ↔ Música via swipe/pager)

**Interfaces:**
- Consumes: `PulsoSessions` (`StateFlow<AmsSession?>`), `MediaState`, `MediaCommand`

- `MediaScreen`: título, artista, nome do player; botões anterior / play-pause / próxima; volume +/−. Sem player ativo (`!hasActivePlayer`) ou sem sessão → texto "Nada tocando no iPhone".

- [ ] **Step 1:** Implementar. Run `./gradlew testDebugUnitTest assembleDebug`.
- [ ] **Step 2 (manual, seção Música):** Spotify e Apple Music: faixa aparece, muda ao trocar de música, botões funcionam, volume altera no iPhone; pausar no iPhone reflete no relógio.
- [ ] **Step 3:** Commit `feat(media): control iPhone media from the watch`.

---

### Task 11: Rodada completa de aceite

- [ ] **Step 1:** Executar `docs/manual-test.md` inteiro com o Pedro, incluindo um dia de bateria. Anotar resultados no próprio arquivo (data + OK/Falhou por item).
- [ ] **Step 2:** Run `./gradlew testDebugUnitTest` — PASS. Corrigir o que falhar no roteiro (cada correção com teste quando for lógica de protocolo).
- [ ] **Step 3:** Atualizar README (status da Fase 1, screenshots opcionais) e commit `docs: record phase 1 acceptance results`.
