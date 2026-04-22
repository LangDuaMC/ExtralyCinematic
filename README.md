# 🎬 ExtralyCinematic v2.5

The ultimate, ultra-lightweight cinematic and cutscene plugin for Paper 1.21.11+. Create breathtaking camera movements, visual effects, and synchronized actions with zero NMS hassle.

[🇻🇳 Bấm vào đây để xem hướng dẫn Tiếng Việt](#-extralycinematic-v25-vietnamese)

---

## 🚀 Features (v2.5 The Optimization Update)
* **Pure Bukkit API:** Completely removed `CommandAPI` and NMS dependencies. The plugin is now incredibly lightweight (~50KB), future-proof, and immune to version-update crashes.
* **Bulletproof GUI 2.0:** Upgraded to the `InventoryHolder` standard, preventing all click-glitches and UI bugs.
* **Deep GUI Editor:** Manage everything from a 45-slot dashboard. Edit BGM, Zoom, Camera Shake, Focus Points, and Duration without ever typing a manual configuration command.
* **Smooth Interpolation:** Utilizes Catmull-Rom splines for buttery-smooth camera movements and rotations, completely eliminating spectator de-sync.
* **Action Frames:** Trigger commands, titles, and subtitles exactly when the camera hits a specific frame.
* **Soft-Dependencies:** Full support for PlaceholderAPI, WorldGuard, and MythicMobs.

## 📦 Installation
1. Download `ExtralyCinematic-2.5.jar`.
2. Place it inside your server's `plugins/` folder.
3. Start or restart your server (Do not use `/reload`).
4. *(Optional)* Install PlaceholderAPI, WorldGuard, or MythicMobs for extended features.

## ⌨️ Commands & Permissions
**Main Command:** `/cinematic` or `/cine`
**Permission:** `cinematic.cmd` (Usage) | `cinematic.admin` (Reload)

| Command | Description |
| :--- | :--- |
| `/cine edit` | Opens the Main Dashboard GUI. |
| `/cine rec <name> [seconds]` | Creates a cinematic with a countdown recording. |
| `/cine record start <name>` | Starts freehand recording. |
| `/cine record stop` | Stops freehand recording. |
| `/cine play <player> <name>` | Plays a cinematic for a specific player. |
| `/cine stop <player>` | Force-stops the cinematic for a player. |
| `/cine reload` | Reloads configs and cinematic data. |

## 📖 Wiki & Usage Guide

### 1. Creating a Cinematic
There are two ways to create a cinematic:
* **The Quick Way (Recording):** Type `/cine rec <Name> 3`. The plugin will count down from 3, then record your exact movements in-game as a camera path. Type `/cine record stop` when done.
* **The Precise Way (Manual):** Type `/cine edit`, click **"+ Add Cinematic"** (or use commands to create a track), and manually add frames at your current location.

### 2. The Dashboard Editor (`/cine edit`)
This is the heart of v2.5. Opening the dashboard displays all your cinematics. Click on one to open the **Deep Settings GUI**:
* 🎶 **Edit BGM:** Sets a background music track (Minecraft sound name) to play during the cinematic.
* 🔭 **Edit Zoom:** Sets a starting and ending FOV zoom level (uses Slowness/Speed effects smoothly).
* 🫨 **Edit Shake:** Adds an earthquake/shaking effect to the camera (Set intensity, e.g., 0.5).
* 🎯 **Set/Clear Focus:** Forces the camera to always look at a specific XYZ location, regardless of where the camera moves. Click to set it to your current standing location.
* ⏱️ **Edit Duration:** Overrides the total length of the cinematic in seconds.

### 3. Tracks & Frames Management
Inside the Cinematic GUI, click **Manage Tracks**:
* A cinematic can have multiple tracks (e.g., "Main Camera", "Alternative Angle").
* Click a Track to view its **Frames**.
* **Frames** are the keyframes of your path. You can Shift-Right-Click to delete a bad frame, or Click it to add **Commands** that will execute when the camera reaches this exact spot.

### 4. Timeline & Transitions
Inside the Cinematic GUI, click **Manage Timeline**:
* Link different tracks together (e.g., Play Track A, then transition to Track B).
* Click a clip to add a **Transition Effect** (like `DARKEN_FADE`) to create cinematic black-screen fades between camera cuts.

### 5. Playing the Cinematic
Use `/cine play <player> <name>` (can be used in console or command blocks). The player will be put into Spectator mode, bound to an invisible, optimized camera entity, and returned safely to their original location once finished.

---
<br>

# 🇻🇳 ExtralyCinematic v2.5 (Vietnamese)

Plugin tạo hiệu ứng Cinematic và Cutscene chuyên nghiệp, siêu nhẹ dành cho Paper 1.21.11+. Tạo ra các góc máy quay đẹp mắt, hiệu ứng hình ảnh và đồng bộ hành động mà không bao giờ lo lỗi NMS.

## 🚀 Tính năng mới (Bản cập nhật Tối ưu hóa v2.5)
* **Thuần Bukkit API:** Loại bỏ hoàn toàn sự phụ thuộc vào `CommandAPI` và NMS. Plugin giờ đây siêu nhẹ (~50KB), tương thích vĩnh viễn với các bản cập nhật Minecraft sau này.
* **Hệ thống GUI 2.0:** Nâng cấp toàn bộ menu lên chuẩn `InventoryHolder`. Chống mọi lỗi kẹt click, giật lag UI.
* **Deep GUI Editor:** Quản lý mọi thứ từ Dashboard 45-slot. Chỉnh sửa Nhạc nền (BGM), Zoom, Rung màn hình (Shake), Tiêu điểm nhìn (Focus), và Thời lượng (Duration) trực tiếp trong GUI mà không cần gõ lệnh thủ công.
* **Nội suy mượt mà:** Sử dụng thuật toán Catmull-Rom Spline để di chuyển và xoay góc nhìn camera siêu mượt, loại bỏ hoàn toàn hiện tượng giật lag khi spectate.
* **Khung hình hành động (Action Frames):** Kích hoạt Lệnh (Command), Tiêu đề (Title/Subtitle) chính xác tại khung hình camera đi qua.
* **Tích hợp:** Hỗ trợ tốt PlaceholderAPI, WorldGuard, và MythicMobs.

## 📦 Cài đặt
1. Tải file `ExtralyCinematic-2.5.jar`.
2. Đặt vào thư mục `plugins/` của server.
3. Khởi động lại server (Không dùng lệnh `/reload`).
4. *(Tùy chọn)* Cài đặt thêm PlaceholderAPI, WorldGuard hoặc MythicMobs để mở rộng tính năng.

## ⌨️ Lệnh & Quyền hạn
**Lệnh chính:** `/cinematic` hoặc `/cine`
**Quyền:** `cinematic.cmd` (Sử dụng lệnh) | `cinematic.admin` (Reload)

| Lệnh | Mô tả |
| :--- | :--- |
| `/cine edit` | Mở bảng điều khiển Dashboard GUI chính. |
| `/cine rec <tên> [giây]` | Tạo cinematic và đếm ngược để quay hình. |
| `/cine record start <tên>` | Bắt đầu quay hình tự do. |
| `/cine record stop` | Dừng quay hình. |
| `/cine play <người_chơi> <tên>` | Trình chiếu cinematic cho một người chơi. |
| `/cine stop <người_chơi>` | Buộc dừng cinematic đang chiếu. |
| `/cine reload` | Tải lại config và dữ liệu cinematic. |

## 📖 Wiki & Hướng dẫn sử dụng

### 1. Tạo một Cinematic
Có 2 cách để tạo một đoạn phim:
* **Cách nhanh (Record):** Gõ `/cine rec <Tên> 3`. Plugin sẽ đếm ngược 3 giây, sau đó ghi lại chính xác quỹ đạo di chuyển của bạn trong game làm đường dẫn camera. Gõ `/cine record stop` khi hoàn thành.
* **Cách thủ công (Chính xác):** Gõ `/cine edit`, tạo Track mới và thêm các Frame (Khung hình) thủ công tại vị trí bạn đang đứng.

### 2. Bảng điều khiển (Dashboard Editor - `/cine edit`)
Đây là trái tim của bản v2.5. Mở Dashboard sẽ hiển thị toàn bộ Cinematic của bạn. Click vào một Cinematic để mở **Cài đặt chuyên sâu**:
* 🎶 **Edit BGM:** Đặt nhạc nền (tên âm thanh Minecraft) phát trong lúc chiếu phim.
* 🔭 **Edit Zoom:** Đặt mức độ FOV phóng to/thu nhỏ lúc bắt đầu và kết thúc (Sử dụng hiệu ứng Speed/Slowness một cách mượt mà).
* 🫨 **Edit Shake:** Thêm hiệu ứng động đất/rung lắc camera (Ví dụ cường độ: 0.5).
* 🎯 **Set/Clear Focus:** Ép camera luôn luôn nhìn thẳng vào một tọa độ XYZ cố định, bất kể camera đang bay đi đâu. Click để cài đặt tiêu điểm tại vị trí bạn đang đứng.
* ⏱️ **Edit Duration:** Ghi đè tổng thời lượng của cinematic (tính bằng giây).

### 3. Quản lý Tracks & Frames
Bên trong Cinematic GUI, click **Manage Tracks**:
* Một cinematic có thể có nhiều Track (VD: "Máy quay chính", "Máy quay góc cao").
* Click vào một Track để xem các **Frames** (Khung hình).
* **Frames** là các điểm tụ của đường bay camera. Bạn có thể Shift-Right-Click để xóa frame lỗi, hoặc Click vào nó để thêm **Lệnh (Commands)**. Các lệnh này sẽ được tự động chạy khi camera bay ngang qua đúng điểm đó.

### 4. Dòng thời gian & Chuyển cảnh (Timeline & Transitions)
Bên trong Cinematic GUI, click **Manage Timeline**:
* Ghép nối các Track lại với nhau (VD: Chiếu Track A, sau đó chuyển sang Track B).
* Click vào một Clip để thêm **Hiệu ứng chuyển cảnh (Transition)** (như `DARKEN_FADE`) để tạo hiệu ứng mờ đen màn hình hệt như phim rạp khi chuyển góc máy.

### 5. Trình chiếu Cinematic
Sử dụng lệnh `/cine play <tên_người_chơi> <tên_cinematic>` (có thể dùng trong console hoặc command block). Người chơi sẽ được chuyển sang chế độ Khán giả (Spectator), khóa góc nhìn vào một thực thể camera tàng hình siêu mượt, và được trả về vị trí cũ an toàn khi phim kết thúc.