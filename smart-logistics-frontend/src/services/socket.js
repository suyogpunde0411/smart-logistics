import { io } from "socket.io-client";

let socket = null;

const getSocketUrl = () => {
  const envUrl = import.meta.env.VITE_SOCKET_URL;
  if (envUrl && !envUrl.includes("localhost")) return envUrl;
  const host = typeof window !== "undefined" && window.location ? window.location.hostname : "localhost";
  return `http://${host}:5001`;
};

export const connectSocket = (token) => {
  if (socket) socket.disconnect();
  socket = io(getSocketUrl(), {
    auth: { token },
    query: { token },
    transports: ["websocket", "polling"],
  });
  return socket;
};

export const getSocket = () => socket;

export const disconnectSocket = () => {
  if (socket) {
    socket.disconnect();
    socket = null;
  }
};