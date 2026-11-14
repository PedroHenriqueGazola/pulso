# Gotchas (BLE, ANCS e Wear OS)

Lições herdadas do [WearBridge](https://github.com/k97/WearBridge) e do que aparecer neste projeto.

1. **Callback com o mesmo nome do override** — lambda `onServicesDiscovered` igual ao método causou recursão infinita.
   Regra: callbacks recebidos por parâmetro têm sufixo `Callback`.
2. **Canais de notificação ficam em cache** — apagar e recriar com o mesmo ID não muda vibração/importância.
   Regra: IDs versionados (`alerts_v1` → `alerts_v2`) a cada mudança de configuração.
3. **Nome do iPhone pareado varia** — pode vir truncado ou com outro nome.
   Regra: nunca identificar o iPhone pelo nome; usar o endereço salvo.
4. **App iOS precisa de conta paga para BLE peripheral** — não se aplica à Fase 1 (sem app iOS).
5. **Full-screen intent sozinho não abre activity no Wear OS**.
   Regra: chamar `startActivity()` direto do serviço além do full-screen intent.
6. **`find ~` no macOS dispara pedidos de privacidade** — evitar buscas na home.
7. **`setNotificationSilent()` mata o pop-up** — zera o som e o heads-up não aparece.
   Regra: nunca usar; silêncio é decidido pelo canal; vibração extra pelo `Vibrator`.
8. **`USE_FULL_SCREEN_INTENT` no Android 14+ é app-op**.
   Regra: `adb shell appops set app.pulso.watch USE_FULL_SCREEN_INTENT allow`.
9. **ConnectionService self-managed não usa a UI de chamada do sistema**.
   Regra: o app desenha a própria tela de chamada.
10. **Escritas GATT vibram o relógio** — buscar atributos de notificações antigas na reconexão causa vibração constante.
    Regra: ignorar eventos `PreExisting` sem buscar atributos.
11. **Canal sem som não mostra pop-up**.
    Regra: todo canal de alerta tem som definido.
12. **`discoverServices()` repetido reinscreve no ANCS** e reseta a sessão.
    Regra: descobrir serviços uma vez por conexão; não reinscrever se já inscrito.
13. **Agrupamento automático silencia notificações** do mesmo app.
    Regra: group key única por notificação (`ancs_<uid>`).
14. **Pop-up da Samsung só mostra o texto com `MessagingStyle`**.
    Regra: mensagens sempre com `MessagingStyle`.
15. **ANCS entrega eventos só para o último dispositivo que se inscreveu**.
    Regra: só um relógio por vez; desinstalar outros bridges (WearBridge) antes de testar.
16. **Ligação atendida gera MODIFIED, não REMOVED** — REMOVED só quando a ligação termina.
    Regra: MODIFIED de uma ligação tocando → estado "em ligação no iPhone".
