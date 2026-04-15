# CinematicCore

> A lightweight, smooth, and feature-rich recording and playback plugin for Paper servers.
> 
> *[Cuộn xuống để xem phiên bản Tiếng Việt / Scroll down for the Vietnamese version](#phiên-bản-tiếng-việt-vietnamese-version)*

---

## 🇬🇧 ENGLISH VERSION

**CinematicCore** allows administrators to record player movements and play them back for other players as smooth camera fly-throughs. Designed for high performance, it features advanced path smoothing, an intuitive in-game GUI editor, and visual path debugging.

### ✨ Features
* **Smooth Camera Pathing (Catmull-Rom Spline):** Replaces rigid linear movement with smooth curves. The camera gracefully glides through corners without jarring snaps.
* **In-Game GUI Editor:** No more fumbling with complex commands! Type `/cinematic edit` to view, manage, and edit frames directly via a clean inventory GUI.
* **Visual Path Debugging:** Use `/cinematic path <name>` to spawn a 10-second particle trail outlining the exact flight path and camera direction in-game.
* **Command Triggers on Frames:** Attach server commands to specific keyframes. When the camera reaches that frame, the command executes automatically.
* **Optimized Storage:** Cinematics are saved individually as `.json` files inside the `cinematics/` folder, preventing monolithic file corruption and optimizing read/write speeds.
* **Smart Cleanup:** Players are instantly removed from active viewing sessions if they disconnect, preventing phantom tasks and saving resources.

### 📜 Commands & Permissions
*Permission required for all commands: `cinematic.cmd`*

| Command | Description |
|---|---|
| `/cinematic rec <name> <seconds>` | Record a cinematic for a fixed duration (countdown method). |
| `/cinematic record start <name>` | Start recording on-the-fly (unlimited duration). |
| `/cinematic record stop` | Stop the current on-the-fly recording. |
| `/cinematic play <player> <name>` | Play a cinematic for a specific player. |
| `/cinematic stop <player>` | Force-stop an ongoing cinematic for a player. |
| `/cinematic path <name>` | Visualize the camera path using particles. |
| `/cinematic edit` | Open the main GUI Editor. |
| `/cinematic list` | List all available cinematics in chat. |
| `/cinematic delete <name>` | Delete a cinematic via command (also available in GUI). |
| `/cinematic addcmd <name> <frame> <cmd>` | Add a command to a specific frame. |

### 🚀 Quick Start Guide
1. Run `/cinematic record start MyIntro`.
2. Fly around to capture your frames. 
3. Run `/cinematic record stop` to finish.
4. (Optional) Run `/cinematic path MyIntro` to see the exact curve of your camera.
5. (Optional) Run `/cinematic edit`, click `MyIntro` to teleport to specific frames, delete bad frames, or add commands.
6. Run `/cinematic play <player> MyIntro` to test it!

---

## 🇻🇳 PHIÊN BẢN TIẾNG VIỆT (VIETNAMESE VERSION)

**CinematicCore** là một plugin siêu nhẹ giúp quản trị viên ghi lại quỹ đạo bay và phát lại cho người chơi xem dưới dạng các đoạn cắt cảnh (cinematic) mượt mà. Plugin được tối ưu hóa với thuật toán làm mượt quỹ đạo, giao diện GUI trực quan và hệ thống xem trước đường bay bằng Particle.

### ✨ Tính năng nổi bật
* **Camera siêu mượt (Catmull-Rom Spline):** Loại bỏ hoàn toàn sự giật cục của di chuyển đường thẳng (Linear). Camera giờ đây sẽ uốn lượn mượt mà qua các góc cua giống như dùng ReplayMod.
* **Chỉnh sửa qua GUI (In-Game Editor):** Không cần nhớ lệnh lằng nhằng! Dùng `/cinematic edit` để mở giao diện túi đồ, từ đó có thể xem, xóa frame, dịch chuyển đến vị trí frame, và quản lý các đoạn cinematic.
* **Hiển thị đường bay (Visual Pathing):** Lệnh `/cinematic path <name>` sẽ vẽ ra một dải hạt (particles) in-game trong 10 giây, giúp bạn nhìn thấy chính xác quỹ đạo bay và hướng nhìn của camera.
* **Gắn lệnh vào Frame:** Bạn có thể cài đặt để server tự động chạy một lệnh nào đó (ví dụ: phát âm thanh, nổ pháo hoa) khi camera bay đến một khung hình (frame) cụ thể.
* **Lưu trữ thông minh:** Mỗi cinematic được lưu thành một file `.json` riêng biệt trong thư mục `cinematics/`. Tránh tình trạng hỏng toàn bộ dữ liệu và tối ưu tốc độ đọc/ghi.
* **Tự động dọn dẹp:** Tự động hủy cinematic nếu người xem thoát khỏi server, giúp tiết kiệm tài nguyên.

### 📜 Lệnh & Quyền hạn (Permissions)
*Quyền yêu cầu cho tất cả các lệnh: `cinematic.cmd`*

| Lệnh | Mô tả |
|---|---|
| `/cinematic rec <name> <giây>` | Ghi hình theo thời gian cố định (Có đếm ngược). |
| `/cinematic record start <name>` | Bắt đầu ghi hình tự do. |
| `/cinematic record stop` | Kết thúc quá trình ghi hình tự do. |
| `/cinematic play <player> <name>` | Phát cinematic cho người chơi xem. |
| `/cinematic stop <player>` | Ép dừng cinematic của một người chơi. |
| `/cinematic path <name>` | Hiển thị đường bay bằng Particle. |
| `/cinematic edit` | Mở giao diện GUI để chỉnh sửa cinematic. |
| `/cinematic list` | Liệt kê danh sách cinematic đang có. |
| `/cinematic delete <name>` | Xóa cinematic (Có thể làm trực tiếp trong GUI). |
| `/cinematic addcmd <name> <frame> <cmd>` | Gắn 1 lệnh vào một khung hình cụ thể. |

### 🚀 Hướng dẫn nhanh
1. Dùng lệnh `/cinematic record start MyIntro`.
2. Bay lượn xung quanh để plugin ghi lại các khung hình.
3. Dùng lệnh `/cinematic record stop` để kết thúc quay.
4. (Tùy chọn) Gõ `/cinematic path MyIntro` để nhìn trực quan đường bay bạn vừa quay bằng Particle.
5. (Tùy chọn) Gõ `/cinematic edit`, bấm vào `MyIntro` để dịch chuyển đến từng frame, xóa các frame bị lỗi, hoặc gắn lệnh vào frame.
6. Gõ `/cinematic play <player> MyIntro` để tận hưởng thành quả!