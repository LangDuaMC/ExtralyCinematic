# 🎥 ExtralyCinematic

> A lightweight, smooth, and feature-rich recording and playback plugin for Paper servers.
> 
> *[Cuộn xuống để xem phiên bản Tiếng Việt / Scroll down for the Vietnamese version](#-phiên-bản-tiếng-việt-vietnamese-version)*

---

## 🇬🇧 ENGLISH VERSION

**ExtralyCinematic** allows administrators to record player movements and play them back for other players as smooth camera fly-throughs. Designed for high performance, it features advanced path smoothing, an intuitive in-game GUI editor, and visual path debugging.

### ✨ Key Features
* **Smooth Camera Pathing (Catmull-Rom Spline):** Replaces rigid linear interpolation with advanced mathematical curves. The camera gracefully glides through corners without jarring snaps or robotic movements.
* **Smart Angle Wrapping:** Automatically calculates the shortest rotation path to prevent the camera from violently snapping 360 degrees when looking around.
* **In-Game GUI Editor:** Type `/cinematic edit` to access a clean, paginated inventory GUI where you can teleport to frames, delete bad frames, and open the Command Editor.
* **Visual Path Debugging:** Use `/cinematic path <name>` (or click the Ender Eye in the GUI) to spawn a 10-second particle trail outlining the exact flight path.
* **Command Triggers on Frames:** Attach server commands (with `%player%` support) to specific keyframes.
* **Optimized Storage:** Cinematics are saved individually as `.json` files inside the `plugins/ExtralyCinematic/cinematics/` folder.

### 📥 Installation & Setup
1. Download the compiled `.jar` file.
2. Place it into your server's `plugins/` directory.
3. Restart the server to generate the default configuration files.

### ⚙️ Configuration (`config.yml`)
You can fine-tune the plugin's performance and smoothness by editing `plugins/ExtralyCinematic/config.yml`.

```yaml
# The number of interpolation steps between two frames.
# Higher value = Smoother camera movement, but requires more teleport packets (can cause slight lag for players with high ping).
# Lower value = Less packets sent, but movement might look a bit rigid.
# Recommended default: 10
interpolation-steps: 10
```

### 📜 Commands & Permissions
*Permission required for all commands: `cinematic.cmd`*

| Command | Description |
|---|---|
| `/cinematic record start <name>` | Start recording on-the-fly. Move around to capture frames. |
| `/cinematic record stop` | Stop and save the current on-the-fly recording. |
| `/cinematic rec <name> <seconds>` | Record a cinematic for a fixed duration with a countdown. |
| `/cinematic play <player> <name>` | Play a cinematic for a specific player. |
| `/cinematic stop <player>` | Force-stop an ongoing cinematic for a player. |
| `/cinematic path <name>` | Visualize the camera path using particles in-game. |
| `/cinematic edit` | **[RECOMMENDED]** Open the main GUI Editor to manage everything. |
| `/cinematic list` | List all available cinematics in chat. |
| `/cinematic delete <name>` | Delete a cinematic file completely. |

---

## 🇻🇳 PHIÊN BẢN TIẾNG VIỆT (VIETNAMESE VERSION)

**ExtralyCinematic** là một plugin siêu nhẹ và hiệu năng cao dành cho các server Paper. Plugin giúp quản trị viên ghi lại quỹ đạo bay và phát lại cho người chơi xem dưới dạng các đoạn cắt cảnh (cinematic) mượt mà như trong phim.

### ✨ Tính năng nổi bật
* **Camera siêu mượt (Catmull-Rom Spline):** Loại bỏ hoàn toàn sự giật cục của di chuyển đường thẳng (Linear). Camera uốn lượn qua các góc cua mượt mà giống hệt như dùng ReplayMod.
* **Chống lật camera (Smart Angle Wrapping):** Tự động tính toán góc quay ngắn nhất, ngăn chặn lỗi camera bị giật xoay tròn 360 độ.
* **Trình quản lý GUI (In-Game Editor):** Dùng `/cinematic edit` để mở giao diện quản lý. Bạn có thể dịch chuyển đến từng frame, xóa các frame quay hỏng, hoặc mở trình chỉnh sửa lệnh.
* **Vẽ đường bay trực quan (Visual Pathing):** Dùng lệnh `/cinematic path <name>` để vẽ ra quỹ đạo bay bằng hạt lửa (Flame) và hướng nhìn (End Rod) trong 10 giây để dễ dàng bắt lỗi.
* **Gắn lệnh vào Frame:** Tự động chạy lệnh server khi camera bay đến một khung hình cụ thể (Hỗ trợ biến `%player%`).
* **Lưu trữ tối ưu:** Mỗi cinematic được lưu thành một file `.json` riêng tại `plugins/ExtralyCinematic/cinematics/`.

### 📥 Hướng dẫn Cài đặt
1. Bỏ file `.jar` của plugin vào thư mục `plugins/` của server.
2. Khởi động lại server để plugin tạo các thư mục và file cấu hình mặc định.

### ⚙️ Cấu hình (`config.yml`)
Bạn có thể tinh chỉnh độ mượt và hiệu năng của plugin thông qua file `plugins/ExtralyCinematic/config.yml`.

```yaml
# Số bước chia nhỏ (nội suy) giữa 2 khung hình (frame).
# Số càng TO = Camera bay càng mượt, nhưng server phải gửi nhiều packet dịch chuyển hơn (có thể gây giật nhẹ cho người chơi ping cao).
# Số càng NHỎ = Ít tốn tài nguyên server hơn, nhưng camera có thể không mượt bằng.
# Mức khuyến nghị mặc định: 10
interpolation-steps: 10
```

### 📜 Lệnh & Quyền hạn (Permissions)
*Tất cả các lệnh đều yêu cầu quyền: `cinematic.cmd`*

| Lệnh | Mô tả |
|---|---|
| `/cinematic record start <name>` | Bắt đầu ghi hình tự do. Bay lượn để lưu frame. |
| `/cinematic record stop` | Dừng và lưu lại quá trình ghi hình tự do. |
| `/cinematic rec <name> <giây>` | Ghi hình theo thời gian đếm ngược chỉ định sẵn. |
| `/cinematic play <player> <name>` | Phát cinematic cho người chơi xem. |
| `/cinematic stop <player>` | Ép dừng cinematic của người chơi và trả họ về chỗ cũ. |
| `/cinematic path <name>` | Hiển thị đường bay bằng Particle trong 10 giây. |
| `/cinematic edit` | **[KHUYÊN DÙNG]** Mở giao diện GUI để thao tác mọi thứ. |
| `/cinematic list` | Liệt kê danh sách cinematic đang có. |
| `/cinematic delete <name>` | Xóa hoàn toàn một cinematic. |

### 🚀 Hướng dẫn Quy trình Sử dụng Nhanh
1. Bay đến điểm xuất phát, gõ `/cinematic record start Intro`.
2. Bay lượn từ từ theo quỹ đạo bạn muốn. Cứ thoải mái, thuật toán của plugin sẽ tự làm mượt các đoạn run tay!
3. Gõ `/cinematic record stop` khi hoàn thành.
4. Gõ `/cinematic edit` và bấm vào biểu tượng Mắt Ender để kiểm tra lại đường bay. Xóa các frame thừa/hỏng nếu cần.
5. Gõ `/cinematic play <player> Intro` để phát thử!