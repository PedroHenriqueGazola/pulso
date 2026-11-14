# Roteiro de teste manual (Fase 1)

Marque cada item com data e OK / Falhou.

## Pré-requisitos

- [ ] Bluetooth do S21 FE desligado (o relógio continua configurado).
- [ ] WearBridge não instalado no relógio e não pareado no iPhone.
- [ ] `adb shell appops set app.pulso.watch USE_FULL_SCREEN_INTENT allow`

## Pareamento

- [ ] Tocar "Parear iPhone", escolher o iPhone, aceitar o pareamento no iPhone.
- [ ] Aceitar "Compartilhar notificações do sistema?" no iPhone.
- [ ] Tela mostra "Conectado a <nome>".

## Notificações

- [ ] WhatsApp: chega com pop-up, vibração e texto completo.
- [ ] iMessage: idem.
- [ ] E-mail: idem.
- [ ] Mensagem com emoji e acentos aparece correta.
- [ ] Com Foco / Não Perturbe ligado no iPhone: aparece sem vibrar e sem pop-up.

## Dispensar

- [ ] Botão "Dispensar" no relógio remove a notificação do iPhone.
- [ ] Arrastar a notificação no relógio **não** remove do iPhone.
- [ ] Apagar no iPhone remove do relógio.

## Ligações

- [ ] Ligação de telefone: tela cheia + vibração; "Atender" atende no iPhone.
- [ ] Ligação de telefone: "Recusar" recusa.
- [ ] Atender no iPhone: relógio mostra "Em ligação no iPhone"; desligar → tela some.
- [ ] Ligação de WhatsApp (PT-BR): tela de chamada aparece.
- [ ] Ligação com o relógio carregando: tela aparece.

## Música

- [ ] Spotify: faixa e artista aparecem; play/pause, próxima, anterior funcionam.
- [ ] Apple Music: idem.
- [ ] Volume +/− altera no iPhone.
- [ ] Pausar no iPhone reflete no relógio.

## Reconexão

- [ ] Desligar Bluetooth do iPhone → "Aguardando iPhone"; religar → "Conectado" sem novo pareamento.
- [ ] Reconectar com várias notificações já no iPhone **não** vibra em rajada.
- [ ] Reiniciar o relógio → o serviço volta sozinho e reconecta.

## Bateria

- [ ] Um dia normal sem o app: ___% ao fim do dia.
- [ ] Um dia normal com o app: ___% ao fim do dia (alvo: no máximo ~10% a mais).
