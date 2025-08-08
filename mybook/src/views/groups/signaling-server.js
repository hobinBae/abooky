// // signaling-server.js
// const WebSocket = require('ws');
// const wss = new WebSocket.Server({ port: 3001 });

// const rooms = new Map();

// wss.on('connection', (ws) => {
//   ws.on('message', (message) => {
//     const data = JSON.parse(message);
//     // 메시지 라우팅 로직
//     // 같은 그룹의 다른 참여자들에게 메시지 전달
//   });
// });

const LIVEKIT_URL = 'wss://your-project.livekit.cloud';
const API_URL = '/api/token';