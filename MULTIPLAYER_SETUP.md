# Hướng dẫn kích hoạt Multiplayer cho TopSpeed Audio Racing

## Tổng quan

TopSpeed Audio Racing đã được tích hợp tính năng multiplayer để kết nối với server Node.js của bạn (dự án 2 - One Piece Arena).

## Bước 1: Thêm API vào server dự án 2

Mở file `server.js` trong thư mục `one-piece-arena` và thêm các API endpoints sau:

```javascript
// Copy nội dung từ file này và thêm vào server.js
// Hoặc chạy file nàystandalone:

// Thêm vào cuối server.js
require('./topSpeedServerApi.js')(app, io, rooms);
```

**Hoặc đơn giản hơn**, copy đoạn code từ `app/src/main/assets/topSpeedServerApi.js` và dán vào sau phần khai báo routes của One Piece Arena.

## Bước 2: Cấu hình IP server

Trong TopSpeed Android, sửa `ServerConfig.kt` để trỏ đến IP của máy bạn:

```kotlin
// Trong TopSpeed-Android/app/src/main/java/com/topspeed/audio/network/ServerConfig.kt

// Đổi DEFAULT_SERVER_URL thành IP của máy bạn
const val DEFAULT_SERVER_URL = "http://192.168.1.100:3000"  // THAY ĐỔI IP
```

## Bước 3: Kết nối từ điện thoại

### Cách 1: Cùng mạng WiFi (Khuyến nghị)
1. Đảm bảo điện thoại và máy tính cùng mạng WiFi
2. Lấy IP của máy tính: `ipconfig` (Windows) hoặc `ifconfig` (Mac/Linux)
3. Sửa IP trong code như trên

### Cách 2: Qua Internet
1. Deploy server lên Railway hoặc server hosting
2. Sử dụng URL public của server
3. Server phải support CORS

## Cách chơi Multiplayer

### Người chơi 1 (Tạo phòng):
1. Mở app TopSpeed
2. Kết nối server (sẽ có nút trong menu)
3. Tạo phòng mới
4. Đợi người chơi 2

### Người chơi 2 (Vào phòng):
1. Mở app TopSpeed  
2. Kết nối cùng server
3. Nhập mã phòng (4 ký tự)
4. Đợi host bắt đầu

## API Endpoints

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/topspeed/ping` | Kiểm tra server |
| POST | `/api/topspeed/room/create` | Tạo phòng |
| POST | `/api/topspeed/room/join` | Vào phòng |
| POST | `/api/topspeed/room/leave` | Rời phòng |
| GET | `/api/topspeed/room/list` | Danh sách phòng |
| POST | `/api/topspeed/race/state` | Gửi race state |
| POST | `/api/topspeed/race/start` | Bắt đầu race |

## Troubleshooting

### Không kết nối được
- Kiểm tra server đang chạy: `curl http://localhost:3000/api/topspeed/ping`
- Kiểm tra IP đúng chưa
- Tắt tường lửa trên máy tính

### Không thấy phòng
- Refresh danh sách phòng
- Tạo phòng mới

### Lag
- Chơi cùng mạng WiFi sẽ ít lag hơn
- Kiểm tra tốc độ mạng

## Lưu ý

1. **Server đã chạy project 2** - Phải thêm API endpoints của TopSpeed vào
2. **Cùng port 3000** - TopSpeed dùng chung server với One Piece Arena
3. **Prefix `/api/topspeed/`** - Để tránh xung đột với API của One Piece Arena