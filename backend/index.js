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
const { createServer } = require("http");
const { Server } = require("socket.io");

// Create HTTP server
const httpServer = createServer();

// Create Socket.IO server with CORS configuration
const io = new Server(httpServer, {
  cors: {
    origin: [
      "http://localhost:5173", // Vite dev server
      "http://localhost:3000", // Alternative dev port
      "http://localhost:8080", // Same origin
      "http://127.0.0.1:5173",
      "http://127.0.0.1:3000",
      "http://127.0.0.1:8080"
    ],
    methods: ["GET", "POST"],
    credentials: true
  },
  allowEIO3: true
});

// Store client metadata and active rooms
const clientData = new Map();
const activeRooms = new Map(); // roomId -> { createdAt, clients: Set(), createdBy: clientId }

console.log("WebRTC Signaling Server started on port 8080");

// Helper function to create a new room
function createRoom(createdBy = null) {
  const roomId = randomUUID();
  activeRooms.set(roomId, {
    createdAt: new Date(),
    clients: new Set(),
    createdBy: createdBy
  });
  console.log(`Room ${roomId} created by ${createdBy || 'server'}`);
  return roomId;
}

// Helper function to clean up empty rooms
function cleanupRoom(roomId) {
  const room = activeRooms.get(roomId);
  if (room && room.clients.size === 0) {
    activeRooms.delete(roomId);
    console.log(`Room ${roomId} deleted (empty)`);
  }
}

io.on("connection", function(socket) {
  console.log("Client connected");
  const clientId = randomUUID();
  
  // Store client data (no room initially)
  clientData.set(socket.id, {
    clientId: clientId,
    room: null
  });

  console.log(`Client ${clientId} connected. Total clients: ${io.sockets.sockets.size}`);

  socket.emit("welcome", {
    type: "welcome",
    clientId,
    message: "Connected to signaling server!",
  });

  // Handle joining a specific room with roomId
  socket.on("join-room", function(data) {
    const roomId = data.roomId;
    
    if (!roomId) {
      socket.emit("error", { message: "Room ID is required" });
      return;
    }

    // Check if room exists
    if (!activeRooms.has(roomId)) {
      socket.emit("error", { message: `Room ${roomId} does not exist or has expired` });
      return;
    }

    // Leave current room if any
    const client = clientData.get(socket.id);
    if (client && client.room) {
      socket.leave(client.room);
      const oldRoom = activeRooms.get(client.room);
      if (oldRoom) {
        oldRoom.clients.delete(clientId);
        socket.to(client.room).emit("user-left", {
          clientId: clientId,
          roomId: client.room
        });
        cleanupRoom(client.room);
      }
    }

    // Join new room
    socket.join(roomId);
    client.room = roomId;
    
    const room = activeRooms.get(roomId);
    room.clients.add(clientId);

    console.log(`Client ${clientId} joined room: ${roomId}`);
    
    // Notify others in room about new user
    socket.to(roomId).emit("user-joined", {
      clientId: clientId,
      roomId: roomId
    });

    // Send current room members to the new client
    const roomMembers = Array.from(room.clients).filter(id => id !== clientId);
    socket.emit("room-joined", {
      roomId: roomId,
      members: roomMembers
    });
  });

  socket.on("offer", function(message) {
    console.log(`Offer from client ${clientId}`);
    
    if (message.to) {
      // Direct message to specific client
      socket.to(message.to).emit('offer', {
        type: 'offer',
        offer: message.offer,
        from: clientId
      });
    } else {
      // Broadcast to current room (excluding sender)
      const client = clientData.get(socket.id);
      if (client && client.room) {
        socket.to(client.room).emit('offer', {
          type: 'offer',
          offer: message.offer,
          from: clientId
        });
      }
    }
  });

  socket.on("answer", function(message) {
    console.log(`Answer from client ${clientId} to ${message.to}`);
    
    // Direct message to specific client
    socket.to(message.to).emit('answer', {
      type: 'answer',
      answer: message.answer,
      from: clientId
    });
  });

  socket.on("candidate", function(message) {
    console.log(`ICE candidate from client ${clientId} to ${message.to}`);
    
    // Direct message to specific client
    socket.to(message.to).emit('candidate', {
      type: 'candidate',
      candidate: message.candidate,
      from: clientId
    });
  });

  socket.on("disconnect", function() {
    console.log(`Client ${clientId} disconnected`);
    
    const client = clientData.get(socket.id);
    if (client && client.room) {
      const room = activeRooms.get(client.room);
      if (room) {
        room.clients.delete(clientId);
        socket.to(client.room).emit("user-left", {
          clientId: clientId,
          roomId: client.room
        });
        cleanupRoom(client.room);
      }
      clientData.delete(socket.id);
    }
    
    console.log(`Total clients: ${io.sockets.sockets.size}`);
  });
});

// REST API endpoints for secure room management
httpServer.on('request', (req, res) => {
  // Set CORS headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
  
  if (req.method === 'OPTIONS') {
    res.writeHead(200);
    res.end();
    return;
  }

  if (req.url === '/api/create-room' && req.method === 'POST') {
    // Create a new secure room
    const roomId = createRoom('web-client');
    const shareableLink = `${req.headers.origin || 'http://localhost:5173'}?room=${roomId}`;
    
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ 
      roomId: roomId,
      shareableLink: shareableLink,
      created: true,
      timestamp: new Date().toISOString(),
      message: 'Room created successfully. Share the link with others to join.'
    }));
  } 
  else if (req.url.startsWith('/api/room/') && req.method === 'GET') {
    // Check if room exists and get info
    const roomId = req.url.split('/')[3];
    const room = activeRooms.get(roomId);
    
    if (room) {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({
        roomId: roomId,
        exists: true,
        memberCount: room.clients.size,
        createdAt: room.createdAt,
        // Don't expose member IDs for security
        canJoin: true
      }));
    } else {
      res.writeHead(404, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ 
        error: 'Room not found or has expired',
        exists: false,
        canJoin: false
      }));
    }
  }
  else if (req.url === '/api/rooms/active' && req.method === 'GET') {
    // Get count of active rooms (no sensitive data)
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ 
      activeRoomCount: activeRooms.size,
      timestamp: new Date().toISOString()
    }));
  }
  else {
    res.writeHead(404, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ error: 'Endpoint not found' }));
  }
});

// Start the server
httpServer.listen(8080, () => {
  console.log("Socket.IO server listening on port 8080");
});