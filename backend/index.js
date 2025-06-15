const WebSocket = require("ws");

const wss = new WebSocket.Server({ port: 8080 });

const clients = new Map();
let clientIdCounter = 0;

console.log("WebRTC Signaling Server started on port 8080");

wss.on("connection", function connection(ws) {
  console.log("Client connected");
  const clientId = ++clientIdCounter;
  clients.set(clientId, ws);

  console.log(`Client ${clientId} connected. Total clients: ${clients.size}`);

  ws.send(
    JSON.stringify({
      type: "welcome",
      clientId: clientId,
      message: "Connected to signaling server!",
    })
  );

  ws.on("message", function incoming(data) {
    try {
      const message = JSON.parse(data.toString());
      console.log(`Message from client ${clientId}: ${message.type}`);
      switch (message.type) {
        case "offer":
          handleOffer(clientId, message);
          break;
        case "answer":
          handleAnswer(clientId, message);
          break;
        case "candidate":
          console.log('entering here');
          
          handleIceCandidate(clientId, message);
          break;

        default:
          console.log(`Unknown message type ${message.type}`);
          break;
      }
    } catch (err) {
      console.log(`Error parsing message, ${err}`);
      ws.send(
        JSON.stringify({
          type: "error",
          message: "Invalid message format",
        })
      );
    }
  });

  ws.on("close", function () {
    console.log(`Closing connection with client ${clientId}`);
    clients.delete(clientId);
    console.log(`Total clients: ${clients.size}`);
  });
});

function handleOffer(senderId, message){
  console.log(`Forwarding offer from client ${senderId}`);

  clients.forEach((client, clientId) =>{
    if(clientId !== senderId && client.readyState === WebSocket.OPEN){
      client.send(JSON.stringify({
        type: 'offer',
        offer: message.offer,
        from: senderId
      }));
    }
  });
}

function handleAnswer(senderId, message){
  console.log(`Forwarding answer from client ${senderId} to client ${message.to}`);

  const targetClient = clients.get(message.to);

    if(targetClient && targetClient.readyState === WebSocket.OPEN){
      targetClient.send(JSON.stringify({
        type: 'answer',
        answer: message.answer,
        from: senderId
      }));
    }
}

function handleIceCandidate(senderId, message){

  console.log(`Forwarding ICE candidate from client ${senderId}`);

  const targetClient = clients.get(message.to);

    if(targetClient && targetClient.readyState === WebSocket.OPEN){
      targetClient.send(JSON.stringify({
        type: 'candidate',
        candidate: message.candidate,
        from: senderId
      }));
    }
}