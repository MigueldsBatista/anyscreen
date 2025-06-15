# WebRTC Signaling Flow Diagram

## Overview
This diagram illustrates the WebRTC signaling flow between two clients (script.js) and a signaling server (index.js).

## Components
- **Client A** (script.js) - WebRTC peer that initiates connection
- **Signaling Server** (index.js:8080) - WebSocket server that relays messages
- **Client B** (script.js) - WebRTC peer that receives connection

## Flow Steps

### 1. Initial Connection
```
Client A → WebSocket Connection → Signaling Server
Client B → WebSocket Connection → Signaling Server
```

### 2. Client Registration
```
Signaling Server → Welcome Message + ClientId → Client A
Signaling Server → Welcome Message + ClientId → Client B
```

### 3. Offer Creation & Transmission
```
Client A: Creates RTCPeerConnection
Client A: Calls getUserMedia() for video stream
Client A: Creates SDP Offer
Client A → {type: "offer", offer: sdp, to: clientB} → Signaling Server
Signaling Server → Forward Offer → Client B
```

### 4. Answer Creation & Transmission
```
Client B: Receives offer via handleIncomingOffer()
Client B: Creates RTCPeerConnection
Client B: Sets remote description (offer)
Client B: Creates SDP Answer
Client B → {type: "answer", answer: sdp, to: clientA} → Signaling Server
Signaling Server → Forward Answer → Client A
```

### 5. ICE Candidate Exchange & Selection
```
Client A: Generates multiple ICE candidates (Host, SRFLX, Relay)
Client A → {type: "candidate", candidate: ice, to: clientB} → Signaling Server
Signaling Server → Forward ICE → Client B

Client B: Generates multiple ICE candidates (Host, SRFLX, Relay)  
Client B → {type: "candidate", candidate: ice, to: clientA} → Signaling Server
Signaling Server → Forward ICE → Client A

WebRTC Engine: Performs connectivity checks between ALL candidate pairs
WebRTC Engine: Selects the best working candidate pair based on:
- Priority (Host > Server Reflexive > Relay)
- Connectivity success
- Latency and quality metrics
```

#### ICE Candidate Types:
- **Host**: Direct IP address of the device (highest priority)
- **Server Reflexive (SRFLX)**: Public IP discovered via STUN server
- **Relay**: IP of TURN relay server (lowest priority, always works)

#### Selection Process:
1. **Candidate Gathering**: Each peer discovers all possible network paths
2. **Connectivity Checks**: WebRTC automatically tests all candidate pairs
3. **Best Path Selection**: Chooses highest priority working connection
4. **Fallback**: If direct connection fails, uses TURN relay

### 6. Direct P2P Connection Established
```
WebRTC selects optimal candidate pair automatically
Client A ↔ Direct WebRTC Connection ↔ Client B
(Video/Audio streaming bypasses signaling server)
(Connection uses selected ICE candidate pair - usually Host-to-Host for same LAN)
```

## ICE Candidate Selection Details

### How ICE Selection Works:
1. **Both clients generate candidate lists** when `createPeerConnection()` is called
2. **Candidates are exchanged** via signaling server using WebSocket messages
3. **WebRTC performs connectivity checks** automatically between all candidate pairs
4. **Best candidate pair is selected** based on priority and connectivity
5. **Media flows through selected path** - signaling server is no longer needed

### Example ICE Candidate Priority:
```
Client A candidates:
- Host: 192.168.1.100:5000 (Priority: High)
- SRFLX: 203.0.113.10:5000 (Priority: Medium) 
- Relay: turn.server:3478 (Priority: Low)

Client B candidates:
- Host: 192.168.1.200:5001 (Priority: High)
- SRFLX: 203.0.113.20:5001 (Priority: Medium)
- Relay: turn.server:3479 (Priority: Low)

WebRTC tests combinations:
✓ A-Host ↔ B-Host (SELECTED - same LAN, highest priority)
✗ A-Host ↔ B-SRFLX (fails - different networks)
✓ A-SRFLX ↔ B-SRFLX (works but lower priority)
✓ A-Relay ↔ B-Relay (works but lowest priority)
```

## Key Functions in script.js

### Client-side Functions:
- `handleIncomingOffer(offer, fromClientId)` - Processes incoming offers
- `handleIncomingAnswer(answer)` - Processes incoming answers
- `handleIncomingIceCandidate(candidate)` - Processes ICE candidates
- `createPeerConnection()` - Sets up RTCPeerConnection
- `createOffer()` - Creates and sends SDP offer
- `createAnswer()` - Creates and sends SDP answer

### WebSocket Message Handler:
```javascript
socket.onmessage = function (event) {
    const message = JSON.parse(event.data);
    switch(message.type) {
        case 'welcome': // Store client ID
        case 'offer': // Handle incoming offer
        case 'answer': // Handle incoming answer
        case 'ice-candidate': // Handle ICE candidate
        case 'error': // Handle server errors
    }
}
```

## Key Functions in index.js

### Server-side Functions:
- `handleOffer(senderId, message)` - Forwards offers to all other clients
- `handleAnswer(senderId, message)` - Forwards answers to target client
- `handleIceCandidate(senderId, message)` - Forwards ICE candidates to target client

### WebSocket Events:
- `connection` - New client connects, assigns clientId
- `message` - Routes messages based on type (offer/answer/candidate)
- `close` - Cleans up client from clients Map

## Message Types

### Welcome Message
```json
{
  "type": "welcome",
  "clientId": 1,
  "message": "Connected to signaling server!"
}
```

### Offer Message
```json
{
  "type": "offer",
  "offer": { /* SDP offer object */ },
  "from": 1
}
```

### Answer Message
```json
{
  "type": "answer",
  "answer": { /* SDP answer object */ },
  "to": 1
}
```

### ICE Candidate Message
```json
{
  "type": "candidate",
  "candidate": { /* ICE candidate object */ },
  "to": 1
}
```

## Issues Found in Code

### index.js Issues:
1. Line 14: `clientes.set()` should be `clients.set()`
2. Line 24: `date.toString()` should be `message.toString()`
3. Line 50: `clients.delete(client)` should be `clients.delete(clientId)`
4. Line 55: `clientId` is not defined in scope
5. Line 67: `client.send()` should be `targetClient.send()`
6. Line 69: `offer` should be `answer`
7. Line 78: `client.send()` should be `targetClient.send()`
8. Line 80: `offer` should be `candidate`

### script.js Issues:
1. Line 21: `createPeerConnnection` has extra 'n' (typo)
2. Line 65: Case should be 'candidate' not 'ice-candidate'

## Data Flow Summary
1. **Connection Setup**: Clients connect to WebSocket server
2. **Signaling Phase**: SDP offers/answers and ICE candidates exchanged via server
3. **P2P Establishment**: Direct connection established between clients
4. **Media Streaming**: Video/audio flows directly between peers

The signaling server acts as a relay during the connection establishment phase but is not involved in the actual media transmission once the P2P connection is established.
