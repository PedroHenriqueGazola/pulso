# Pulso

**Notificações, ligações e música do iPhone num Galaxy Watch (Wear OS), sem instalar nada no iPhone.**

Troquei um Galaxy S21 FE por um iPhone 17 e o Galaxy Watch 6 virou um relógio que só mostra a hora.
A Samsung não suporta iPhone no Wear OS, e as opções que existem são pagas por assinatura
([Merge Watch](https://www.merge.watch)) ou incompletas ([WearBridge](https://github.com/k97/WearBridge)).
O Pulso é a minha versão, para uso próprio e instalada por `adb`.

## Como funciona

O iOS já expõe dois serviços Bluetooth para acessórios pareados. São os mesmos que smartbands e
relógios de outras marcas usam:

```
   iPhone                                     Galaxy Watch (Pulso)
 ┌──────────────────────┐   Bluetooth LE    ┌──────────────────────────────┐
 │ ANCS  notificações   │ ◀───────────────▶ │ ancs/   parsers de protocolo │
 │       e ligações     │                   │ calls/  tela de ligação      │
 │ AMS   música         │ ◀───────────────▶ │ ams/    controle de mídia    │
 └──────────────────────┘                   │ notifications/  Wear OS      │
                                            └──────────────────────────────┘
```

- **ANCS** (Apple Notification Center Service): toda notificação do iPhone, com app de origem,
  título, texto e ações (ex.: recusar uma ligação, dispensar uma notificação).
- **AMS** (Apple Media Service): o que está tocando, play/pause, próxima e volume.

O relógio é o cliente GATT: faz o bond com o iPhone, se inscreve nos dois serviços e transforma
cada evento numa notificação do Wear OS. O iOS só pede uma confirmação: **"Compartilhar
Notificações do Sistema?"**.

## Status

Testado num Galaxy Watch 6 (Wear OS 6 / Android 16) com iPhone 17 (iOS 26):

- ✅ Pareamento com o iPhone e reconexão sozinha.
- ✅ Notificações com pop-up, vibração, ícone e cor do app (WhatsApp, Instagram, Gmail, Nubank…).
- ✅ "Dispensar" no relógio apaga a notificação no iPhone.

Implementado, mas ainda não validado no aparelho:

- Ligações (atender, recusar, "em ligação no iPhone"), inclusive de WhatsApp e FaceTime.
- Controle de música.
- Seguir o Foco do iPhone: a notificação aparece, mas sem vibrar.
- Voltar sozinho quando o iPhone sai e volta do alcance, e ligar junto com o relógio.

## Limitações (do iOS, não do app)

- **Fotos, figurinhas e áudios não aparecem.** O ANCS só entrega texto, então o relógio vê
  "📷 Foto". Nenhum app no iPhone consegue ler as conversas de outro app, e a mídia só chega ao
  Apple Watch, por um canal que só a Apple pode usar.
- **Não dá para responder mensagens.** O ANCS não tem essa ação.
- **Saúde, contatos e câmera** exigem um app no iPhone (veja o roadmap).
- O relógio precisa ter sido configurado uma vez com um celular Android.

## O que aprendi no caminho

Os bugs mais interessantes só apareceram com o relógio no pulso. Nenhum deles aparecia nos testes
unitários:

**"Não vibra" era o relógio fora do pulso.** As notificações chegavam, mas o Wear OS segura
vibração e pop-up quando o sensor diz que o relógio não está sendo usado. O log entrega:
`Not alerting: Device is off-body`.

**Vibrava duas vezes por mensagem.** O WhatsApp no iOS atualiza a notificação logo depois de
mandá-la. O relógio recebia um `Modified` do ANCS e alertava de novo. Resolvido com
`setOnlyAlertOnce`.

**A One UI Watch ignora o ícone da notificação.** Ela mostra o ícone do *app* que publicou, então
todas as notificações apareciam com o ícone do Pulso. A saída foi desenhar um avatar (círculo na
cor da marca com o logo) e usar como foto do "remetente" no `MessagingStyle`.

**Um ícone derrubou o serviço por uma hora.** Os SVGs do Simple Icons usam a forma compacta das
flags de arco (`a1 1 0 01.5.5`), que o `PathParser` do Android rejeita. O serviço caiu na primeira
mensagem do WhatsApp, e o Android agendou o restart para dali a 3.600.000 ms. Agora um teste
valida todo `pathData`, e uma notificação com problema é registrada no log sem derrubar as
próximas.

**`autoConnect = true` não percebe um link que já está de pé.** Quando o iOS reconectava ao
relógio antes de o app registrar o cliente GATT, o callback nunca chegava: o relógio ficava
"conectado" e sem notificações. Agora, se o link já existe, o app conecta direto.

**Identificadores de app errados.** O Nubank no iOS é `com.nu.iphone` e o iFood é
`br.com.brainweb.ifood`. Dá para conferir na API pública de busca da App Store.

**Armadilhas de protocolo já conhecidas** (herdadas do WearBridge, em
[`docs/gotchas.md`](docs/gotchas.md)):
- ignorar notificações `PreExisting` ao reconectar, senão o relógio vibra em rajada;
- negociar um MTU maior, senão títulos com acento chegam cortados;
- uma ligação atendida chega como `Modified`, não `Removed`;
- só o último acessório inscrito recebe o ANCS, então o Bluetooth do celular Android antigo
  precisa estar desligado.

**Ligação de WhatsApp não é categoria de ligação.** Para o ANCS, ela é uma notificação comum do
WhatsApp. O classificador olha o app e procura padrões em português e inglês ("Chamada de voz",
"Incoming call"). Sem confundir "Chamada de voz perdida" nem "tô ligando pra você".

## Instalação

1. No relógio: **Configurações → Sobre o relógio → Software** e toque 7× em **Versão do software**.
2. **Opções do desenvolvedor**: ative **Depuração ADB** e **Depurar por Wi‑Fi**. Mantenha a tela
   ligada, porque o Wi‑Fi do relógio desliga quando ela apaga e o `adb` cai.
3. No computador, na mesma rede:
   ```bash
   adb pair <ip>:<porta-de-pareamento> <código>
   adb connect <ip>:<porta>
   cd watch && ./gradlew installDebug
   adb shell appops set app.pulso.watch USE_FULL_SCREEN_INTENT allow
   ```
4. Desligue o Bluetooth do celular Android antigo.
5. Abra o Pulso no relógio, aceite as permissões, toque em **Parear** e escolha o iPhone.
6. No iPhone: **Emparelhar** e **Permitir** em "Compartilhar Notificações do Sistema?".

## Desenvolvimento

Kotlin, Jetpack Compose for Wear OS, minSdk 33.

```bash
cd watch
./gradlew testDebugUnitTest     # testes unitários (protocolo, chamadas, roteamento, ícones)
./gradlew installDebug          # instala no relógio conectado
adb logcat -s Pulso             # acompanha a conexão e as sessões
```

Os protocolos (`ancs/`, `ams/`) são Kotlin puro, sem APIs do Android, e são testados com bytes reais
do ANCS/AMS. O serviço só orquestra: liga `bluetooth/` aos protocolos e entrega os eventos para
`notifications/`, `calls/` e `media/`.

- Design: [`docs/superpowers/specs/`](docs/superpowers/specs/)
- Plano de implementação: [`docs/superpowers/plans/`](docs/superpowers/plans/)
- Roteiro de teste no relógio: [`docs/manual-test.md`](docs/manual-test.md)

## Roadmap

- **Fase 1** (atual): o que dá para fazer só com ANCS e AMS.
- **Fase 2**: um app no iPhone (instalado com SideStore e Apple ID grátis) para mandar os dados de
  saúde para o Apple Saúde, além de contatos e câmera remota.

## Créditos

- Os parsers do ANCS e a lista de armadilhas vêm do [WearBridge](https://github.com/k97/WearBridge)
  (MIT).
- Ícones de apps: [Simple Icons](https://simpleicons.org) (CC0). Ícones de categoria:
  [Material Design Icons](https://github.com/google/material-design-icons) (Apache 2.0). As marcas
  pertencem aos seus donos.

## Licença

[MIT](LICENSE)
