# Fluxo de Funcionamento do Sistema

## 1. Host inicia o app
**Servidor Node local rodando** — capaz de:
- Gerar e armazenar um roomId único
- Criar os endpoints `setup-room` e `teardown-room`
- Manter o roomId e a conexão WebSocket do host em memória (no backend e no vuex)

**Interface do host** — exibe o link gerado para compartilhar.

**Mecanismo de WebSocket** configurado para comunicação bidirecional entre host e servidor.

## 2. Host compartilha link e convidado abre no navegador
**Servidor Node** que escuta requisições HTTP na rota `/{roomId}`.

**Validação do roomId** — rejeitar conexões inválidas.

**Página cliente (guest)** carregada no navegador:
- Cliente web com código para conectar via WebSocket
- Interface que permite enviar requisição para entrar na sala

## 3. Servidor valida ID, permite conexão e envia convite para host
**WebSocket server** que:
- Recebe conexão do guest
- Verifica se o roomId existe e está ativo
- Envia notificação (mensagem) para o host informando que alguém quer entrar

**Host interface**:
- Recebe mensagem via WebSocket
- Apresenta opção para aceitar ou recusar o convidado

## 4. Host aceita ou recusa
**Host envia mensagem via WebSocket ao servidor**:
- **Aceitar**: sinaliza para o guest que pode iniciar conexão
- **Recusar**: encerra tentativa de conexão

**Servidor** repassa decisão para o guest.

## 5. Estabelecimento da conexão WebRTC
**Host**:
- Captura tela via `getDisplayMedia`
- Cria `RTCPeerConnection`
- Adiciona stream capturado
- Cria oferta SDP e envia via WebSocket para guest

**Guest**:
- Recebe oferta via WebSocket
- Cria resposta SDP
- Envia resposta via WebSocket para host

**Ambos**:
- Troca de candidatos ICE via WebSocket
- Estabelecem conexão ponto a ponto

## 6. Transmissão ponto a ponto da tela
- **Host** envia vídeo da tela capturada diretamente para o guest via WebRTC
- **Guest** recebe e exibe o vídeo em elemento `<video>`
- **Servidor Node** atua apenas como canal de sinalização, não passando mídia

## Implementação do Sistema de Rotas

### Rotas distintas para host e guest
**Exemplo:**
- Host: `/host/:roomId`
- Guest: `/room/:roomId`

### Componentes dedicados para cada rota
Cada rota carrega um componente específico:
- `/host/:roomId` → `HostView.vue`
- `/room/:roomId` → `GuestView.vue`

### Benefícios
- Sem ifs no código — a distinção é feita automaticamente pelo roteador
- Código separado por responsabilidade

### Exemplo de configuração com Vue Router

```js
const routes = [
    {
        path: '/host',
        name: 'Host',
        component: HostView,
    },
    {
        path: '/room/:roomId',
        name: 'GuestRoom',
        component: GuestView,
        props: true,
    }
];
```

## Sistema de Validação de RoomID

### 1. Como e onde validar um roomId?
- **No servidor**: validação principal e autoritária
- **No cliente**: validação preliminar para feedback rápido, mas não confiável

### 2. O que validar?
- Se o roomId existe no servidor
- Se o roomId está ativo (host conectado e sala não encerrada)
- Se o guest/host tem permissão para acessar a sala (autenticação/autorização, se houver)
- Evitar sala inválida, expiradas ou rooms "fantasmas"

### 3. Como validar?

**No servidor**:
- Armazenar rooms ativos: manter um registro (em memória, banco, cache) com roomId e seu estado
- Na requisição do guest/host: quando receber o pedido para entrar na sala, verificar se o roomId está ativo
- Resposta: aceitar ou rejeitar o pedido (com mensagem clara)

**No cliente**:
- Antes de tentar conectar, pode chamar uma API para verificar se o roomId existe (opcional)
- Ou tentar conexão e reagir ao retorno de erro do servidor

### 4. Exemplo de fluxo de validação
1. Guest entra no link `/room/:roomId`
2. Cliente pode fazer requisição ao servidor: `GET /validate-room/:roomId`
3. Servidor responde:
     - `200 OK` + dados da sala se válido
     - `404` / `400` se inválido ou não ativo
4. Se válido, cliente conecta via WebSocket/socket.io para entrar na sala
5. Se inválido, mostra mensagem de erro no cliente

### 5. Segurança adicional
- Implementar tokens temporários vinculados ao roomId para evitar acesso indevido
- Caso use autenticação, validar permissões no servidor
