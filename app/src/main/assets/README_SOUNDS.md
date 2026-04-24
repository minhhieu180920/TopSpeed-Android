# Hướng dẫn copy âm thanh từ game gốc TopSpeed-h

## Vị trí âm thanh trong game gốc

Âm thanh gốc nằm tại:
```
TopSpeed-h\TopSpeed-h\installer\tspeed\Sounds\
```

## Cấu trúc thư mục cần copy

```
app/src/main/res/raw/
├── en/
│   ├── menu/          # Menu sounds (sẽ được TTS đọc)
│   ├── numbers/       # Số đếm 0-100 (TTS đọc)
│   └── gameplay/      # Gameplay sounds (giữ người lồng tiếng)
│       ├── engine.ogg
│       ├── carstart.ogg
│       ├── crash.ogg
│       ├── bumpleft.ogg
│       ├── bumpright.ogg
│       ├── curbleft.ogg
│       ├── curbright.ogg
│       ├── brake.ogg
│       ├── horn.ogg
│       └── ...
```

## Cách copy

### 1. Copy thủ công

Mở folder gốc và copy các thư mục:

```bash
# Từ thư mục game gốc
cd "TopSpeed-h\TopSpeed-h\installer\tspeed\Sounds"

# Copy tất cả vào raw folder của Android
# (Thực hiện thủ công trong File Explorer)
```

### 2. Script PowerShell (chạy trong Windows)

```powershell
# Tạo thư mục đích
$dest = "C:\Users\MINH HIEU\Downloads\lap trinh abk\TopSpeed-Android\app\src\main\res\raw"

# Copy tất cả thư mục en
$src = "C:\Users\MINH HIEU\Downloads\lap trinh abk\TopSpeed-h\TopSpeed-h\installer\tspeed\Sounds"

Copy-Item -Path "$src\en" -Destination "$dest\en" -Recurse -Force
```

### 3. Từ file batch (Windows)

```batch
@echo off
set SRC=C:\Users\MINH HIEU\Downloads\lap trinh abk\TopSpeed-h\TopSpeed-h\installer\tspeed\Sounds
set DEST=C:\Users\MINH HIEU\Downloads\lap trinh abk\TopSpeed-Android\app\src\main\res\raw

xcopy /E /I /Y "%SRC%\en" "%DEST%\en"
echo Done!
pause
```

## Âm thanh quan trọng cần có

### Gameplay sounds (bắt buộc):
- `engine.ogg` - Tiếng máy
- `carstart.ogg` - Đề máy
- `crash.ogg` - Va chạm
- `bumpleft.ogg`, `bumpright.ogg` - Chạm ta lông
- `curbleft.ogg`, `curbright.ogg` - Kề múi bó vỉa
- `brake.ogg` - Phanh
- `horn.ogg` - Còi

### Menu sounds (TTS sẽ đọc thay):
- Tất cả file trong `en/menu/`
- Tất cả file trong `en/numbers/`

## Lưu ý

1. **Định dạng**: Game gốc dùng `.ogg` (Ogg Vorbis) - Android hỗ trợ tốt
2. **TTS cho menu**: Menu sounds trong game gốc là người lồng tiếng, nhưng Android version sẽ dùng Google TTS để đọc thay thế
3. **Gameplay sounds**: Giữ nguyên người lồng tiếng vì đây là hiệu ứng âm thanh, không phải thông tin cần đọc

## Sau khi copy

Build lại project:
```bash
cd TopSpeed-Android
./gradlew assembleDebug
```
