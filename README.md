# Pulso

Notificações, ligações e música do **iPhone** no **Galaxy Watch** (Wear OS), sem app no iPhone.

O relógio conversa direto com os serviços Bluetooth que o próprio iOS expõe para acessórios:

- **ANCS** (Apple Notification Center Service) — notificações e ligações.
- **AMS** (Apple Media Service) — o que está tocando e controles de mídia.

Projeto pessoal, instalado por `adb`. Não está na Play Store.

## O que funciona (Fase 1)

- Notificações do iPhone no relógio, com pop-up e vibração (respeita o Foco do iPhone).
- Botão "Dispensar" remove a notificação no iPhone.
- Ligações: atender, recusar, "em ligação no iPhone" (inclui ligações de WhatsApp).
- Controle de música do iPhone.
- Reconexão automática e início junto com o relógio.

## Limitações

- Não dá para responder mensagens (o ANCS não permite).
- Sem dados de saúde no Apple Saúde, contatos ou câmera — isso exige um app no iPhone (Fase 2).
- O relógio precisa ter sido configurado uma vez com um celular Android.

## Instalação

1. No relógio: Configurações → Sobre o relógio → Software → toque 7× em "Versão do software".
2. Configurações → Opções do desenvolvedor → ative "Depuração ADB" e "Depurar por Wi‑Fi".
3. No Mac (mesma rede):
   ```bash
   adb pair <ip>:<porta-de-pareamento>
   adb connect <ip>:<porta>
   cd watch && ./gradlew installDebug
   adb shell appops set app.pulso.watch USE_FULL_SCREEN_INTENT allow
   ```
4. Desligue o Bluetooth do celular Android antigo, se ele ainda estiver por perto.
5. Abra o Pulso no relógio e toque em "Parear iPhone".

## Desenvolvimento

```bash
cd watch
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

- Design: `docs/superpowers/specs/`
- Armadilhas conhecidas: `docs/gotchas.md`
- Roteiro de teste no relógio: `docs/manual-test.md`

## Créditos

Os parsers do protocolo ANCS foram adaptados do [WearBridge](https://github.com/k97/WearBridge)
(MIT), assim como a lista de armadilhas em `docs/gotchas.md`.

Os ícones de apps vêm do [Simple Icons](https://simpleicons.org) (CC0) e os ícones de categoria
do [Material Design Icons](https://github.com/google/material-design-icons) (Apache 2.0). As marcas
pertencem aos seus donos.

## Licença

MIT
