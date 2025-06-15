/* 
What do i imagine about the flow

we must have a url, this url somehow will point to the original
We have Client A and Client B
when client B connects to a url, this url could contain the id
of client A, and this will generate a offer to client A
who will chose to accept or deny it, if he accepts it
the answer is sent to the server and added to client B
and then they can comunicate using web rtc

initial goal ->
start the page with nothing and link the remote camera to client B
client B will use the url that contains client A ip

*/

const { randomUUID } = require("node:crypto");

const WebSocket = require("ws");

const wss = new WebSocket.Server({ port: 8080 });

const clients = new Map();

console.log("WebRTC Signaling Server started on port 8080");

wss.on("connection", function connection(ws) {
  console.log("Client connected");
  const clientId = randomUUID();
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
  let found = false;

  clients.forEach((client, clientId) =>{
    if(clientId !== senderId && message.to === clientId && client.readyState === WebSocket.OPEN){
      console.log(`Forwarding offer from client ${senderId}`);
      found = true;
      client.send(JSON.stringify({
        type: 'offer',
        offer: message.offer,
        from: senderId
      }));
    }
  });
  if(!found){
    console.log(`Client ${message.to} not found`);
  }
}

function handleAnswer(senderId, message){
  
  const targetClient = clients.get(message.to);
  
  if(targetClient && targetClient.readyState === WebSocket.OPEN){
      console.log(`Forwarding answer from client ${senderId} to client ${message.to}`);
      targetClient.send(JSON.stringify({
        type: 'answer',
        answer: message.answer,
        from: senderId
      }));
    }
}

function handleIceCandidate(senderId, message){

  const targetClient = clients.get(message.to);
  
  if(targetClient && targetClient.readyState === WebSocket.OPEN){
      console.log(`Forwarding ICE candidate from client ${senderId}`);
      targetClient.send(JSON.stringify({
        type: 'candidate',
        candidate: message.candidate,
        from: senderId
      }));
    }
}