let myClientId = null;
let targetClientId = null;

const socket = new WebSocket('ws://localhost:8080');

socket.onopen = function (event) {
  console.log('You are Connected to WebSocket Server');
};

socket.onclose = function (event) {
  console.log('Disconnected from WebSocket server');
};

// Handle incoming offer
async function handleIncomingOffer(offer, fromClientId) {
  console.log('Received offer from client:', fromClientId);
  const isAllowed = confirm(`Do you want to accept incoming offer from client ${fromClientId}?`);
  if (!isAllowed) {
    console.log('Offer declined by user');
    return;
  }

  targetClientId = fromClientId;

  // Create peer connection first
  await createPeerConnnection(answerTextArea);

  await peerConnection.setRemoteDescription(offer);
  const answer = await peerConnection.createAnswer();
  await peerConnection.setLocalDescription(answer);

  // Send answer via WebSocket
  console.log('Sending answer to client:', targetClientId);
  socket.send(JSON.stringify({
    type: 'answer',
    answer: answer,
    to: targetClientId
  }));

  // Also update textarea for visibility
  answerTextArea.value = JSON.stringify(answer);
}

// Handle incoming answer
async function handleIncomingAnswer(answer) {
  console.log('Received answer');

  if (!peerConnection.currentRemoteDescription) {
    await peerConnection.setRemoteDescription(answer);
  }

  // Update textarea for visibility
  answerTextArea.value = JSON.stringify(answer);
}

// Handle incoming ICE candidate
async function handleIncomingIceCandidate(candidate) {
  console.log('Received ICE candidate');

  if (peerConnection) {
    await peerConnection.addIceCandidate(candidate);
  }
}

socket.onmessage = function (event) {
  try {
    const message = JSON.parse(event.data);
    console.log('Received message:', message.type);

    switch(message.type) {
      case 'welcome':
        myClientId = message.clientId;
        console.log('My client ID:', myClientId);
        break;
      case 'offer':
        handleIncomingOffer(message.offer, message.from);
        break;
      case 'answer':
        handleIncomingAnswer(message.answer);
        break;
      case 'candidate':
        handleIncomingIceCandidate(message.candidate);
        break;
      case 'error':
        console.error('Server error:', message.message);
        break;
    }
  } catch (error) {
    console.error('Error parsing message:', error);
  }
};

function sendMessage() {
  const messageInput = document.getElementById('messageInput');
  const message = messageInput.value;
  socket.send(message);
  messageInput.value = '';
}

let peerConnection;
let localStream;
let remoteStream;

const createOfferBtn = document.getElementById('create-offer');
const createAnswerBtn = document.getElementById('create-answer');
const addAnswerBtn = document.getElementById('add-answer');

const offerTextArea = document.getElementById('offer-sdp');
const answerTextArea = document.getElementById('answer-sdp');

const servers = {
  iceServers: [
    { urls: 'stun:stun.l.google.com:19302' },
    { urls: 'stun:stun.l.google.com:5349' },
    { urls: 'stun:stun1.l.google.com:3478' },
    { urls: 'stun:stun1.l.google.com:5349' },
    { urls: 'stun:stun2.l.google.com:19302' },
    { urls: 'stun:stun2.l.google.com:5349' },
    { urls: 'stun:stun3.l.google.com:3478' },
    { urls: 'stun:stun3.l.google.com:5349' },
    { urls: 'stun:stun4.l.google.com:19302' },
    { urls: 'stun:stun4.l.google.com:5349' }
  ]
};

const init = async () =>{
  localStream = await navigator.mediaDevices.getUserMedia({audio: false, video: true});
  console.log('here?');
};

const createPeerConnnection = async (sdbElement) =>{
  peerConnection = new RTCPeerConnection(servers);
  remoteStream = new MediaStream();
  document.getElementById('user-2').srcObject = remoteStream;

  localStream.getTracks().forEach(track => {
    peerConnection.addTrack(track, localStream);
  });
  peerConnection.ontrack = async (e) => {
    e.streams[0].getTracks().forEach(track => {
      remoteStream.addTrack(track);
    });
  };

  peerConnection.onicecandidate = async (e) =>{
    if(e.candidate && targetClientId){
      console.log('Sending ICE candidate');
      socket.send(JSON.stringify({
        type: 'candidate',
        candidate: e.candidate,
        to: targetClientId
      }));
    }
    if (e.candidate) {
      sdbElement.value = JSON.stringify(peerConnection.localDescription);
    }
  };

};

const createOffer = async (providedTargetClientId) => {
  targetClientId = providedTargetClientId;

  try {
    await createPeerConnnection(offerTextArea);

    const offer = await peerConnection.createOffer();
    await peerConnection.setLocalDescription(offer);

    // Send offer via WebSocket
    console.log('Sending offer to client:', targetClientId);
    socket.send(JSON.stringify({
      type: 'offer',
      offer: offer,
      to: targetClientId
    }));
  } catch (error) {
    console.error('Error creating offer:', error);
  }
};

createOfferBtn.addEventListener('click', () => createOffer());

init();

// Only create automatic offer if clientId is provided in URL
const params = new URLSearchParams(document.location.search);
console.log(params);

const clientId = params.get('clientId');
if (clientId) {
  // Wait for socket connection before creating offer
  console.log('preparing to offer ', clientId);

  socket.addEventListener('open', () => {
    setTimeout(() => createOffer(clientId), 1000); // Small delay to ensure connection is stable
  });
}
