# 🛣️ Roadmap: Screen Capture with WebRTC

## 🟨 Semana 1: Captura de Tela com Java

### Aprender/Usar:
- `java.awt.Robot` para capturar imagens da tela
- `BufferedImage` e `ImageIO` para manipulação/salvamento de frames

### Implementações práticas:
- App Java que tira screenshots da tela periodicamente (30-60 FPS)
- Testar desempenho e resolução
- Salvar os frames temporariamente como JPEG/PNG para debug

### Pontos de atenção:
- Cuidado com consumo de CPU ao capturar muitos frames
- Testar em diferentes resoluções e múltiplos monitores
- Medir tempo de captura para garantir FPS alvo

## 🟨 Semana 2: Codificação de vídeo (MJPEG / YUV)

### Aprender/Usar:
- Conversão de `BufferedImage` para `byte[]`
- Codificação simples como MJPEG para testes
- Estudo inicial de integração com ferramentas como ffmpeg via process builder

### Implementações práticas:
- Criar método que converte os frames em sequência JPEG ou stream binário
- Medir e ajustar tempo de serialização

### Pontos de atenção:
- MJPEG é simples, mas não tão eficiente quanto H.264 — ideal apenas para testes iniciais
- Se precisar de compressão real-time eficiente, estudar JNI com libx264 ou GStreamer

## 🟨 Semana 3: Servidor WebSocket para signaling

### Aprender/Usar:
- Biblioteca como Java-WebSocket ou tyrus
- Protocolo de signaling com mensagens JSON (offer, answer, ICE)

### Implementações práticas:
- Servidor WebSocket em Java que aceita conexões de clientes
- Mensagens JSON com `type: offer/answer/candidate`, `sdp`, `candidate`, etc.

### Pontos de atenção:
- Cada cliente precisa de um ID para manter o estado da conexão
- WebSocket deve gerenciar múltiplos clientes (concorrência com threads ou executors)

## 🟨 Semana 4: Frontend WebRTC (Vue.js)

### Aprender/Usar:
- `RTCPeerConnection`, `MediaStream`, `getUserMedia`, `setRemoteDescription`, `addIceCandidate`
- Vue.js para interface leve

### Implementações práticas:
- Cliente Vue com botão "Conectar"
- Criação de offer → envio via WebSocket → recebimento da answer
- Receber o vídeo e exibir no `<video>` tag

### Pontos de atenção:
- WebRTC exige HTTPS/TLS (usar self-signed certs ou rodar local com exceção)
- Lidar com eventos ICE e estados da conexão

## 🟨 Semana 5: Integração: envio de vídeo do Java via WebRTC

### Aprender/Usar:
- Integração com WebRTC em Java (via aiortc-java bindings ou via lib externa em C com JNI, ou usar GStreamer com pipeline H.264 e RTP)

### Implementações práticas:
- Criar pipeline de envio de vídeo do Java para o RTCPeerConnection
- Estudar libwebrtc via JNI (mais complexo)
- Alternativa: usar um servidor WebRTC intermediário (como mediasoup ou Janus) e Java apenas como produtor de vídeo via RTP

### Pontos de atenção:
- WebRTC em Java é limitado — talvez precise usar FFmpeg ou GStreamer externamente e Java apenas para controle
- O encoder precisa alimentar o WebRTC com MediaStreamTrack

## 🟨 Semana 6: Controle via API REST/WebSocket

### Aprender/Usar:
- Criar endpoints de controle: iniciar/parar captura, mudar qualidade
- Usar JSON para comandos e eventos

### Implementações práticas:
- Cliente Vue envia comandos via WebSocket (ou REST)
- Java responde e altera o comportamento (start/stop captura, mudar FPS)

### Pontos de atenção:
- Sincronização dos estados: streaming ligado/desligado, erros, reconexões
- Garantir robustez da comunicação

## 🟨 Semana 7: Otimização e testes de carga

### O que fazer:
- Medir uso de CPU e rede
- Testar estabilidade com reconexões e longa duração
- Log detalhado com SLF4J + Logback

### Tarefa prática:
- Rodar o app em 2+ máquinas na LAN
- Medir latência e FPS
- Logar tempo por frame, memória, threads

### Pontos de atenção:
- Concorrência: gerenciar threads corretamente
- Escalabilidade: quantos clientes simultâneos?

## 🟨 Semana 8: Empacotamento

### Aprender/Usar:
- `jpackage` ou launch4j para gerar executável
- Geração de `.deb`, `.msi`, `.appimage` etc

### Tarefa prática:
- Empacotar e testar em outros dispositivos
- Verificar permissões e compatibilidade

## 🔧 Alternativa com MJPEG (caso WebRTC se prove inviável)

Se não for possível integrar WebRTC diretamente com Java:
- Use MJPEG via HTTP como fallback (streaming `multipart/x-mixed-replace`)
- Pode ser feito com simples `HttpServer` + `BufferedImage` → JPEG

## 📌 Conclusão: Pontos de atenção críticos

WebRTC com Java é desafiador, mas possível com:
- GStreamer via pipeline RTP/RTMP
- Servidor intermediário que consome vídeo e entrega WebRTC
- WebSocket é obrigatório para signaling (SDP e ICE)
- A captura de tela em Java é simples, mas a codificação e envio via WebRTC exigem estudo aprofundado
