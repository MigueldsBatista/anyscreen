import { reactive } from 'vue';
import { io, Socket } from 'socket.io-client';

export const state = reactive({
  connected: false,
  socket: null as Socket | null,
  messages: [] as string[],
  clientId: null as string | null,
  roomId: null as string | null,
  roomMembers: [] as string[]
});

// Use the config or fallback to localhost
const URL = import.meta.env.VITE_BACKEND_URL ?? 'http://localhost:8080';

export const socket = io(URL, {
  autoConnect: false,
  transports: ['websocket']
});

socket.on('connect', () => {
  console.log('Connected to signaling server');
  state.connected = true;
  state.socket = socket;
});

socket.on('disconnect', () => {
  console.log('Disconnected from signaling server');
  state.connected = false;
  state.socket = null;
});

socket.on('welcome', (data: any) => {
  console.log('Welcome message received:', data);
  state.clientId = data.clientId;
});

socket.on('room-joined', (data: any) => {
  console.log('Successfully joined room:', data);
  state.roomId = data.roomId;
  state.roomMembers = data.members;
});

socket.on('user-joined', (data: any) => {
  console.log('User joined room:', data);
  if (!state.roomMembers.includes(data.clientId)) {
    state.roomMembers.push(data.clientId);
  }
});

socket.on('user-left', (data: any) => {
  console.log('User left room:', data);
  const index = state.roomMembers.indexOf(data.clientId);
  if (index > -1) {
    state.roomMembers.splice(index, 1);
  }
});

socket.on('error', (data: any) => {
  console.error('Socket error:', data);
});

socket.on('connect_error', (error: any) => {
  console.error('Connection error:', error);
  state.connected = false;
});

// Export connect and disconnect functions
export const connectSocket = () => {
  socket.connect();
};

export const disconnectSocket = () => {
  socket.disconnect();
};

// Room management functions
export const createRoom = async (): Promise<{ roomId: string; shareableLink: string } | null> => {
  try {
    const response = await fetch(`${URL}/api/create-room`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (response.ok) {
      const data = await response.json();
      return {
        roomId: data.roomId,
        shareableLink: data.shareableLink
      };
    } else {
      console.error('Failed to create room');
      return null;
    }
  } catch (error) {
    console.error('Error creating room:', error);
    return null;
  }
};

export const joinRoom = (roomId: string) => {
  if (state.socket) {
    socket.emit('join-room', { roomId });
  }
};

export const checkRoomExists = async (roomId: string) => {
  try {
    const response = await fetch(`${URL}/api/room/${roomId}`);
    if (response.ok) {
      return await response.json();
    } else {
      return { exists: false };
    }
  } catch (error) {
    console.error('Error checking room:', error);
    return { exists: false };
  }
};

// WebRTC signaling functions
export const sendOffer = (offer: RTCSessionDescriptionInit, targetClientId?: string) => {
  if (state.socket) {
    socket.emit('offer', {
      offer,
      to: targetClientId
    });
  }
};

export const sendAnswer = (answer: RTCSessionDescriptionInit, targetClientId: string) => {
  if (state.socket) {
    socket.emit('answer', {
      answer,
      to: targetClientId
    });
  }
};

export const sendIceCandidate = (candidate: RTCIceCandidate, targetClientId: string) => {
  if (state.socket) {
    socket.emit('candidate', {
      candidate,
      to: targetClientId
    });
  }
};
