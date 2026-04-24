// Server API cho TopSpeed Audio Racing Multiplayer
// Thêm vào server.js của dự án 2 (one-piece-arena)

// Room management for TopSpeed
const topSpeedRooms = new Map();

// Generate room code
function generateRoomCode() {
    const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
    let code = '';
    for (let i = 0; i < 4; i++) {
        code += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return code;
}

// API Routes cho TopSpeed
app.get('/api/topspeed/ping', (req, res) => {
    res.json({ success: true, message: 'TopSpeed server ready' });
});

// Tạo phòng đua
app.post('/api/topspeed/room/create', (req, res) => {
    try {
        const { trackId = 0, laps = 3, playerName = 'Player' } = req.body;

        // Generate unique room code
        let roomCode;
        do {
            roomCode = generateRoomCode();
        } while (topSpeedRooms.has(roomCode));

        const playerId = 'player_' + Date.now();

        const room = {
            code: roomCode,
            host: playerId,
            hostName: playerName,
            players: [{
                id: playerId,
                name: playerName,
                isHost: true,
                position: 0,
                speed: 0,
                lap: 0,
                finished: false
            }],
            trackId,
            laps,
            maxPlayers: 4,
            status: 'waiting', // waiting, racing, finished
            createdAt: Date.now()
        };

        topSpeedRooms.set(roomCode, room);

        res.json({
            success: true,
            roomCode,
            playerId,
            playerName,
            room: {
                code: roomCode,
                hostName: playerName,
                trackId,
                laps,
                playerCount: 1,
                maxPlayers: 4
            }
        });

        console.log(`[TopSpeed] Room created: ${roomCode} by ${playerName}`);
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
});

// Tham gia phòng
app.post('/api/topspeed/room/join', (req, res) => {
    try {
        const { roomCode, playerName = 'Player' } = req.body;

        const room = topSpeedRooms.get(roomCode.toUpperCase());
        if (!room) {
            return res.status(404).json({ success: false, error: 'Phòng không tồn tại' });
        }

        if (room.players.length >= room.maxPlayers) {
            return res.status(400).json({ success: false, error: 'Phòng đã đầy' });
        }

        if (room.status !== 'waiting') {
            return res.status(400).json({ success: false, error: 'Cuộc đua đã bắt đầu' });
        }

        const playerId = 'player_' + Date.now();

        const player = {
            id: playerId,
            name: playerName,
            isHost: false,
            position: 0,
            speed: 0,
            lap: 0,
            finished: false
        };

        room.players.push(player);

        // Broadcast to other players via Socket.io if available
        if (io) {
            io.to(roomCode).emit('player_joined', player);
        }

        res.json({
            success: true,
            roomCode,
            playerId,
            playerName,
            room: {
                code: roomCode,
                players: room.players.map(p => ({
                    id: p.id,
                    name: p.name,
                    isHost: p.isHost
                })),
                trackId: room.trackId,
                laps: room.laps,
                status: room.status
            }
        });

        console.log(`[TopSpeed] Player ${playerName} joined room ${roomCode}`);
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
});

// Rời phòng
app.post('/api/topspeed/room/leave', (req, res) => {
    try {
        const { roomCode, playerId } = req.body;

        const room = topSpeedRooms.get(roomCode);
        if (!room) {
            return res.json({ success: true });
        }

        // Remove player
        room.players = room.players.filter(p => p.id !== playerId);

        // If no players left, delete room
        if (room.players.length === 0) {
            topSpeedRooms.delete(roomCode);
            console.log(`[TopSpeed] Room ${roomCode} deleted (empty)`);
        } else {
            // If host left, reassign host
            if (!room.players.some(p => p.isHost)) {
                room.players[0].isHost = true;
                room.host = room.players[0].id;
            }

            // Broadcast via Socket.io
            if (io) {
                io.to(roomCode).emit('player_left', { playerId });
            }
        }

        res.json({ success: true });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
});

// Lấy danh sách phòng
app.get('/api/topspeed/room/list', (req, res) => {
    try {
        const rooms = Array.from(topSpeedRooms.values())
            .filter(r => r.status === 'waiting')
            .map(r => ({
                code: r.code,
                hostName: r.hostName,
                playerCount: r.players.length,
                trackName: r.trackId === 0 ? 'Default Circuit' :
                         r.trackId === 1 ? 'Professional Circuit' : 'Adventure Trail',
                maxPlayers: r.maxPlayers,
                laps: r.laps
            }));

        res.json({ success: true, rooms });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
});

// Gửi race state
app.post('/api/topspeed/race/state', (req, res) => {
    try {
        const { roomCode, playerId, position, speed, lap, finished, timestamp } = req.body;

        const room = topSpeedRooms.get(roomCode);
        if (!room) {
            return res.json({ success: false, error: 'Room not found' });
        }

        // Update player state
        const player = room.players.find(p => p.id === playerId);
        if (player) {
            player.position = position;
            player.speed = speed;
            player.lap = lap;
            player.finished = finished;
        }

        // Broadcast state to other players
        if (io) {
            io.to(roomCode).emit('player_state', {
                playerId,
                position,
                speed,
                lap,
                finished,
                timestamp
            });
        }

        res.json({ success: true });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
});

// Bắt đầu cuộc đua
app.post('/api/topspeed/race/start', (req, res) => {
    try {
        const { roomCode, playerId } = req.body;

        const room = topSpeedRooms.get(roomCode);
        if (!room) {
            return res.status(404).json({ success: false, error: 'Room not found' });
        }

        // Verify host
        if (room.host !== playerId) {
            return res.status(403).json({ success: false, error: 'Only host can start race' });
        }

        // Start race
        room.status = 'racing';

        // Reset player states
        room.players.forEach(p => {
            p.position = 0;
            p.speed = 0;
            p.lap = 0;
            p.finished = false;
        });

        // Broadcast start
        if (io) {
            io.to(roomCode).emit('race_started', {
                trackId: room.trackId,
                laps: room.laps,
                players: room.players.map(p => p.id)
            });
        }

        res.json({ success: true });

        console.log(`[TopSpeed] Race started in room ${roomCode}`);
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
});

// Cleanup old rooms (call periodically)
function cleanupRooms() {
    const now = Date.now();
    const maxAge = 24 * 60 * 60 * 1000; // 24 hours

    for (const [code, room] of topSpeedRooms) {
        if (now - room.createdAt > maxAge || room.players.length === 0) {
            topSpeedRooms.delete(code);
            console.log(`[TopSpeed] Cleaned up room ${code}`);
        }
    }
}

// Cleanup every hour
setInterval(cleanupRooms, 60 * 60 * 1000);

module.exports = { app }; // Export for integration